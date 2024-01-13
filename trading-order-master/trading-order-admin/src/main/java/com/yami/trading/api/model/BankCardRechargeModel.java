package com.yami.trading.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@ApiModel
public class BankCardRechargeModel {

    @ApiModelProperty("法币")
    @NotBlank
    private  String currency;


    @ApiModelProperty("充值数量")
    private BigDecimal amount;

    @ApiModelProperty("付款凭证（上传支付详情截图))")
    @NotBlank
    private String  paymentVoucherImg;

    @ApiModelProperty("国家/地区")
    @NotBlank
    private  String country;

    @ApiModelProperty("国家/地区代码")
    @NotBlank
    private  String countryCode;
}
