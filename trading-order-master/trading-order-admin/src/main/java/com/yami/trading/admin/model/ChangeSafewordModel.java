package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class ChangeSafewordModel {

    @ApiModelProperty("旧资金密码")
    @NotBlank
    private String oldSafeword;


    @ApiModelProperty("新资金密码")
    @NotBlank
        private  String newSafeword;
}
