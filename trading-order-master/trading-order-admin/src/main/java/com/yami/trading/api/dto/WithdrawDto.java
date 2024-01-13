package com.yami.trading.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel
public class WithdrawDto {

    private String usernames;
    private String names;

    /**
     * 订单号
     */
    @ApiModelProperty("订单号")
    private String orderNo;
    /**
     * 数量
     */
    @ApiModelProperty("数量")
    private BigDecimal amount;

    /**
     * 订单手续费，USDT。
     */
    @ApiModelProperty("订单手续费USDT")
    private BigDecimal amountFee;


    @ApiModelProperty("创建时间")
    private Date createTime;



    /**
     * 提现货币 CNY USD
     */
    @ApiModelProperty("提现货币 CNY USD")
    private String currency;
}
