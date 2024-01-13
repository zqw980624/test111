package com.yami.trading.admin.controller.user.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class AgentAllStatisticsModel extends PageRequest {

    @ApiModelProperty("开始时间 yyyy-MM-dd ")
    private   String startTime;

    @ApiModelProperty("结束时间 yyyy-MM-dd ")
    private  String endTime;

    @ApiModelProperty("uid 或 usercode")
    private String username;

    private  String userId;
}
