package com.yami.trading.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class SetSafewordModel {


    @Length(min = 1,message = "资金密码最小6位")
    @NotBlank(message = "资金密码不能为空")
    @ApiModelProperty("资金密码")
    private  String safeword;
}
