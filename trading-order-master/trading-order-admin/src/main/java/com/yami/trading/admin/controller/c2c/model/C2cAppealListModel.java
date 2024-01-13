package com.yami.trading.admin.controller.c2c.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class C2cAppealListModel extends PageRequest {

    @ApiModelProperty("订单号")
    private  String orderNo;


    @ApiModelProperty("用户名、用户UID")
    private  String userCode;
    private  String roleName;

    @ApiModelProperty("申诉状态：0已提交/1已处理")
    private  String status;
    @ApiModelProperty("承兑商昵称、承兑商UID")
    private  String c2cUserCode;
    @ApiModelProperty("承兑商类型 1后台承兑商/2用户承兑商")
    private  String c2cUserType;
    @ApiModelProperty("承兑商用户名、用户UID")
    private  String c2cUserPartyCode;


}
