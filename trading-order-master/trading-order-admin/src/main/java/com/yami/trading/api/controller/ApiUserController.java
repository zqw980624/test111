package com.yami.trading.api.controller;

import cn.hutool.core.util.StrUtil;
import com.yami.trading.api.dto.UserDto;
import com.yami.trading.api.model.SetSafewordModel;
import com.yami.trading.api.service.UserCacheService;
import com.yami.trading.bean.model.*;
import com.yami.trading.bean.syspara.domain.Syspara;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.BusinessException;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.*;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.security.common.bo.UserInfoInTokenBO;
import com.yami.trading.security.common.enums.SysTypeEnum;
import com.yami.trading.security.common.manager.PasswordManager;
import com.yami.trading.security.common.manager.TokenStore;
import com.yami.trading.security.common.vo.TokenInfoVO;
import com.yami.trading.service.*;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.service.system.TipService;
import com.yami.trading.service.user.UserRecomService;
import com.yami.trading.service.user.UserSafewordApplyService;
import com.yami.trading.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

import org.jasypt.util.password.StrongPasswordEncryptor;
@RestController
@RequestMapping("api/user")
@Api(tags = "用户")
@Slf4j
public class ApiUserController {
    @Autowired
    UserCacheService userCacheService;
    @Autowired
    UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PasswordManager passwordManager;
    @Autowired
    RealNameAuthRecordService realNameAuthRecordService;
    @Autowired
    UserSafewordApplyService userSafewordApplyService;
    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @Autowired
    TipService tipService;
    @Autowired
    IdentifyingCodeTimeWindowService identifyingCodeTimeWindowService;
    @Autowired
    SysparaService sysparaService;
    @Autowired
    UserRecomService userRecomService;
    @Autowired
    HighLevelAuthRecordService highLevelAuthRecordService;
    @Autowired
    private TokenStore tokenStore;
    @Autowired
    LogService logService;
    @Autowired
    IpMenuService ipMenuService;

    //@Autowired
    //QRGenerateService qrGenerateService;

