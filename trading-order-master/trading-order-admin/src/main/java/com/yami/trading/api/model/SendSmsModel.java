package com.yami.trading.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class SendSmsModel {

    @ApiModelProperty("手机号码")
    @NotBlank
    private  String mobile;


    @ApiModelProperty("国际区号")
    @NotBlank
    private  String code;

}
