package com.yami.trading.common.lang;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class HttpContextUtil {

    public static HttpServletRequest getHttpServletRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(requestAttributes == null){
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }
}