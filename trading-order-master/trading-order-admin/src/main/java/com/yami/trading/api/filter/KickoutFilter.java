/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */

package com.yami.trading.api.filter;

import cn.hutool.core.util.StrUtil;
import com.yami.trading.bean.syspara.domain.Syspara;
import com.yami.trading.common.enums.YamiHttpStatus;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.IPHelper;
import com.yami.trading.common.util.IpUtil;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.security.common.manager.TokenStore;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * 一些简单的安全过滤：
 * xss
 * @author lgh
 */
@Component
public class KickoutFilter implements Filter {
    Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    SysparaService sysparaService;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    UserService userService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException{
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String ip = IPHelper.getIpAddr();
        if (IpUtil.isCorrectIpRegular(ip)) {
            // 黑名单限制
            Syspara syspara = sysparaService.find("blacklist_ip");
            String blackUsers = syspara.getSvalue();
            if(org.apache.commons.lang3.StringUtils.isNotEmpty(blackUsers)) {
                String[] ips = blackUsers.split(",");

                if(Arrays.asList(ips).contains(ip.trim())){
                    String token = req.getHeader("token");
                    if (!StrUtil.isBlank(token)) {
                        userService.logout(SecurityUtils.getUser().getUserId());
                        // 删除该用户在该系统当前的token
                        tokenStore.deleteCurrentToken(token);
                        throw new YamiShopBindException(403,"accessToken 已过期");
                    }
                }
            }
        }

    //    logger.info("uri:{}",req.getRequestURI());
        // xss 过滤
		chain.doFilter(req, resp);
    }

    @Override
    public void destroy() {

    }
}
