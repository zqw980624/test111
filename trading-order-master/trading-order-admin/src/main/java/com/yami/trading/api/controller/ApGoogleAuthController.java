package com.yami.trading.api.controller;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.yami.trading.api.dto.GoogleAuthDto;
import com.yami.trading.api.model.GoogleAuthBindModel;
import com.yami.trading.api.service.UserCacheService;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.GoogleAuthenticator;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/gooleAuth")
@Api(tags = "谷歌验证码")
public class ApGoogleAuthController {
    @Autowired
    UserCacheService userCacheService;
    @Autowired
    UserService userService;
    @Autowired
    SysparaService sysparaService;

    @GetMapping("/get")
    @ApiOperation(value = "谷歌身份验证器 获取密钥及二维码")
    public Result<GoogleAuthDto> get() {
        String secretKey = GoogleAuthenticator.generateSecretKey();
        QrConfig config = new QrConfig(345, 345);
        config.setMargin(3);
        String host = sysparaService.find("google_auth_host").getSvalue();
        User user = userService.getById(SecurityUtils.getCurrentUserId());
        String content = String.format("otpauth://totp/%s@%s?secret=%s", user.getUserName(), host, secretKey);
        String base64 = QrCodeUtil.generateAsBase64(content, config, "png");
        GoogleAuthDto dto = new GoogleAuthDto();
        dto.setGoogleAuthImg(base64);
        dto.setGoogleAuthSecret(secretKey);
        return Result.succeed(dto);
    }

    @PostMapping("/bind")
    @ApiOperation(value = "谷歌身份绑定")
    public Result bind(@Valid GoogleAuthBindModel model) {
        String userId = SecurityUtils.getUser().getUserId();
        long t = System.currentTimeMillis();
       // GoogleAuthenticator ga = new GoogleAuthenticator();
       // ga.setWindowSize(5);
        //boolean flag = ga.check_code(model.getSecret(), Long.valueOf(model.getCode()), t);
        //if (flag) {
            User user = userService.getById(userId);
           /* if (user.isGoogleAuthBind()) {
                throw new YamiShopBindException("谷歌验证码已绑定");
            }*/
            user.setGoogleAuthBind(true);
            //user.setGoogleAuthSecret(model.getSecret());
            user.setUpdateTime(new Date());
            //int userLevel = userService.getUserLevelByAuth(user);
//            user.setUserLevel(((int) Math.floor(user.getUserLevel() / 10)) * 10 + userLevel);
            userCacheService.updateUser(user);
            Map<String,Object> map=new HashMap<>();
            map.put("google_auth_bind",true);
            return Result.succeed(map);
      //  } else {
         //   throw new YamiShopBindException("谷歌验证码错误");
      //  }
    }
}
