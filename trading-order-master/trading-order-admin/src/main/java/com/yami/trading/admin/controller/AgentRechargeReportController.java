package com.yami.trading.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.model.UserStatisticsModel;
import com.yami.trading.bean.user.dto.AgentRechargeReportDto;
import com.yami.trading.bean.user.dto.UserBenefitsDto;
import com.yami.trading.common.domain.Result;
import com.yami.trading.service.agent.AgentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

//@RestController
//@Api(tags ="用户收益报表")
//@RequestMapping("agentRechargeReport")

public class AgentRechargeReportController {
    @Autowired
    AgentService agentService;

//    @PostMapping(value = "list")
//    @ApiOperation("列表")
    public Result<Page<AgentRechargeReportDto>> listTotal(@RequestBody @Valid UserStatisticsModel request){
        Page page=new Page(request.getCurrent(), request.getSize());
        return Result.ok(page);
    }

}
