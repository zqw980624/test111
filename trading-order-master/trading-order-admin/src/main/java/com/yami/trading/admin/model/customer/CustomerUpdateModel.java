package com.yami.trading.admin.model.customer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class CustomerUpdateModel {


    @ApiModelProperty("ID")
    @NotBlank
    private String id;


    @ApiModelProperty("自动回复")
    @NotBlank
    private String autoAnswer;

    @ApiModelProperty("登录权限  true 开启 false 禁用")
    private boolean enabled;
    @ApiModelProperty("登录人资金密码")
    @NotBlank
    private String loginSafeword;

    @ApiModelProperty("备注")
    private String remarks;
}
