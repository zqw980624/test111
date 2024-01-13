package com.yami.trading.security.common.filter;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.yami.trading.security.common.adapter.AuthConfigAdapter;
import com.yami.trading.security.common.manager.TokenStore;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.handler.HttpHandler;
import com.yami.trading.security.common.bo.UserInfoInTokenBO;
import com.yami.trading.security.common.util.AuthUserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 授权过滤，只要实现AuthConfigAdapter接口，添加对应路径即可：
 *
 * @author 菠萝凤梨
 * @date 2022/3/25 17:33
 */
@Component
public class AuthFilter implements Filter {

    @Autowired
    private AuthConfigAdapter authConfigAdapter;

    @Autowired
    private HttpHandler httpHandler;

    @Autowired
    private TokenStore tokenStore;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String requestUri = req.getRequestURI();

        List<String> excludePathPatterns = authConfigAdapter.excludePathPatterns();

        AntPathMatcher pathMatcher = new AntPathMatcher();
        // 如果匹配不需要授权的路径，就不需要校验是否需要授权
        if (CollectionUtil.isNotEmpty(excludePathPatterns)) {
            for (String excludePathPattern : excludePathPatterns) {
                if (pathMatcher.match(excludePathPattern, requestUri)) {
                    chain.doFilter(req, resp);
                    return;
                }
            }
        }

        String token = req.getHeader("token");
        String accessToken = req.getHeader("Authorization");


        if (StrUtil.isEmpty(accessToken)) {
            accessToken = token;
        }
        if (StrUtil.isEmpty(accessToken)) {
            accessToken = req.getParameter("token");
        }
        // 也许需要登录，不登陆也能用的uri
        List<String> maybeAuthUris = authConfigAdapter.maybeAuthUri();
        boolean mayAuth = false;
        if (CollectionUtil.isNotEmpty(maybeAuthUris)) {
            for (String maybe : maybeAuthUris) {
                if (pathMatcher.match(maybe, requestUri)) {
                    mayAuth = true;
                    break;
                }
            }
        }


        UserInfoInTokenBO userInfoInToken = null;

        try {
            // 如果有token，就要获取token
            if (StrUtil.isNotBlank(accessToken)) {
                userInfoInToken = tokenStore.getUserInfoByAccessToken(accessToken, true);
            } else if (!mayAuth) {
                // 返回前端401
                httpHandler.printServerResponseToWeb(HttpStatus.UNAUTHORIZED.getReasonPhrase(), HttpStatus.UNAUTHORIZED.value());
                return;
            }
            // 保存上下文
            AuthUserContext.set(userInfoInToken);

            chain.doFilter(req, resp);

        } catch (Exception e) {
            // 手动捕获下非controller异常
            if (e instanceof YamiShopBindException) {
                httpHandler.printServerResponseToWeb(e.getMessage(), 200);
            } else {
                throw e;
            }
        } finally {
            AuthUserContext.clean();
        }
    }
}
