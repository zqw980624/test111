package com.yami.trading.admin.controller.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yami.trading.admin.controller.service.SysUserOperService;
import com.yami.trading.bean.model.Agent;
import com.yami.trading.bean.model.Log;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.IPHelper;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.agent.AgentService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.service.user.UserRecomService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.sys.model.SysRole;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.service.SysRoleService;
import com.yami.trading.sys.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysUserOperServiceImpl implements SysUserOperService {
    @Autowired
    SysUserService sysUserService;
    @Autowired
    AgentService agentService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    SysRoleService roleService;
    @Autowired
    UserService userService;
    @Autowired
    LogService logService;
    @Autowired
    UserRecomService userRecomService;
    @Autowired
    SysRoleService sysRoleService;

    @Override
    public List<String> getListUser(String sysUserId) {
        String userName = SecurityUtils.getSysUser().getUsername();
        User user = userService.findByUserName(userName);
        List<String> checked_list = userRecomService.
                findChildren(user.getUserId());
        return checked_list;
    }

    @Override
    @Transactional
    public void addAgent(String userName, String password, String safeWord, String roleName, String remarks, String parentsUserCode, boolean loginAuthorityboolean, boolean operaAuthority) {
        agentService.saveAgent(userName,
                password, "000000", roleName, remarks, parentsUserCode,
                loginAuthorityboolean, operaAuthority);
        SysUser dbUser = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, userName));
        if (dbUser != null) {
            throw new YamiShopBindException("该用户已存在");
        }
        List<SysRole> list = roleService.list(Wrappers.<SysRole>query()
                .lambda().eq(SysRole::getRoleName, "代理商"));
        List<Long> roleIdList = new ArrayList<>();
        for (SysRole sysUser : list) {
            roleIdList.add(sysUser.getRoleId());
        }
        SysUser sysUser = new SysUser();
        sysUser.setPassword(passwordEncoder.encode(password));
        sysUser.setRemarks(remarks);
        sysUser.setSafePassword(passwordEncoder.encode(safeWord));
        sysUser.setEmail(null);
        sysUser.setRoleIdList(roleIdList);
        sysUser.setMobile(null);
        sysUser.setUsername(userName);
        sysUser.setStatus(loginAuthorityboolean ? 1 : 0);
        sysUserService.saveUserAndUserRole(sysUser);
    }

    @Override
    @Transactional
    public void restPassword(String password, String id) {
        Agent agent = agentService.getById(id);
        if (agent == null) {
            throw new YamiShopBindException("参数错误!");
        }
        User user = userService.getById(agent.getUserId());
        if (user == null) {
            throw new YamiShopBindException("代理商不存在!");
        }
        user.setLoginPassword(passwordEncoder.encode(password));
        userService.updateById(user);
        SysUser sysUser = sysUserService.getByUserName(user.getUserName());
        sysUser.setPassword(passwordEncoder.encode(password));
        logService.saveLog(user, "ip:" + IPHelper.getIpAddr() + ",管理员手动代理商修改登录密码", Constants.LOG_CATEGORY_OPERATION);
    }

    @Override
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

    @Override
    @Transactional
    public void unbindGoogleAuthCode(String id) {
        Agent agent = agentService.getById(id);
        if (agent == null) {
            throw new YamiShopBindException("参数错误");
        }
        User user = userService.getById(agent.getUserId());
        SysUser sysUser = sysUserService.getByUserName(user.getUserName());
        if (sysUser == null) {
            throw new YamiShopBindException("参数错误");
        }
        sysUser.setGoogleAuthSecret(null);
        sysUser.setGoogleAuthBind(false);
        sysUserService.updateById(sysUser);
        user.setGoogleAuthBind(false);
        user.setGoogleAuthSecret(null);
        userService.updateById(user);
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_OPERATION);
        log.setOperator(SecurityUtils.getSysUser().getUsername());
        log.setUsername(user.getUserName());
        log.setUserId(user.getUserId().toString() + "");
        log.setLog("ip:" + IPHelper.getIpAddr() + "谷歌验证器解除绑定");
        log.setCreateTime(new Date());
        logService.save(log);
    }

    @Override
    public void bindGoogleAuthCode(String googleAuthSecret, String id) {
        Agent agent = agentService.getById(id);
        if (agent == null) {
            throw new YamiShopBindException("参数错误");
        }
        User user = userService.getById(agent.getUserId());
        SysUser sysUser = sysUserService.getByUserName(user.getUserName());
        if (sysUser == null) {
            throw new YamiShopBindException("参数错误");
        }
        sysUser.setGoogleAuthSecret(googleAuthSecret);
        sysUser.setGoogleAuthBind(true);
        sysUserService.updateById(sysUser);
        user.setGoogleAuthBind(true);
        user.setGoogleAuthSecret(googleAuthSecret);
        userService.updateById(user);
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_OPERATION);
        log.setOperator(SecurityUtils.getSysUser().getUsername());
        log.setUsername(user.getUserName());
        log.setUserId(user.getUserId().toString() + "");
        log.setLog("ip:" + IPHelper.getIpAddr() + "谷歌验证器绑定");
        log.setCreateTime(new Date());
        logService.save(log);
    }
}
