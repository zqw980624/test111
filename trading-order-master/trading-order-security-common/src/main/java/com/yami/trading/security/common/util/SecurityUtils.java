package com.yami.trading.security.common.util;

import com.yami.trading.common.domain.YamiUser;
import com.yami.trading.security.common.bo.UserInfoInTokenBO;
import com.yami.trading.security.common.model.YamiSysUser;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class SecurityUtils {
    /**
     * 获取用户
     */
    public YamiSysUser getSysUser() {
        UserInfoInTokenBO userInfoInTokenBO = AuthUserContext.get();
        if(userInfoInTokenBO == null){
            return null;
        }
        YamiSysUser details = new YamiSysUser();
        String userId = userInfoInTokenBO.getUserId();
        // 兼容swagger 请求情况
        if(StringUtils.isEmpty(userId)){
            return null;
        }
        details.setUserId(Long.valueOf(userId));
        details.setEnabled(userInfoInTokenBO.getEnabled());
        details.setUsername(userInfoInTokenBO.getNickName());
        details.setAuthorities(userInfoInTokenBO.getPerms());
        details.setShopId(userInfoInTokenBO.getShopId());
        return details;
    }

    /**
     * 获取用户
     */
    public YamiUser getUser() {
//        if (!HttpContextUtils.getHttpServletRequest().getRequestURI().startsWith(USER_REQUEST)) {
//            // 用户相关的请求，应该以/p开头！！！
//            throw new RuntimeException("yami.user.request.error");
//        }
        UserInfoInTokenBO userInfoInTokenBO = AuthUserContext.get();
        YamiUser yamiUser = new YamiUser();
        if(userInfoInTokenBO == null){
            yamiUser.setUserId(null);
        }else{
            yamiUser.setUserId(userInfoInTokenBO.getUserId());

        }
        return yamiUser;
    }
    /**
     * 获取当前用户id，没登录时候返回null
     * @return
     */
    public String getCurrentUserId(){
        UserInfoInTokenBO userInfoInTokenBO = AuthUserContext.get();
        if(userInfoInTokenBO == null){
            return null;
        }
        return getUser().getUserId();
    }

    /**
     * 获取当前用户id，没登录时候返回null
     * @return
     */
    public String getCurrentSysUserId(){
        UserInfoInTokenBO userInfoInTokenBO = AuthUserContext.get();
        if(userInfoInTokenBO == null){
            return null;
        }
        return getSysUser().getUserId().toString();
    }
}

