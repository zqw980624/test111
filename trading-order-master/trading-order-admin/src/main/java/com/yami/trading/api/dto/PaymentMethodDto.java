package com.yami.trading.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class PaymentMethodDto {


    /**
     * 支付方式名称
     */

    @ApiModelProperty("支付方式名称")
    private String methodName;

    private  String uuid;


    /**
     * 支付方式图片
     */
    @ApiModelProperty("支付方式图片")
    private String methodImg;

}
