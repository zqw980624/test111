package com.yami.trading.admin.controller.user.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class WithdrawListModel  extends PageRequest {

    @ApiModelProperty("订单号")
    private  String orderNo;

    @ApiModelProperty("用户名 uid")
    private String userName;

    @ApiModelProperty("账号类型 ")
    private  String  rolename;
    @ApiModelProperty("")
    private String status;
}
