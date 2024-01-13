package com.yami.trading.admin.model.c2c;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class SaveTranslateModel {

    @ApiModelProperty("参数名  比如  param_name1")
    @NotBlank
    private  String contentName;


    @ApiModelProperty("参数名1（必填）")
    @NotBlank
    private  String content;

    @ApiModelProperty("参数名1多语言")
    @NotBlank
    private  String langTrans;
}
