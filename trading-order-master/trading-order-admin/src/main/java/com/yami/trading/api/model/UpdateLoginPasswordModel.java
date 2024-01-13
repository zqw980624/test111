package com.yami.trading.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@ApiModel
public class UpdateLoginPasswordModel implements Serializable {


    @ApiModelProperty("旧密码")
    @NotBlank
    private String oldPassword;

    @ApiModelProperty("新密码")
    @NotBlank
    private String password;
}
