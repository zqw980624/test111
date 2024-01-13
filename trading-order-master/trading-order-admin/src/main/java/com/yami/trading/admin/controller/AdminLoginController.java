package com.yami.trading.admin.controller;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yami.trading.admin.dto.GoogleAuthDto;
import com.yami.trading.admin.model.ChangeLoginPasswordModel;
import com.yami.trading.admin.model.ChangeSafewordModel;
import com.yami.trading.admin.model.CheckSafeWordModel;
import com.yami.trading.admin.model.LoginModel;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.annotation.SysLog;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.GoogleAuthenticator;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.security.common.bo.UserInfoInTokenBO;
import com.yami.trading.security.common.enums.SysTypeEnum;
import com.yami.trading.security.common.manager.PasswordCheckManager;
import com.yami.trading.security.common.manager.PasswordManager;
import com.yami.trading.security.common.manager.TokenStore;
import com.yami.trading.security.common.vo.TokenInfoVO;
import com.yami.trading.service.user.UserService;
import com.yami.trading.sys.constant.Constant;
import com.yami.trading.sys.model.SysMenu;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.service.SysMenuService;
import com.yami.trading.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author FrozenWatermelon
 * @date 2020/6/30
 */
@RestController
@Api(tags = "登录")
public class AdminLoginController {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private TokenStore tokenStore;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysMenuService sysMenuService;
    @Autowired
    private PasswordCheckManager passwordCheckManager;
    @Autowired
    private PasswordManager passwordManager;
    @Autowired
    private UserService userService;

    @PostMapping("/adminLogin")
    @ApiOperation(value = "账号密码 + 验证码登录(用于后台登录)", notes = "通过账号/手机号/用户名密码登录")
    public Result<?> login(@Valid @RequestBody LoginModel loginModel) {

        SysUser sysUser = sysUserService.getByUserName(loginModel.getUserName());
        if (sysUser == null) {
            throw new YamiShopBindException("账号或密码不正确");
        }
        GoogleAuthenticator ga = new GoogleAuthenticator();
        ga.setWindowSize(5);
        // 半小时内密码输入错误十次，已限制登录30分钟
        String decryptPassword = passwordManager.decryptPassword(loginModel.getPassWord());


        passwordCheckManager.checkPassword(SysTypeEnum.ADMIN, loginModel.getUserName(), decryptPassword, sysUser.getPassword());
        // 不是店铺超级管理员，并且是禁用状态，无法登录
        if (Objects.equals(sysUser.getStatus(), 0)) {
            // 未找到此用户信息
            throw new YamiShopBindException("未找到此用户信息");
        }
        UserInfoInTokenBO userInfoInToken = new UserInfoInTokenBO();
        userInfoInToken.setUserId(String.valueOf(sysUser.getUserId()));
        userInfoInToken.setSysType(SysTypeEnum.ADMIN.value());
        userInfoInToken.setEnabled(sysUser.getStatus() == 1);
        userInfoInToken.setPerms(getUserPermissions(sysUser.getUserId()));
        userInfoInToken.setNickName(sysUser.getUsername());
        userInfoInToken.setShopId(sysUser.getShopId());
        tokenStore.deleteAllToken(String.valueOf(SysTypeEnum.ADMIN.value()), String.valueOf(sysUser.getUserId()));
        // 存储token返回vo
        TokenInfoVO tokenInfoVO = tokenStore.storeAndGetVo(userInfoInToken);
        return Result.ok(tokenInfoVO);
    }

