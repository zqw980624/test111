package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@ApiModel
public class UpdateWalltModel {


    @ApiModelProperty("用户id")
    @NotBlank
    private  String userId;


    @ApiModelProperty("账变金额(不能小于0)")
    @Min(0)
    private BigDecimal moneyRevise;


    @ApiModelProperty(" 账变类型 1 .平台充值金额(正式用户记录报表)  2 平台扣除金额(不记录报表)")
    @Min(1)
    @Max(2)
    private  int accountType;

    @ApiModelProperty("账变币种  usdt  btc  eth" )
    @NotBlank
    private  String coinType;



    @ApiModelProperty("登录人资金密码")
    @NotBlank
    private  String safePassword;
}
