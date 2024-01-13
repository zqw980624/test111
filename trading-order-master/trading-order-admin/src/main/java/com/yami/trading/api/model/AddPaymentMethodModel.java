package com.yami.trading.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class AddPaymentMethodModel {

    @ApiModelProperty("更新传id 新增不传")
    private  String  id;

    /**
     * 支付方式模板ID
     */

    @NotBlank()
    @ApiModelProperty("支付方式模板ID")
    private String method_config_id;


    /**
     * 真实姓名
     */
    @NotBlank()
    @ApiModelProperty("真实姓名")
    private String real_name;




    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;



    @ApiModelProperty("参数1")
    private String param_value1;


    private String param_value2;
    private String param_value3;
    private String param_value4;
    private String param_value5;
    private String param_value6;
    private String param_value7;
    private String param_value8;
    private String param_value9;
    private String param_value10;
    private String param_value11;
    private String param_value12;
    private String param_value13;
    private String param_value14;
    private String param_value15;
    private  String qrcode;

}
