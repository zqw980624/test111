package com.yami.trading.admin.controller.c2c.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class BankCardOrderModel extends PageRequest {

    @ApiModelProperty("状态 0 待付款 1 已付款 2 申诉中 3 已完成 4  已取消 5 已超时")
    private  String status;

    @ApiModelProperty("订单号")
    private  String order_no;
    private  String rolename;
    @ApiModelProperty("用户名、用户UID")
    private  String user_code;

    @ApiModelProperty("所有买卖方式  buy 买入  sell 卖出")
    private  String direction;
}
