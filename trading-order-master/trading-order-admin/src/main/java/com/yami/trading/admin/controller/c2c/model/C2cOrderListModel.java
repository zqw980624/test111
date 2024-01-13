package com.yami.trading.admin.controller.c2c.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.servlet.http.HttpServletRequest;

@Data
@ApiModel
public class C2cOrderListModel  extends PageRequest {


    @ApiModelProperty("状态 0 待付款 1 已付款 2 申诉中 3 已完成 4  已取消 5 已超时")
        private  String status;

        @ApiModelProperty("订单号")
    private  String order_no;
    private  String rolename;
    @ApiModelProperty("承兑商昵称、承兑商UID")
    private  String c2c_user_code;

    @ApiModelProperty("承兑商类型 1后台承兑商/2用户承兑商")
    private  String c2c_user_type;


    @ApiModelProperty("承兑商用户名、承兑商用户UID")
    private  String c2c_user_party_code;


    @ApiModelProperty("用户名、用户UID")
    private  String user_code;

    @ApiModelProperty("所有买卖方式  buy 买入  sell 卖出")
    private  String direction;



}
