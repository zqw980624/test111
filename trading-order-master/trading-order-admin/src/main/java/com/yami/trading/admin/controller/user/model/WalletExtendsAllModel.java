package com.yami.trading.admin.controller.user.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class WalletExtendsAllModel {

    @ApiModelProperty("用户id")
    @NotBlank
    private  String  userId;
}
