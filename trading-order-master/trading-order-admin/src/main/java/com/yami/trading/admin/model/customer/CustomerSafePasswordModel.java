package com.yami.trading.admin.model.customer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class CustomerSafePasswordModel {


    @ApiModelProperty("ID")
    private String id;

    @ApiModelProperty("资金密码")
    @NotBlank
    private String safePassword;


}
