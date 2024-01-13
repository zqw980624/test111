package com.yami.trading.dao.agent;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.agent.AgentDto;
import com.yami.trading.bean.model.Agent;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface AgentMapper extends BaseMapper<Agent> {

    Page listTotal(Page page, @Param("userName") String userName);

    AgentDto listTotalAge(String userName);

    Page pagedQueryNetwork(Page page);
}