    @GetMapping("getLoginGoogleAuthSecret")
    @ApiOperation("获取谷歌验证码密钥")
    public Result<GoogleAuthDto> getLoginGoogleAuthSecret(@RequestParam(required = false) String id,@RequestParam(required = false) String name) {
        String secretKey = GoogleAuthenticator.generateSecretKey();
        QrConfig config = new QrConfig(345, 345);
        config.setMargin(3);
        String userName= SecurityUtils.getSysUser().getUsername();
        if(StringUtils.isNotEmpty(id)) {
            User user = userService.getById(id);
            if(null != user) {
                userName = user.getUserName();
            }
        } else if(StringUtils.isNotEmpty(name)){
            userName = name;
        }
        String content = String.format("otpauth://totp/%s?secret=%s", userName,secretKey);
        String base64 = QrCodeUtil.generateAsBase64(content, config, "png");
        GoogleAuthDto dto = new GoogleAuthDto();
        dto.setGoogleAuthImg(base64);
        dto.setGoogleAuthSecret(secretKey);
        return Result.ok(dto);
    }

//    @PostMapping("/bind")
//    @ApiOperation(value = "谷歌身份绑定")
//    @SysLog("谷歌身份绑定")
//    public ResponseEntity bind(@Valid @RequestBody GoogleAuthBindModel model) {
//        Long userId = SecurityUtils.getSysUser().getUserId();
//        long t = System.currentTimeMillis();
//        GoogleAuthenticator ga = new GoogleAuthenticator();
//        ga.setWindowSize(5);
//        boolean flag = ga.check_code(model.getSecret(), Long.valueOf(model.getCode()), t);
//        if (flag) {
//            SysUser user = sysUserService.getById(userId);
//            if (user.isGoogleAuthBind()) {
//                throw new YamiShopBindException("谷歌验证码已绑定!");
//            }
//            user.setGoogleAuthBind(true);
//            user.setGoogleAuthSecret(model.getSecret());
//            user.setUpdateTime(new Date());
//            sysUserService.updateById(user);
//        } else {
//            throw new YamiShopBindException("谷歌验证码错误!");
//        }
//        return ResponseEntity.ok(null);
//    }
//
//    @PostMapping("/unbinding")
//    @ApiOperation(value = "谷歌身份解绑")
//    @SysLog("谷歌身份解绑")
//    public ResponseEntity unbinding(@Valid @RequestBody GooleAuthUnbindingModel model) {
//        model.setSafeWord( passwordManager.decryptPassword(model.getSafeWord()));
//        SysUser sysUser= sysUserService.getById(SecurityUtils.getSysUser().getUserId());
//        if (!passwordEncoder.matches(model.getSafeWord(), sysUser.getSafePassword())) {
//            throw new YamiShopBindException("资金密码不正确!");
//        }
//        if (!sysUser.isGoogleAuthBind()){
//            throw new YamiShopBindException("谷歌验证码未绑定!");
//        }
//        long t = System.currentTimeMillis();
//        GoogleAuthenticator ga = new GoogleAuthenticator();
//        ga.setWindowSize(5);
//        boolean flag = ga.check_code(sysUser.getGoogleAuthSecret(),model.getGooleAuthCode(),t);
//       if (!flag){
//           throw new YamiShopBindException("谷歌验证码不正确!");
//       }
//        sysUser.setGoogleAuthSecret("");
//        sysUser.setGoogleAuthBind(false);
//        sysUserService.updateById(sysUser);
//        return ResponseEntity.ok(null);
//    }


    @PostMapping("changeLoginPassword")
    @ApiOperation("修改登录密码")
    @SysLog("修改登录密码")
    public Result<?> changeLoginPassword(@RequestBody @Valid ChangeLoginPasswordModel model) {
        //model.setOldPassword(passwordManager.decryptPassword(model.getOldPassword()));
        //model.setNewPassword(passwordManager.decryptPassword(model.getNewPassword()));
        passwordManager.decryptPassword(model.getNewPassword());
        //passwordManager.decryptPassword(passwordManager.decryptPassword(model.getNewPassword()));

        Long userId = SecurityUtils.getSysUser().getUserId();
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new YamiShopBindException("用户不存在!");
        }
        if (!passwordEncoder.matches(model.getOldPassword(), user.getPassword())) {
            throw new YamiShopBindException("旧密码不正确!");
        }
        user.setPassword(passwordEncoder.encode(model.getNewPassword()));
        sysUserService.updateById(user);
        return Result.ok(null);
    }

    @PostMapping("checkLoginSafePassword")
    @ApiOperation("检查登录人资金密码")
    public Result<?> checkLoginSafePassword(@RequestBody @Valid CheckSafeWordModel  model) {
        sysUserService.checkSafeWord(model.getLoginSafeword());
        return Result.ok(null);
    }


    @PostMapping("changeSafeword")
    @ApiOperation("修改资金密码")
    @SysLog("修改资金密码")
    public Result<?> changeSafeword(@RequestBody @Valid ChangeSafewordModel model) {
        Long adminUserId = SecurityUtils.getSysUser().getUserId();
        SysUser user = sysUserService.getSysUserById(adminUserId);
        model.setOldSafeword(passwordManager.decryptPassword(model.getOldSafeword()));
        model.setNewSafeword(passwordManager.decryptPassword(model.getNewSafeword()));
        if (!passwordEncoder.matches(model.getOldSafeword(), user.getSafePassword())) {
            throw new YamiShopBindException("资金密码不正确!");
        }
        user.setSafePassword(passwordEncoder.encode(model.getNewSafeword()));
        sysUserService.updateById(user);
        return Result.ok(null);
    }

    private Set<String> getUserPermissions(Long userId) {
        List<String> permsList;
        //系统管理员，拥有最高权限
        if (userId == Constant.SUPER_ADMIN_ID) {
            List<SysMenu> menuList = sysMenuService.list(Wrappers.emptyWrapper());
            permsList = menuList.stream().map(SysMenu::getPerms).collect(Collectors.toList());
        } else {
            permsList = sysUserService.queryAllPerms(userId);
        }
        return permsList.stream().flatMap((perms) -> {
                    if (StrUtil.isBlank(perms)) {
                        return null;
                    }
                    return Arrays.stream(perms.trim().split(StrUtil.COMMA));
                }
        ).collect(Collectors.toSet());
    }
}
