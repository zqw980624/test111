package com.yami.trading.admin.dto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class WaitCountDto {


    @ApiModelProperty("用户基础认证 数")
    private  long realNameAuthCount;



    @ApiModelProperty("高级认证数")
    private  long highLevelAuthCount;


    @ApiModelProperty("充值订单 数")
    private  long rechargeCount;

    @ApiModelProperty("提现订单 数")
    private  long withdrawCount;
}
