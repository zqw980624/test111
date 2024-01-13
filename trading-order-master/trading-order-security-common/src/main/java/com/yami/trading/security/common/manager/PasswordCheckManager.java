/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.trading.security.common.manager;

import cn.hutool.core.util.StrUtil;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.IPHelper;
import com.yami.trading.common.util.RedisUtil;
import com.yami.trading.security.common.enums.SysTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @date 2022/3/25 17:33
 * @author lh
 */
@Component
public class PasswordCheckManager {


    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 半小时内最多错误10次
     */
    private static final int TIMES_CHECK_INPUT_PASSWORD_NUM = 10;

    /**
     * 检查用户输入错误的验证码次数
     */
    private static final String CHECK_VALID_CODE_NUM_PREFIX = "checkUserInputErrorPassword_";
   /* public void checkPassword(SysTypeEnum sysTypeEnum,String userNameOrMobile, String rawPassword, String encodedPassword) {

        String checkPrefix = sysTypeEnum.value() + CHECK_VALID_CODE_NUM_PREFIX + IPHelper.getIpAddr();

        int count = 0;
        if(RedisUtil.hasKey(checkPrefix + userNameOrMobile)){
            count = RedisUtil.get(checkPrefix + userNameOrMobile);
        }
        if(count > TIMES_CHECK_INPUT_PASSWORD_NUM){
            throw new YamiShopBindException("密码输入错误十次，已限制登录30分钟");
        }
        // 半小时后失效
        RedisUtil.set(checkPrefix + userNameOrMobile,count,1800);
        // 密码不正确
        String encode = passwordEncoder.encode("123456");
//        if (StrUtil.isBlank(encodedPassword) || !passwordEncoder.matches(rawPassword,encodedPassword)){
//            count++;
//            // 半小时后失效
//            RedisUtil.set(checkPrefix + userNameOrMobile,count,1800);
//            throw new YamiShopBindException("账号或密码不正确");
//        }
    }*/
   public void checkPassword(SysTypeEnum sysTypeEnum,String userNameOrMobile, String rawPassword, String encodedPassword) {

       String checkPrefix = sysTypeEnum.value() + CHECK_VALID_CODE_NUM_PREFIX + IPHelper.getIpAddr();

       int count = 0;
       if(RedisUtil.hasKey(checkPrefix + userNameOrMobile)){
           count = RedisUtil.get(checkPrefix + userNameOrMobile);
       }
       if(count > TIMES_CHECK_INPUT_PASSWORD_NUM){
           throw new YamiShopBindException("密码输入错误十次，已限制登录30分钟");
       }
       // 半小时后失效
       RedisUtil.set(checkPrefix + userNameOrMobile,count,1800);
       // 密码不正确
       if (StrUtil.isBlank(encodedPassword) || !passwordEncoder.matches(rawPassword,encodedPassword)){
           count++;
           // 半小时后失效
           RedisUtil.set(checkPrefix + userNameOrMobile,count,1800);
           throw new YamiShopBindException("账号或密码不正确");
       }
   }
}