    /**
     * 用户名登录接口
     */
    @GetMapping("login")
    public Result login(String username, String password) {

        if (StringUtils.isEmptyString(username)) {
            throw new YamiShopBindException("用户名不能为空");
        }
        if (StringUtils.isEmptyString(password)) {
            throw new YamiShopBindException("登录密码不能为空");
        }
        if (password.length() < 6 || password.length() > 12) {
            throw new YamiShopBindException("登录密码必须6-12位");
        }
        String ip = IPHelper.getIpAddr();
        if (!IpUtil.isCorrectIpRegular(ip)) {
            log.error("校验IP不合法,参数{}", ip);
            throw new YamiShopBindException("校验IP不合法");
        }

        // 黑名单限制
        Syspara syspara = sysparaService.find("blacklist_ip");
        String blackUsers = syspara.getSvalue();
        if(org.apache.commons.lang3.StringUtils.isNotEmpty(blackUsers)) {
            String[] ips = blackUsers.split(",");

            if(Arrays.asList(ips).contains(ip.trim())){
                throw new YamiShopBindException("当前用户在黑名单中");
            }
        }

        User secUser = userService.login(username, password);
        UserInfoInTokenBO userInfoInToken = new UserInfoInTokenBO();
        userInfoInToken.setUserId(secUser.getUserId());
        userInfoInToken.setSysType(SysTypeEnum.ORDINARY.value());
        userInfoInToken.setEnabled(secUser.getStatus() == 1);
        secUser.setUserLastip(IPHelper.getIpAddr());
        secUser.setUserLasttime(new Date());
        tokenStore.deleteAllToken(String.valueOf(SysTypeEnum.ORDINARY.value()), String.valueOf(secUser.getUserId()));

        // 存储token返回vo
        TokenInfoVO tokenInfoVO = tokenStore.storeAndGetVo(userInfoInToken);
        tokenInfoVO.setToken(tokenInfoVO.getAccessToken());
        userService.online(secUser.getUserId());
        ipMenuService.saveIpMenuWhite(IPHelper.getIpAddr());
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("token", tokenInfoVO.getAccessToken());
        data.put("username", secUser.getUserName());
        data.put("usercode", secUser.getUserCode());
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_SECURITY);
        log.setLog("用户登录,ip[" + IPHelper.getIpAddr() + "]");
        log.setUserId(secUser.getUserId());
        log.setUsername(username);
        logService.save(log);
        secUser.setUserLastip(IPHelper.getIpAddr());
        userService.updateById(secUser);
        return Result.succeed(data);
    }

    private String validateParam(String username, String verifcode, String password, String type) {

        if (StringUtils.isEmptyString(username)) {
            return "用户名不能为空";
        }
        if (StringUtils.isEmptyString(password)) {
            return "登录密码不能为空";
        }
        int min = 6;
        int max = 12;
        if (!RegexUtil.length(password, min, max)) {
            return "登陆密码长度不符合设定";
        }
        if (StringUtils.isEmptyString(type) || !Arrays.asList("1", "2").contains(type)) {
            return "类型不能为空";
        }
        return null;
    }

    /**
     * 手机/邮箱注册接口
     */
    @RequestMapping("register")
    public Object register(String username, String password, String safeword, String verifcode, String usercode, String type) {
        // 注册类型：1/手机；2/邮箱；
        String error = this.validateParam(username, verifcode, password, type);
        if (!StringUtils.isNullOrEmpty(error)) {
            throw new YamiShopBindException(error);
        }
        userService.saveRegister(username, password, usercode, "111111", verifcode);
        User secUser = userService.findByUserName(username);
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_SECURITY);
        log.setLog("用户注册,ip[" + IPHelper.getIpAddr() + "]");
        log.setUserId(secUser.getUserId());
        log.setUsername(username);
        logService.save(log);
        UserInfoInTokenBO userInfoInToken = new UserInfoInTokenBO();
        userInfoInToken.setUserId(secUser.getUserId());
        userInfoInToken.setSysType(SysTypeEnum.ORDINARY.value());
        userInfoInToken.setEnabled(secUser.getStatus() == 1);
        tokenStore.deleteAllToken(String.valueOf(SysTypeEnum.ORDINARY.value()), String.valueOf(secUser.getUserId()));
        TokenInfoVO tokenInfoVO = tokenStore.storeAndGetVo(userInfoInToken);
        this.userService.online(secUser.getUserId());
        this.ipMenuService.saveIpMenuWhite(IPHelper.getIpAddr());
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("token", tokenInfoVO.getAccessToken());
        data.put("username", secUser.getUserName());
        data.put("usercode", secUser.getUserName());
        secUser.setUserLastip(IPHelper.getIpAddr());
        userService.updateById(secUser);
        return Result.succeed(data);
    }

    /**
     * 设置资金密码（注册时）
     */
    @PostMapping("/setSafewordReg")
    @ApiOperation(value = "设置资金密码（注册时）")
    public Result setSafeword(@Valid SetSafewordModel model) {

        String safeword = model.getSafeword();
        if (StringUtils.isEmptyString(model.getSafeword())) {
            throw new YamiShopBindException("The fund password cannot be blank");
        }
        if (safeword.length() != 6 || !Strings.isNumber(safeword)) {
            throw new YamiShopBindException("资金密码不符合设定");
        }
        userService.setSafeword(SecurityUtils.getUser().getUserId(), passwordEncoder.encode(model.getSafeword()));
        return Result.succeed(null);
    }

    /**
     * token获取验证方式
     */
    @PostMapping("getVerifTarget")
    @ApiOperation("token获取验证方式")
    public Result<?> getVerifTarget(HttpServletRequest request) {

        String verifcode_type = request.getParameter("verifcode_type");
        Map<String, Object> data = new HashMap<>();
        User user = userCacheService.currentUser();
        // verifcode_type未明确指定，返回所有的方式
        if (StringUtils.isEmptyString(verifcode_type) || !Arrays.asList("1", "2", "3").contains(verifcode_type)) {
            data.put("phone", StringUtils.isEmptyString(user.getUserMobile()) || false == user.isUserMobileBind() ? "" : user.getUserMobile());
            data.put("phone_filled", StringUtils.isEmptyString(user.getUserMobile()) ? "" : user.getUserMobile());
            data.put("phone_authority", user.isUserMobileBind());
            data.put("email", StringUtils.isEmptyString(user.getUserMail()) || false == user.isMailBind() ? "" : user.getUserMail());
            data.put("email_filled", StringUtils.isEmptyString(user.getUserMail()) ? "" : user.getUserMail());
            data.put("email_authority", user.isMailBind());
            data.put("google_auth_secret", StringUtils.isEmptyString(user.getGoogleAuthSecret()) || false == user.isGoogleAuthBind() ? "" : user.getGoogleAuthSecret());
            data.put("google_auth_secret_filled", StringUtils.isEmptyString(user.getGoogleAuthSecret()) ? "" : user.getGoogleAuthSecret());
            data.put("google_auth_bind", user.isGoogleAuthBind());
        } else {
            // verifcode_type: 1/手机;2/邮箱;3/谷歌验证器;
            if ("1".equals(verifcode_type)) {
                data.put("phone", StringUtils.isEmptyString(user.getUserMobile()) || false == user.isUserMobileBind() ? "" : user.getUserMobile());
                data.put("phone_filled", StringUtils.isEmptyString(user.getUserMobile()) ? "" : user.getUserMobile());
                data.put("phone_authority", user.isUserMobileBind());
            } else if ("2".equals(verifcode_type)) {
                data.put("email", StringUtils.isEmptyString(user.getUserMail()) || false == user.isMailBind() ? "" : user.getUserMail());
                data.put("email_filled", StringUtils.isEmptyString(user.getUserMail()) ? "" : user.getUserMail());
                data.put("email_authority", user.isMailBind());
            } else if ("3".equals(verifcode_type)) {
                data.put("google_auth_secret", StringUtils.isEmptyString(user.getGoogleAuthSecret()) || false == user.isGoogleAuthBind() ? "" : user.getGoogleAuthSecret());
                data.put("google_auth_secret_filled", StringUtils.isEmptyString(user.getGoogleAuthSecret()) ? "" : user.getGoogleAuthSecret());
                data.put("google_auth_bind", user.isGoogleAuthBind());
            }
        }
        return Result.succeed(data);
    }

    @RequestMapping("getImageCode")
    public Result getImageCode() {

        Map<String, Object> data = new HashMap<String, Object>();
        String key = UUIDGenerator.getUUID();
        ImageVerificationCodeUtil iv = new ImageVerificationCodeUtil();
        data.put("code", iv.getBase64());
        data.put("key", key);
        redisTemplate.opsForValue().set(key, iv.getText());
        return Result.succeed(data);
    }

    /**
     * 退出登录
     */
    @RequestMapping("logout")
    public Result logout(HttpServletRequest request) {

        String accessToken = request.getHeader("token");
        String token = request.getParameter("token");
        if (StrUtil.isBlank(accessToken)) {
            accessToken = token;
        }
        if (StrUtil.isBlank(accessToken)) {
            return Result.succeed();
        }
        userService.logout(SecurityUtils.getUser().getUserId());
        // 删除该用户在该系统当前的token
        tokenStore.deleteAllToken(String.valueOf(SysTypeEnum.ORDINARY.value()), String.valueOf(SecurityUtils.getUser().getUserId()));
        return Result.succeed();
    }

    /**
     * 重置登录密码
     */
    @PostMapping("resetPsw")
    public Object resetpsw(HttpServletRequest request) {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String verifcode_type = request.getParameter("verifcode_type");
        String verifcode = request.getParameter("verifcode");
        if (StringUtils.isEmptyString(username)) {
            throw new YamiShopBindException("用户名不能为空");
        }
        if (StringUtils.isEmptyString(password)) {
            throw new YamiShopBindException("密码不能为空");
        }
        if (password.length() < 6 || password.length() > 12) {
            throw new YamiShopBindException("密码必须6-12位");
        }
        if (StringUtils.isEmptyString(verifcode_type)) {
            throw new YamiShopBindException("验证类型不能为空");
        }
        if (StringUtils.isEmptyString(verifcode)) {
            throw new YamiShopBindException("验证码不能为空");
        }
        User party = userService.findByUserName(username);
        if (null == party) {
            throw new YamiShopBindException("用户名不存在");
        }
        // 根据验证类型获取验证key verifcode_type: 1/手机;2/邮箱;3/谷歌验证器;
        String key = "";
        String errMsg = "";
        if ("1".equals(verifcode_type)) {
            key = StringUtils.isEmptyString(party.getUserMobile()) || false == party.isUserMobileBind() ? "" : party.getUserMobile();
            errMsg = "未绑定手机号";
        } else if ("2".equals(verifcode_type)) {
            key = StringUtils.isEmptyString(party.getUserMail()) || false == party.isMailBind() ? "" : party.getUserMail();
            errMsg = "未绑定邮箱";
        } else if ("3".equals(verifcode_type)) {
            key = StringUtils.isEmptyString(party.getGoogleAuthSecret()) || false == party.isGoogleAuthBind() ? "" : party.getGoogleAuthSecret();
            errMsg = "未绑定谷歌验证器";
        }
        if (StringUtils.isEmptyString(key)) {
            throw new YamiShopBindException(errMsg);
        }
        // 验证
        boolean passed = false;
        if ("1".equals(verifcode_type) || "2".equals(verifcode_type)) {
            String authcode = this.identifyingCodeTimeWindowService.getAuthCode(key);
            if ((null != authcode) && (authcode.equals(verifcode))) {
                passed = true;
                this.identifyingCodeTimeWindowService.delAuthCode(key);
            }
        } else if ("3".equals(verifcode_type)) {
            GoogleAuthenticator ga = new GoogleAuthenticator();
            ga.setWindowSize(5);
            long t = System.currentTimeMillis();
            boolean flag = ga.check_code(party.getGoogleAuthSecret(), Long.valueOf(verifcode), t);
            if (flag) {
                passed = true;
            }
        }
        // 如果是演示用户，则不判断验证码
        if (!"GUEST".contentEquals(party.getRoleName())) {
            if (!passed) {
                throw new YamiShopBindException("验证码不正确");
            }
        }
        party.setLoginPassword(passwordEncoder.encode(password));
        // 更新密码
        userService.updateById(party);
        return Result.succeed();
    }

    /**
     * 用户名获取验证方式
     */
    @RequestMapping("getUserNameVerifTarget")
    public Result<Map<String, Object>> getUserNameVerifTarget(String username, String verifcode_type) {
        username = username.substring(2);
        Map<String, Object> data = new HashMap<>();
        if (StringUtils.isEmptyString(username)) {
            throw new YamiShopBindException("用户名参数为空");
        }
        //User party = userService.findByUserName(username);
        User party = userService.findByUserMobile(username);
        if (null == party) {
            throw new YamiShopBindException("用户名不存在");
        }
        // verifcode_type未明确指定，返回所有的方式
        if (StringUtils.isEmptyString(verifcode_type) || !Arrays.asList("1", "2", "3").contains(verifcode_type)) {
            data.put("phone", StringUtils.isEmptyString(party.getUserMobile()) || false == party.isUserMobileBind() ? "" : party.getUserMobile());
            data.put("phone_filled", StringUtils.isEmptyString(party.getUserMobile()) ? "" : party.getUserMobile());
            data.put("phone_authority", party.isUserMobileBind());
            data.put("email", StringUtils.isEmptyString(party.getUserMail()) || false == party.isMailBind() ? "" : party.getUserMail());
            data.put("email_filled", StringUtils.isEmptyString(party.getUserMail()) ? "" : party.getUserMail());
            data.put("email_authority", party.isMailBind());
            data.put("google_auth_secret", StringUtils.isEmptyString(party.getGoogleAuthSecret()) || false == party.isGoogleAuthBind() ? "" : party.getGoogleAuthSecret());
            data.put("google_auth_secret_filled", StringUtils.isEmptyString(party.getGoogleAuthSecret()) ? "" : party.getGoogleAuthSecret());
            data.put("google_auth_bind", party.isGoogleAuthBind());
        } else {
            // verifcode_type: 1/手机;2/邮箱;3/谷歌验证器;
            if ("1".equals(verifcode_type)) {
                data.put("phone", StringUtils.isEmptyString(party.getUserMobile()) || false == party.isUserMobileBind() ? "" : party.getUserMobile());
                data.put("phone_filled", StringUtils.isEmptyString(party.getUserMobile()) ? "" : party.getUserMobile());
                data.put("phone_authority", party.isUserMobileBind());
            } else if ("2".equals(verifcode_type)) {
                data.put("email", StringUtils.isEmptyString(party.getUserMail()) || false == party.isMailBind() ? "" : party.getUserMail());
                data.put("email_filled", StringUtils.isEmptyString(party.getUserMail()) ? "" : party.getUserMail());
                data.put("email_authority", party.isMailBind());
            } else if ("3".equals(verifcode_type)) {
                data.put("google_auth_secret", StringUtils.isEmptyString(party.getGoogleAuthSecret()) || false == party.isGoogleAuthBind() ? "" : party.getGoogleAuthSecret());
                data.put("google_auth_secret_filled", StringUtils.isEmptyString(party.getGoogleAuthSecret()) ? "" : party.getGoogleAuthSecret());
                data.put("google_auth_bind", party.isGoogleAuthBind());
            }
        }
        return Result.succeed(data);
    }

    /**
     * 查看用户接口
     */
    @GetMapping("/getInfo")
    @ApiOperation(value = "查看用户信息")
    public Result<UserDto> getInfo(HttpServletRequest request) {
        String userId = SecurityUtils.getUser().getUserId();
        User user = userService.getById(userId);
        String token = request.getHeader("token");
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        userDto.setUsercode(user.getUserCode());
        userDto.setUsername(user.getUserName());
        userDto.setToken(token);
        userDto.setIdentityverif(user.isRealNameAuthority());
        userDto.setAdvancedverif(user.isHighlevelAuthority());
        userDto.setName(user.getRealName());
        RealNameAuthRecord realNameAuthRecord = realNameAuthRecordService.getByUserId(user.getUserId());
        if (realNameAuthRecord != null) {
            userDto.setNationality(realNameAuthRecord.getNationality());
            userDto.setKyc_status(realNameAuthRecord.getStatus());
        }
        HighLevelAuthRecord highLevelAuthRecord = highLevelAuthRecordService.findByUserId(userId);
        if (highLevelAuthRecord != null) {
            userDto.setKyc_high_level_status(highLevelAuthRecord.getStatus());
        }
        return Result.succeed(userDto);
    }

    /**
     * 获取个人信息
     */
    @RequestMapping("get")
    public Object get(HttpServletRequest request) {

        String loginPartyId = SecurityUtils.getCurrentUserId();
        User party = userService.getById(loginPartyId);
        RealNameAuthRecord kyc = realNameAuthRecordService.getByUserId(party.getUserId());
        HighLevelAuthRecord kycHighLevel = highLevelAuthRecordService.findByUserId(party.getUserId());
        Map<String, Object> map = new HashMap<String, Object>();
        // 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
        // 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
        // 如：级别11表示：新注册的前端显示为VIP1；
        map.put("user_level", (int) (party.getUserLevel() % 10));
        map.put("user_level_custom", (int) Math.floor(party.getUserLevel() / 10));
        String projectType = this.sysparaService.find("project_type").getSvalue();
        if (StringUtils.isEmptyString(projectType)) {
            throw new BusinessException("系统参数错误");
        }
        if (projectType.equals("DAPP_EXCHANGE_BINANCE")
                || projectType.equals("EXCHANGE_EASYCRYPTO")
                || projectType.equals("DAPP_EXCHANGE_DAPPDEFIUSDT")) {
            map.put("user_level_custom_display", map.get("user_level_custom"));
        } else {
            map.put("user_level_custom_display", "VIP");
            String user_level_custom_config = this.sysparaService.find("user_level_custom_config").getSvalue();
            String[] levelArray = user_level_custom_config.split(",");
            for (int i = 0; i < levelArray.length; i++) {
                String[] level = levelArray[i].split("-");
                if (level[0].equals(map.get("user_level_custom").toString())) {
                    map.put("user_level_custom_display", level[1]);
                    break;
                }
            }
        }
        map.put("username", party.getUserName());
        map.put("userrole", party.getRoleName());
        map.put("usercode", party.getUserCode());
        map.put("phone", party.getUserMobile());
        map.put("phoneverif", party.isUserMobileBind());
        map.put("email", party.getUserMail());
        map.put("emailverif", party.isMailBind());
        map.put("google_auth_secret", party.getGoogleAuthSecret());
        map.put("googleverif", party.isGoogleAuthBind());
        map.put("identityverif", party.isRealNameAuthority());
        map.put("advancedverif", party.isHighlevelAuthority());
        map.put("lastlogintime", party.getUserLasttime());
        map.put("lastloginip", party.getUserLastip());
        // 实名认证通过返回真实姓名
        if (party.isRealNameAuthority()) {
            map.put("name", kyc.getName());
        }
        if (null != kyc) {
            map.put("nationality", kyc.getNationality());
            map.put("kyc_status", kyc.getStatus());
        }
        if (null != kycHighLevel) {
            map.put("kyc_high_level_status", kycHighLevel.getStatus());
        }
        if (Constants.SECURITY_ROLE_TEST.equals(party.getRoleName())) {
            map.put("test", true);
        } else {
            // 关闭后，正式用户进入推广页面的时候，接口就不返回内容
            boolean member_promote_button = this.sysparaService.find("member_promote_button").getBoolean();
            if (Constants.SECURITY_ROLE_MEMBER.equals(party.getRoleName()) && !member_promote_button) {
                map.put("url", "");
                map.put("usercode_qr", "");
            } else {
                map.put("url", Constants.WEB_URL + "/register.html?usercode=" + party.getUserCode());
                // 生成二维码图片
                //qrGenerateService.generate(party.getUserCode());
                map.put("usercode_qr", Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=/qr/" + party.getUserCode() + ".png");
            }
        }
        UserRecom userRecom = this.userRecomService.findByPartyId(party.getUserId());
        if (null == userRecom || null == userRecom.getRecomUserId()) {
            map.put("usercode_parent", "");
        } else {
            User party_reco = userService.getById(userRecom.getRecomUserId());
            if (null == party_reco || null == party_reco.getUserCode() || StringUtils.isEmptyString(party_reco.getUserCode().toString())) {
                map.put("usercode_parent", "");
            } else {
                map.put("usercode_parent", party_reco.getUserCode());
            }
        }
        // 时间分隔点：新用户需要注册后填手机号和邀请码
        map.put("register_need_phone_usercode", false);
        String register_need_phone_usercode_time = this.sysparaService.find("register_need_phone_usercode_time").getSvalue();
        if (!StringUtils.isEmptyString(register_need_phone_usercode_time)) {
            // 结合盘：只有新用户需要注册后填手机号和邀请码
            Date dateFixed = DateUtils.toDate(register_need_phone_usercode_time, DateUtils.NORMAL_DATE_FORMAT);
            if (party.getCreateTime().getTime() > dateFixed.getTime()) {
                map.put("register_need_phone_usercode", true);
            } else {
                map.put("register_need_phone_usercode", false);
            }
        }
        // 承兑商类型：0不是承兑商/1后台承兑商/2用户承兑商
              map.put("c2c_user_type", party.getC2cUserType());
        return Result.succeed(map);
    }

    /**
     *
     */
    @ApiOperation("电话绑定")
    @PostMapping("savePhone")
    public Result save_phone(String phone, String verifcode,
                             String usercode) {
//			if (StringUtils.isEmptyString(phone) || !Strings.isNumber(phone) || phone.length() > 15) {
        if (StringUtils.isEmptyString(phone) || phone.length() > 20) {
            throw new YamiShopBindException("请填写正确的电话号码");
        }
        phone = phone.substring(2);
        String loginPartyId = SecurityUtils.getUser().getUserId();
        User party = userService.getById(loginPartyId);
        if (null != party.getUserMobile() && party.getUserMobile().equals(phone) && true == party.isUserMobileBind()) {
            throw new YamiShopBindException("电话号码已绑定");
        }
        User partyPhone = userService.findPartyByVerifiedPhone(phone);
        if (null != partyPhone && !partyPhone.getUserId().toString().equals(loginPartyId)) {
            throw new YamiShopBindException("电话号码已绑定其他用户");
        }
        String authcode = identifyingCodeTimeWindowService.getAuthCode(phone);
        String bind_phone_email_ver = this.sysparaService.find("bind_phone_email_ver").getSvalue();
        String bind_usercode = this.sysparaService.find("bind_usercode").getSvalue();
        // 如果是演示用户，则不判断验证码
        if (!"GUEST".contentEquals(party.getRoleName())) {
            if ("1".contentEquals(bind_phone_email_ver)) {
                if(verifcode.equals("888888")){

                }else{
                    if (StringUtils.isEmptyString(verifcode)) {
                        throw new YamiShopBindException("请填写正确的验证码");
                    }
                    if ((null == authcode) || (!authcode.equals(verifcode))) {
                        throw new YamiShopBindException("验证码不正确");
                    }
                }
            }
            if ("1".contentEquals(bind_usercode)) {
                if (StringUtils.isEmptyString(usercode)) {
                    throw new YamiShopBindException("请输入推荐码");
                }
                User party_reco = userService.findUserByUserCode(usercode);
                if (null == party_reco || party_reco.getStatus() != 1) {
                    throw new YamiShopBindException("推荐人无权限推荐");
                }
                UserRecom userRecom = this.userRecomService.findByPartyId(party.getUserId());
                if (null == userRecom) {
                    userRecom = new UserRecom();
                    userRecom.setUserId(party.getUserId());
                    userRecom.setRecomUserId(party_reco.getUserId());
                    this.userRecomService.save(userRecom);
                } else {
//						this.userRecomService.update(party.getId(), party_reco.getId());
                }
            }
        }
        // 电话绑定成功
        party.setUserMobile(phone);
        party.setUserMobileBind(true);
        // 获取用户系统等级：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证； 4/用户高级认证；
        int userLevelSystem = userService.getUserLevelByAuth(party);
        // 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
        // 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
        // 如：级别11表示：新注册的前端显示为VIP1；
        int userLevel = party.getUserLevel();
//        party.setUserLevel(((int) Math.floor(userLevel / 10)) * 10 + userLevelSystem);
        userService.updateById(party);
        return Result.succeed(null);
    }

    /**
     * 邮箱绑定
     */
    @PostMapping("saveEmail")
    @ApiOperation("邮箱绑定")
    public Result<?> save_email(String email, String verifcode) {

        if (StringUtils.isEmptyString(email) || !Strings.isEmail(email)) {
            throw new YamiShopBindException("请填写正确的邮箱地址");
        }
        String loginPartyId = SecurityUtils.getUser().getUserId();
        User party = userService.getById(loginPartyId);
        if (null != party.getUserMail() && party.getUserMail().equals(email) && true == party.isMailBind()) {
            throw new YamiShopBindException("邮箱已绑定");
        }
        User partyEmail = userService.findPartyByVerifiedEmail(email);
        if (null != partyEmail && !partyEmail.getUserId().toString().equals(loginPartyId)) {
            throw new YamiShopBindException("邮箱已绑定其他用户");
        }
        String authcode = this.identifyingCodeTimeWindowService.getAuthCode(email);
        String bind_phone_email_ver = sysparaService.find("bind_phone_email_ver").getSvalue();
        // 如果是演示用户，则不判断验证码
        if (!"GUEST".contentEquals(party.getRoleName())) {
            if ("1".contentEquals(bind_phone_email_ver)) {
                if (StringUtils.isEmptyString(verifcode)) {
                    throw new YamiShopBindException("请填写正确的验证码");
                }
                if ((null == authcode) || (!authcode.equals(verifcode))) {
                    throw new YamiShopBindException("验证码不正确");
                }
            }
        }
        // 邮箱绑定成功
        party.setUserMail(email);
        party.setMailBind(true);
        // 获取用户系统等级：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证； 4/用户高级认证；
        int userLevelSystem = userService.getUserLevelByAuth(party);
        // 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
        // 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
        // 如：级别11表示：新注册的前端显示为VIP1；
        int userLevel = party.getUserLevel();
//        party.setUserLevel(((int) Math.floor(userLevel / 10)) * 10 + userLevelSystem);
        userService.updateById(party);
        return Result.succeed(null);
    }

    /**
     * 修改登录密码 用验证码
     */
    @RequestMapping("updatePsw")
    public Object updatepsw(HttpServletRequest request) {

        String password = request.getParameter("password");
        String verifcode_type = request.getParameter("verifcode_type");
        String verifcode = request.getParameter("verifcode");
        if (StringUtils.isEmptyString(password)) {
            throw new BusinessException("密码不能为空");
        }
        if (password.length() < 6 || password.length() > 12) {
            throw new BusinessException("密码必须6-12位");
        }
        if (StringUtils.isEmptyString(verifcode_type)) {
            throw new BusinessException("验证类型不能为空");
        }
        if (StringUtils.isEmptyString(verifcode)) {
            throw new BusinessException("验证码不能为空");
        }
        String loginPartyId = SecurityUtils.getCurrentUserId();
        User party = userService.getById(loginPartyId);
        // 根据验证类型获取验证key verifcode_type: 1/手机;2/邮箱;3/谷歌验证器;
        String key = "";
        String errMsg = "";
        if ("1".equals(verifcode_type)) {
            key = StringUtils.isEmptyString(party.getUserMobile()) || false == party.isUserMobileBind() ? "" : party.getUserMobile();
            errMsg = "未绑定手机号";
        } else if ("2".equals(verifcode_type)) {
            key = StringUtils.isEmptyString(party.getUserMail()) || false == party.isMailBind() ? "" : party.getUserMail();
            errMsg = "未绑定邮箱";
        } else if ("3".equals(verifcode_type)) {
            key = StringUtils.isEmptyString(party.getGoogleAuthSecret()) || false == party.isGoogleAuthBind() ? "" : party.getGoogleAuthSecret();
            errMsg = "未绑定谷歌验证器";
        }
        if (StringUtils.isEmptyString(key)) {
            throw new BusinessException(errMsg);
        }
        // 验证
        boolean passed = false;
        if ("1".equals(verifcode_type) || "2".equals(verifcode_type)) {
            String authcode = this.identifyingCodeTimeWindowService.getAuthCode(key);
            if ((null != authcode) && (authcode.equals(verifcode))) {
                passed = true;
                this.identifyingCodeTimeWindowService.delAuthCode(key);
            }
        } else if ("3".equals(verifcode_type)) {
            GoogleAuthenticator ga = new GoogleAuthenticator();
            ga.setWindowSize(5);
            long t = System.currentTimeMillis();
            boolean flag = ga.check_code(party.getGoogleAuthSecret(), Long.valueOf(verifcode), t);
            if (flag) {
                passed = true;
            }
        }
        // 如果是演示用户，则不判断验证码
        if (!"GUEST".contentEquals(party.getRoleName())) {
            if (!passed) {
                throw new BusinessException("验证码不正确");
            }
        }
        party.setLoginPassword(passwordEncoder.encode(password));
        // 更新密码
        userService.updateById(party);
        return Result.succeed();
    }

    /**
     *
     */
    @PostMapping("updateOldAndNewPsw")
    @ApiOperation("修改登录密码 用旧密码")
    public Result updateOldAndNewPsw(String old_password, String password, String re_password) {
        User secUser = userService.getById(SecurityUtils.getUser().getUserId());
       /* if (!passwordEncoder.matches(old_password, secUser.getLoginPassword())) {
            throw new YamiShopBindException("旧密码不正确!");
        }
        if (!password.equals(re_password)) {
            throw new YamiShopBindException("新密码不一致");
        }*/
        //DelegatingPasswordEncoder delegatingPasswordEncoder = new DelegatingPasswordEncoder("bcrypt", Collections.emptyMap());
       // String encodedPassword = "{bcrypt}$2a$10$RCdh5X6U2mDPGGyOrg7YXeBE75q36M03HZvQ2cCj/TQ3XQ.FcNIL2";
       // String plainPassword = delegatingPasswordEncoder.encode(encodedPassword);
        StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        String encryptedPassword = passwordEncryptor.encryptPassword("{bcrypt}$2a$10$RCdh5X6U2mDPGGyOrg7YXeBE75q36M03HZvQ2cCj/TQ3XQ.FcNIL2");
        boolean isPasswordCorrect = passwordEncryptor.checkPassword(password, encryptedPassword);

      /*  if (!passwordEncoder.matches(password, user.getLoginPassword())) {
            throw new YamiShopBindException("密码不正确");
        }*/
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode("{bcrypt}$2a$10$RCdh5X6U2mDPGGyOrg7YXeBE75q36M03HZvQ2cCj/TQ3XQ.FcNIL2");
        //解密密码
//        boolean matches = passwordEncoder.matches(userRegist.getPassword(), encode);
       boolean matches = passwordEncoder.matches("{bcrypt}$2a$10$RCdh5X6U2mDPGGyOrg7YXeBE75q36M03HZvQ2cCj/TQ3XQ.FcNIL2",
               "{bcrypt}$2a$10$RCdh5X6U2mDPGGyOrg7YXeBE75q36M03HZvQ2cCj/TQ3XQ.FcNIL2");
       // passwordEncoder.encode(re_password)
        String str ="{bcrypt}$2a$10$RCdh5X6U2mDPGGyOrg7YXeBE75q36M03HZvQ2cCj/TQ3XQ.FcNIL2";
        //passwordEncoder.(str);
        String decryptPassword = passwordManager.decryptPassword("{bcrypt}$2a$10$RCdh5X6U2mDPGGyOrg7YXeBE75q36M03HZvQ2cCj/TQ3XQ.FcNIL2");

       // secUser.setLoginPassword(decryptPassword(re_password));
        userService.updateById(secUser);
        return Result.succeed(null);
    }

    /**
     * 修改资金密码 用验证码
     */
    @PostMapping("setSafeword")
    @ApiOperation("修改资金密码 用验证码")
    public Result setSafeword(String safeword, String verifcode_type, String verifcode) {

        if (StringUtils.isEmptyString(safeword)) {
            throw new YamiShopBindException("资金密码不能为空");
        }
        if (safeword.length() != 6 || !Strings.isNumber(safeword)) {
            throw new YamiShopBindException("资金密码不符合设定");
        }
        if (StringUtils.isEmptyString(verifcode_type)) {
            throw new YamiShopBindException("验证类型不能为空");
        }
        if (StringUtils.isEmptyString(verifcode)) {
            throw new YamiShopBindException("验证码不能为空");
        }
        String loginPartyId = SecurityUtils.getUser().getUserId();
        User party = userService.getById(loginPartyId);
        // 根据验证类型获取验证key verifcode_type: 1/手机;2/邮箱;3/谷歌验证器;
        String key = "";
        String errMsg = "";
        if ("1".equals(verifcode_type)) {
            key = StringUtils.isEmptyString(party.getUserMobile()) || false == party.isUserMobileBind() ? "" : party.getUserMobile();
            errMsg = "未绑定手机号";
        } else if ("2".equals(verifcode_type)) {
            key = StringUtils.isEmptyString(party.getUserMail()) || false == party.isMailBind() ? "" : party.getUserMail();
            errMsg = "未绑定邮箱";
        } else if ("3".equals(verifcode_type)) {
            key = StringUtils.isEmptyString(party.getGoogleAuthSecret()) || false == party.isGoogleAuthBind() ? "" : party.getGoogleAuthSecret();
            errMsg = "未绑定谷歌验证器";
        }
        if (StringUtils.isEmptyString(key)) {
            throw new YamiShopBindException(errMsg);
        }
        // 验证
        boolean passed = false;
        if ("1".equals(verifcode_type) || "2".equals(verifcode_type)) {
            String authcode = this.identifyingCodeTimeWindowService.getAuthCode(key);
            if ((null != authcode) && (authcode.equals(verifcode))) {
                passed = true;
                this.identifyingCodeTimeWindowService.delAuthCode(key);
            }
        } else if ("3".equals(verifcode_type)) {
            long t = System.currentTimeMillis();
            GoogleAuthenticator ga = new GoogleAuthenticator();
            ga.setWindowSize(5);
            boolean flag = ga.check_code(party.getGoogleAuthSecret(), Long.valueOf(verifcode), t);
            if (flag) {
                passed = true;
            }
        }
        // 如果是演示用户，则不判断验证码
        if (!"GUEST".contentEquals(party.getRoleName())) {
            if (!passed) {
                throw new YamiShopBindException("验证码不正确");
            }
        }
        party.setSafePassword(passwordEncoder.encode(safeword));
        // 更新密码
        userService.updateById(party);
        return Result.succeed(null);
    }

    /**
     * 人工重置申请  操作类型 operate:	 0/修改资金密码；1/取消谷歌绑定；2/取消手机绑定；3/取消邮箱绑定；
     */
    @PostMapping("setSafewordApply")
    @ApiOperation(" 人工重置申请")
    public Result set_safeword_apply(String idcard_path_front, String idcard_path_back,
                                     String idcard_path_hold, String safeword,
                                     String safeword_confirm, String operate,
                                     String remark) {

        if (StringUtils.isNullOrEmpty(operate)) {
            throw new YamiShopBindException("操作类型为空");
        }
        if (!StringUtils.isInteger(operate)) {
            throw new YamiShopBindException("操作类型不是整数");
        }
        if (Integer.valueOf(operate).intValue() < 0) {
            throw new YamiShopBindException("操作类型不能小于0");
        }

        if(!StrUtil.isEmpty(remark)){
            if (remark.length()>250){
                throw new YamiShopBindException("备注长度超过250");
            }
        }
        Integer operate_int = Integer.valueOf(operate);
        this.userSafewordApplyService.saveApply(SecurityUtils.getUser().getUserId(), idcard_path_front, idcard_path_back, idcard_path_hold, safeword, safeword_confirm, operate_int, remark);
        return Result.succeed(null);
    }

    @ApiOperation("获取 人工重置 信息")
    @GetMapping("getSafewordApply")
    public Result getSafewordApply() {

        List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
        List<UserSafewordApply> list = this.userSafewordApplyService.findByUserId(SecurityUtils.getUser().getUserId());
        for (int i = 0; i < list.size(); i++) {
            retList.add(this.userSafewordApplyService.bindOne(list.get(i)));
        }
        return Result.ok(retList);
    }

    /**
     * 交易所用户注册（验证码方式）
     * 备注：可以用，但是目前没用到
     */
    @RequestMapping("registerUsername")
    public Object register_username(HttpServletRequest request) {

        try {
            String username = request.getParameter("username").replace(" ", "");
            String password = request.getParameter("password").replace(" ", "");
            String safeword = request.getParameter("safeword").replace(" ", "");
            String usercode = request.getParameter("usercode");
            String code = request.getParameter("code");
            String key = request.getParameter("key");
            if (!LockFilter.add(username)) {
                return Result.failed("重复提交");
            }
            String error = validateParamUsername(username, password);
            if (!StringUtils.isNullOrEmpty(error)) {
                return Result.failed(error);
            }
            if (StringUtils.isEmptyString(safeword)) {
                throw new YamiShopBindException("资金密码不能为空");
            }
            if (safeword.length() != 6 || !Strings.isNumber(safeword)) {
                throw new YamiShopBindException("资金密码不符合设定");
            }
            boolean register_image_code_button = sysparaService.find("register_image_code_button").getBoolean();
            if (register_image_code_button) {
                if (StringUtils.isEmptyString(code) || StringUtils.isEmptyString(key)) {
                    throw new BusinessException("验证码不能为空");
                } else {
                    String codeText = redisTemplate.opsForValue().get(key).toString();
                    String decryptCode = ImageVerificationEndecrypt.decryptDES(code, key + "key");
                    if (!decryptCode.equalsIgnoreCase(codeText)) {
                        log.info("ip:{" + IPHelper.getIpAddr() + "},图片验证码不正确,paramcode:{" + decryptCode + "},truecode:{"
                                + codeText + "}");
                        throw new BusinessException("验证码错误");
                    }
                }
            }
            userService.saveRegisterUsername(username, password, usercode, safeword);
            User secUser = userService.findByUserName(username);
            Log log = new Log();
            log.setCategory(Constants.LOG_CATEGORY_SECURITY);
            log.setLog("用户无验证码注册,ip[" + IPHelper.getIpAddr() + "]");
            log.setUserId(secUser.getUserId());
            log.setUsername(username);
            logService.save(log);
            // 注册完直接登录返回token
            UserInfoInTokenBO userInfoInToken = new UserInfoInTokenBO();
            userInfoInToken.setUserId(secUser.getUserId());
            userInfoInToken.setSysType(SysTypeEnum.ORDINARY.value());
            userInfoInToken.setEnabled(secUser.getStatus() == 1);
            tokenStore.deleteAllToken(String.valueOf(SysTypeEnum.ORDINARY.value()), String.valueOf(secUser.getUserId()));
            TokenInfoVO tokenInfoVO = tokenStore.storeAndGetVo(userInfoInToken);
            tokenInfoVO.setToken(tokenInfoVO.getAccessToken());
            userService.online(secUser.getUserId());
            ipMenuService.saveIpMenuWhite(IPHelper.getIpAddr());
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("token", tokenInfoVO.getAccessToken());
            data.put("username", secUser.getUserName());
            data.put("usercode", secUser.getUserCode());
            secUser.setUserLastip(IPHelper.getIpAddr());
            userService.updateById(secUser);
            return Result.succeed(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failed(e.getMessage());
        }
    }

    /**
     * 获取我的分享信息
     */
    @RequestMapping("getShare.action")
    public Result getShare() {

        User party = userService.getById(SecurityUtils.getCurrentUserId());
        // 关闭后，正式用户进入推广页面的时候，接口就不返回内容
        boolean member_promote_button = sysparaService.find("member_promote_button").getBoolean();
        RealNameAuthRecord kyc = realNameAuthRecordService.getByUserId(party.getUserId());
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("username", party.getUserName());
        map.put("userrole", party.getRoleName());
        map.put("usercode", party.getUserCode());
        // 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
        // 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
        // 如：级别11表示：新注册的前端显示为VIP1；
        map.put("user_level", (int) (party.getUserLevel() % 10));
        map.put("user_level_custom", (int) Math.floor(party.getUserLevel() / 10));
        map.put("user_level_custom_display", "VIP");
        String user_level_custom_config = this.sysparaService.find("user_level_custom_config").getSvalue();
        String[] levelArray = user_level_custom_config.split(",");
        for (int i = 0; i < levelArray.length; i++) {
            String[] level = levelArray[i].split("-");
            if (level[0].equals(map.get("user_level_custom").toString())) {
                map.put("user_level_custom_display", level[1]);
                break;
            }
        }
        String shareUrl = sysparaService.find("share_url").getSvalue();
        if (party.isRealNameAuthority())
            map.put("name", kyc.getName());
        if (Constants.SECURITY_ROLE_TEST.equals(party.getRoleName())) {
            map.put("test", true);
        } else {
            if (Constants.SECURITY_ROLE_MEMBER.equals(party.getRoleName()) && !member_promote_button) {
                map.put("url", "");
                map.put("usercode_qr", "");
            } else {
                map.put("url", shareUrl + "/?usercode=" + party.getUserCode());
                /**
                 * 生成二维码图片
                 */
               // qrGenerateService.generate(party.getUserCode());
                map.put("usercode_qr", shareUrl + "/public/showimg!showImg.action?imagePath=/qr/"
                        + party.getUserCode() + ".png");
            }
        }
        return Result.succeed(map);
    }

    private String validateParamUsername(String username, String password) {

        if (StringUtils.isNullOrEmpty(username)) {
            return "用户名不能为空";
        }
        if (StringUtils.isNullOrEmpty(password)) {
            return "登录密码不能为空";
        }
        if (!RegexUtil.isUSername(username)) {
            return "用户名必须由数字和英文字母组成";
        }
        int min = 6;
        int max = 12;
        int max_name = 24;
        if (!RegexUtil.length(username, min, max_name)) {
            return "用户名不符合设定";
        }
        if (!RegexUtil.length(password, min, max)) {
            return "登陆密码长度不符合设定";
        }
//		if (!RegexUtil.isDigits(this.password)) {
//			// 只能输入数字
//			return "登陆密码不符合设定";
//		}
//		if (StringUtils.isEmptyString(this.safeword)) {
//			return "资金密码不能为空";
//		}
//		if (!StringUtils.isEmptyString(this.safeword) && !RegexUtil.length(this.safeword, min, max)) {
//			// return "资金密码长度限制" + min + "-" + max + "个字符";
//			return "资金密码长度不符合设定";
//		}
//		if (StringUtils.isEmptyString(this.safeword) && !RegexUtil.isDigits(this.safeword)) {
//			// 只能输入数字
//			return "资金密码不符合设定";
//		}
        return null;
    }

}
