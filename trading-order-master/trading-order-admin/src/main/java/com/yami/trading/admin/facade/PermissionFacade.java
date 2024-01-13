package com.yami.trading.admin.facade;

import com.yami.trading.bean.model.User;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.user.UserRecomService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.sys.model.SysRole;
import com.yami.trading.sys.service.SysRoleService;
import com.yami.trading.sys.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PermissionFacade {
    @Autowired
    private UserRecomService userRecomService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    SysRoleService sysRoleService;
    @Autowired
    UserService userService;

    public boolean checkAgent() {
        List<Long> roleIds = sysRoleService.listRoleIdByUserId(SecurityUtils.getSysUser().getUserId());
        Map<Long, SysRole> sysRoleMap = sysRoleService.list().stream().collect(Collectors.toMap(SysRole::getRoleId, SysRole -> SysRole));
        List<String> roleNames = new ArrayList<>();
        boolean isAgent = false;
        for (Long id : roleIds) {
            if (sysRoleMap.containsKey(id)) {
                sysRoleMap.get(id).getRoleName().equals("代理商");
                isAgent = true;
            }
        }
        return isAgent;
    }

    /**
     * 可以看到的前端用户的数据，如果没有，传入-,null表示所有所的权限
     *
     * @return
     */
    public List<String> getOwnerUserIds() {
        String userName = SecurityUtils.getSysUser().getUsername();
        User user = userService.findByUserName(userName);
        if (userName.equals("admin")){
            return null;
        }
        if(user!=null){
            List<String> checked_list = userRecomService.
                    findChildren(user.getUserId());
            if (checkAgent()) {
                if (checked_list.size() > 0) {
                    return checked_list;
                } else {
                    checked_list.add(userName);
                    return checked_list;
                }
            } else {
                return null;
            }
        }else{
            return null;
        }
//        if (CollectionUtil.isNotEmpty(roleNames) && roleNames.contains(Constants.SECURITY_ROLE_AGENT)) {
//            List<String> children = this.userRecomService.findChildren(userId.toString());
//            if (CollectionUtil.isEmpty(children)) {
//                children = Lists.newArrayList("-");
//            }
//            return children;
//        } else {
//            return null;
//        }
    }
}
