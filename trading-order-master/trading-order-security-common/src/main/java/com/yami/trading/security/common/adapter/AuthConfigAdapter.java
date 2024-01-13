package com.yami.trading.security.common.adapter;

import java.util.List;

/**
 * 授权过滤器的配置 接口
 * @author admin
 */
public interface AuthConfigAdapter {

    /**
     * 需要授权登陆的路径
     * @return
     */
    List<String> pathPatterns();

    /**
     * 不需要授权的路径
     * @return
     */
    List<String> excludePathPatterns();

    /**
     * 不需要登录 可访问的接口
     * @return
     */
    List<String> maybeAuthUri();
}
