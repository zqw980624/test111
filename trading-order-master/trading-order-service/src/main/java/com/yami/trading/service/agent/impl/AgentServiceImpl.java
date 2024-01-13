package com.yami.trading.service.agent.impl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mysql.cj.util.StringUtils;
import com.yami.trading.bean.agent.AgentDto;
import com.yami.trading.bean.model.Agent;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.exception.BusinessException;
import com.yami.trading.common.util.IPHelper;
import com.yami.trading.dao.agent.AgentMapper;
import com.yami.trading.service.agent.AgentService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.service.user.UserRecomService;
import com.yami.trading.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.Date;
import java.util.prefs.BackingStoreException;

@Service
public class   AgentServiceImpl  extends ServiceImpl<AgentMapper, Agent> implements AgentService {

    @Autowired
    UserRecomService userRecomService;

    @Autowired
    UserService userService;

    @Autowired
    LogService logService;

    @Override
    public Page<AgentDto> listTotal(Page page, String userName) {
        Page<AgentDto> reportDtoPage =  baseMapper.listTotal(page,userName);
        return reportDtoPage;
    }
    @Override
    public AgentDto listTotalAge(String userName){

        AgentDto reportDto =  baseMapper.listTotalAge(userName);
        return reportDto;
    }
    @Override
    @Transactional
    public void saveAgent(String userName, String password, String safeWord, String roleName, String remarks, String parentsUserCode, boolean loginAuthorityboolean, boolean  operaAuthority) {

        User user=  userService.saveAgentUser(userName,password,safeWord,roleName,remarks,parentsUserCode,loginAuthorityboolean);
        Agent  agent=new Agent();
        agent.setUserId(user.getUserId());
        if (!StringUtils.isNullOrEmpty(parentsUserCode)) {
            agent.setParentUserId(user.getUserRecom());
        }
        save(agent);
        String log = MessageFormat.format(
                "ip:" + IPHelper.getIpAddr() + ",管理员新增代理商，用户名:{0},登录权限:{1},备注:{2},推荐人uid:{3},操作权限:{4}",
               userName,loginAuthorityboolean,remarks,parentsUserCode,operaAuthority);
        logService.saveLog(user,log,Constants.LOG_CATEGORY_OPERATION);
    }

    @Override
    @Transactional
    public void updateAgent(String id, boolean loginAuthority, boolean operaAuthority,String remarks) {
        Agent agent = getById(id);
        if (agent==null){
            throw  new BusinessException("代理商不存在!");
        }
        User  user = userService.getById(agent.getUserId());
        String log = MessageFormat.format("ip:" +IPHelper.getIpAddr()  + ",管理员修改代理商，用户名:{0},原登录权限:{1},原备注:{2},原操作权限:{3}",
                user.getUserName(), user.getStatus(), user.getRemarks(),
                Constants.SECURITY_ROLE_AGENT.equals(user.getRoleName()));

        String roleName = operaAuthority? Constants.SECURITY_ROLE_AGENT : Constants.SECURITY_ROLE_AGENTLOW;
        user.setRoleName(roleName);

        user.setLoginAuthority(loginAuthority);
        user.setEnabled(operaAuthority);
        user.setRemarks(remarks);

        user.setStatus(loginAuthority ? 1 : 0);
        userService.updateById(user);
        log += MessageFormat.format(",新登录权限:{0},新备注:{1},新操作权限:{2}", loginAuthority, user.getRemarks(), operaAuthority);
        logService.saveLog(user,log,Constants.LOG_CATEGORY_OPERATION);

    }

    @Override
    public Page<AgentDto> pagedQueryNetwork(Page page) {
        return baseMapper.pagedQueryNetwork(page);
    }
}
