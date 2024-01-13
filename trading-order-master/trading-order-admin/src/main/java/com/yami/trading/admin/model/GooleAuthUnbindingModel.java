package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class GooleAuthUnbindingModel {

    @NotBlank
    @ApiModelProperty("资金密码")
    private  String safeWord;

    @ApiModelProperty("谷歌验证码")
    private long gooleAuthCode;
}
