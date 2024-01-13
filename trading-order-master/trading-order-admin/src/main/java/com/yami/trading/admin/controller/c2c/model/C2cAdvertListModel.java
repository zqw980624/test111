package com.yami.trading.admin.controller.c2c.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

@Data
@ApiModel
public class C2cAdvertListModel  extends PageRequest {


    @ApiModelProperty("承兑商昵称、承兑商UID")
    private  String c2c_user_code;


    @ApiModelProperty("承兑商类型 1后台承兑商/2用户承兑商")
    private  String c2c_user_type;

    @ApiModelProperty("用户名、用户UID")
    private  String user_code;

    @ApiModelProperty("所有买卖方式  buy 买入  sell 卖出")
    private  String direction;

    @ApiModelProperty("所有支付币种")
    private   String currency;

    @ApiModelProperty("所有上架币种")
    private  String symbol;
}
