package com.yami.trading.admin.controller.c2c.model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class C2cAdverCloseModel {

    @ApiModelProperty("币种市价")
    private String id;
    private String login_safeword;

}
