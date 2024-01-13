package com.yami.trading.api.controller;

import cn.hutool.core.util.StrUtil;
import com.yami.trading.api.model.RegisterModel;
import com.yami.trading.api.model.SendEmailModel;
import com.yami.trading.api.model.UserLoginModel;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.constants.RedisKeyConstants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.manager.email.EmailManager;
import com.yami.trading.common.manager.email.EmailMessage;
import com.yami.trading.common.util.IPHelper;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.security.common.bo.UserInfoInTokenBO;
import com.yami.trading.security.common.enums.SysTypeEnum;
import com.yami.trading.security.common.manager.PasswordCheckManager;
import com.yami.trading.security.common.manager.PasswordManager;
import com.yami.trading.security.common.manager.TokenStore;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.security.common.vo.TokenInfoVO;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.user.UserDataService;
import com.yami.trading.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @date 2022/3/28 15:20
 */
@RestController
@RequestMapping("api/")
@Api(tags = "非登录接口")
public class ApiIndexController {
    @Autowired
    private TokenStore tokenStore;
    @Autowired
    private PasswordCheckManager passwordCheckManager;
    @Autowired
    private PasswordManager passwordManager;
    @Autowired
    UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private  RedisTemplate<String, String> redisTemplate;

    @Autowired
    SysparaService sysparaService;

    @Autowired
    UserDataService userDataService;

    @PostMapping("/login")
    @ApiOperation(value = "账号密码(用于前端登录)", notes = "通过账号/手机号/用户名密码登录，还要携带用户的类型，也就是用户所在的系统")
    public Result<TokenInfoVO> login(@Valid  UserLoginModel model) {
        String mobileOrUserName = model.getUserName();
        User user=null;
       /* if (model.getType()==1){
            user= userService.findByUserMobile(mobileOrUserName.substring(2));
        }
        if (model.getType()==2){
            user= userService.findByEmail(mobileOrUserName);
        }
        if (model.getType()==3){
            user= userService.findByUserName(mobileOrUserName);
        }
        if (user==null){
            throw new YamiShopBindException("账号或密码不正确");
        }*/
        user= userService.findByUserMobile(mobileOrUserName.substring(2));
        if (user==null){
            throw new YamiShopBindException("账号或密码不正确");
        }
        // 半小时内密码输入错误十次，已限制登录30分钟
        passwordCheckManager.checkPassword(SysTypeEnum.ORDINARY, model.getUserName(), model.getPassWord(), user.getLoginPassword());
        UserInfoInTokenBO userInfoInToken = new UserInfoInTokenBO();
        userInfoInToken.setUserId(user.getUserId());
        userInfoInToken.setSysType(SysTypeEnum.ORDINARY.value());
        userInfoInToken.setEnabled(user.getStatus() == 1);
        user.setUserLastip(IPHelper.getIpAddr());
        user.setUserLasttime(new Date());
        userService.online(user.getUserId());
        userService.updateById(user);
        tokenStore.deleteAllToken(String.valueOf(SysTypeEnum.ORDINARY.value()), String.valueOf(user.getUserId()));
        // 存储token返回vo
        TokenInfoVO tokenInfoVO = tokenStore.storeAndGetVo(userInfoInToken);
        tokenInfoVO.setToken(tokenInfoVO.getAccessToken());

        return Result.succeed(tokenInfoVO);
    }

    @PostMapping("/registerNoVerifcode")
    @ApiOperation(value = "手机/邮箱/用户名注册（无验证码）")
    public Result register(@Valid RegisterModel model) {
        // 注册类型：1/手机；2/邮箱；3/用户名；
        int type = model.getType();
        User user =null;
      /*  if(type==1){//手机
             user = userService.registerMobile(model.getUserName(),
                    passwordEncoder.encode(model.getPassword())
                    , model.getUserCode(),false);
        }else if(type==2){//邮箱
             user = userService.registerMail(model.getUserName(),
                    passwordEncoder.encode(model.getPassword())
                    , model.getUserCode(),false);
        }*/
        user = userService.registerMobile(model.getUserName(),
                passwordEncoder.encode(model.getPassword())
                , model.getUserCode(),false);

        UserInfoInTokenBO userInfoInToken = new UserInfoInTokenBO();
        userInfoInToken.setUserId(user.getUserId());
        userService.online(user.getUserId());
        userInfoInToken.setSysType(SysTypeEnum.ORDINARY.value());
        userInfoInToken.setEnabled(user.getStatus() == 1);
        userDataService.saveRegister(user.getUserId());
        tokenStore.deleteAllToken(String.valueOf(SysTypeEnum.ORDINARY.value()), String.valueOf(user.getUserId()));

        // 存储token返回vo
        TokenInfoVO tokenInfoVO = tokenStore.storeAndGetVo(userInfoInToken);
        tokenInfoVO.setToken(tokenInfoVO.getAccessToken());

        return Result.succeed(tokenInfoVO);
    }

    @PostMapping("/sendEmails")
   @ApiOperation(value = "发送邮箱")
    public ResponseEntity<?> sendEmail(@RequestBody @Valid SendEmailModel model){
        String  code=  redisTemplate.opsForValue().get(RedisKeyConstants.USER_EMAILL_PREFIX+"w253047823@gmail.com");
        if (!StrUtil.isEmpty(code)){
           throw new YamiShopBindException("发送yo频繁,请稍后在试!");
        }

        String sendCodeText =sysparaService.find("send_code_text").getSvalue();
        if (StringUtils.isNullOrEmpty(sendCodeText)) {
            throw  new YamiShopBindException("send_code_text 未配置");
       }
        Random random = new Random();
         code = String.valueOf(random.nextInt(999999) % 900000 + 100000);
        sendCodeText= MessageFormat.format(sendCodeText, new Object[] { code });
        String content = MessageFormat.format("code is ：{0}", new Object[] { code });
        EmailMessage emailMessage=new EmailMessage();
        emailMessage.setTomail("w253047823@gmail.com");
        emailMessage.setSubject(sendCodeText);
        emailMessage.setContent(content);
        EmailManager ee=new EmailManager();
        ee.send(emailMessage);
        redisTemplate.opsForValue().set( RedisKeyConstants.USER_EMAILL_PREFIX+model.getEmail(),code,60, TimeUnit.SECONDS);
        return ResponseEntity.ok(null);
    }


}
