package com.yami.trading.service.agent;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.agent.AgentDto;
import com.yami.trading.bean.model.Agent;

public interface AgentService extends IService<Agent> {
    Page<AgentDto> listTotal(Page page,  String userName);

    AgentDto listTotalAge(String userName);

    void saveAgent(String userName, String password, String safeWord, String roleName, String remarks, String parentsUserCode, boolean loginAuthorityboolean, boolean  operaAuthority);

    void updateAgent(String id, boolean loginAuthority, boolean operaAuthority,String remarks);

    Page<AgentDto> pagedQueryNetwork(Page page);
}
