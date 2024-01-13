package com.yami.trading.common.constants;

public interface RedisKeyConstants {
    String TRADING_PREFIX = "trading:";
    /**
     * 邮箱验证码
     */
    String USER_EMAILL_PREFIX=TRADING_PREFIX+":email_code:";
    /**
     * 手机号验证码
     */
    String USER_MOBILE_PREFIX=TRADING_PREFIX+":mobile_code:";
}
