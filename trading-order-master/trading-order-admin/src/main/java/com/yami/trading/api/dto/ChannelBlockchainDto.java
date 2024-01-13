package com.yami.trading.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class ChannelBlockchainDto {

    @ApiModelProperty("币种名称 BTC ETH USDT")
    private String coin;


    @ApiModelProperty("链名称")
    private String blockchainName;


    @ApiModelProperty("区块链地址图片")
    private String img;

    @ApiModelProperty("区块链地址")
    private String address;
}
