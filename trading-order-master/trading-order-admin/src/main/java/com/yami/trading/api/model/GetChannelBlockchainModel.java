package com.yami.trading.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class GetChannelBlockchainModel {


    @ApiModelProperty("渠道  usdt")
    @NotBlank
    private  String coin;
}
