package com.yami.trading.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class BindEmailModel {



    @ApiModelProperty("邮箱")
    @NotBlank
    private  String email;

    @ApiModelProperty("验证码code")
    @NotBlank
    private   String verifCode;
}


