package com.yami.trading.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class UpdateSafewordModel {

    @ApiModelProperty("资金密码")
    @NotBlank
    private String safeword;

    @ApiModelProperty("1 手机 2 邮箱 3 谷歌验证码")
    @NotBlank
    private int verifCodeType;

    @ApiModelProperty("验证码")
    @NotBlank
    private String verifCode;
}
