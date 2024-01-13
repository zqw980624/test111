package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class CheckSafeWordModel {

    @ApiModelProperty("登录人资金密码")
    @NotBlank
    private  String loginSafeword;
}
