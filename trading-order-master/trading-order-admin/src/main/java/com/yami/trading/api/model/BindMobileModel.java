package com.yami.trading.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;


@Data
@ApiModel
public class BindMobileModel {



    @ApiModelProperty("手机号")
    @NotBlank
    private  String mobile;

    @ApiModelProperty("验证码code")
    @NotBlank
    private   String verifCode;
}