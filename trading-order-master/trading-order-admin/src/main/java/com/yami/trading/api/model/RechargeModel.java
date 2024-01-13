package com.yami.trading.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class RechargeModel {



    @ApiModelProperty("充值数量")
    private double channelAmount;

    @ApiModelProperty("区块链充值地址")
    @NotBlank
    private String blockchainName;

    @ApiModelProperty("付款凭证")
    @NotBlank
    private String img;


    @ApiModelProperty("区块链充值地址")
    @NotBlank
    private String channelAddress;


    @ApiModelProperty("充值币种")
    @NotBlank
    private String coin;

}
