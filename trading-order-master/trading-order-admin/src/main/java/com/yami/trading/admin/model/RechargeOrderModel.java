package com.yami.trading.admin.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel
public class RechargeOrderModel extends PageRequest {
    @ApiModelProperty("账号类型")
    private String rolename;
    @ApiModelProperty(" usercode uuid")
    private String userName;
    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("创建时间开始时间 2023-03-22 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private  Date startTime;
    @ApiModelProperty("创建时间结束时间 2023-03-22 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private  String status;

}
