package com.yami.trading.sys.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class UnbindingGoogleAuthModel {


    private  long id;


    @ApiModelProperty("超级谷歌验证码")
    @NotBlank
    private String rootGoogleAuthCode;
}
