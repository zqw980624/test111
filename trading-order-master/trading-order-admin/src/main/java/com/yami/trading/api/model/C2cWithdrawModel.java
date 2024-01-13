package com.yami.trading.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@ApiModel
public class C2cWithdrawModel {


    @NotBlank
    @ApiModelProperty("提款方式")
    private  String currency;

    @NotBlank
    @ApiModelProperty("支付方式")
    private  String paymentMethodId;


    @ApiModelProperty("提款数量")
    @Min(0)
    private BigDecimal amount;

    @NotBlank
    @ApiModelProperty("资金密码")
    private  String safeWord;
}
