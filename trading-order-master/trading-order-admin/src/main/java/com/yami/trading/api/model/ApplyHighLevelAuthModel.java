package com.yami.trading.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class ApplyHighLevelAuthModel {

    @ApiModelProperty("工作地址")
    @NotBlank
    private  String work_place;

    @ApiModelProperty("家庭地址")
    @NotBlank
    private  String home_place;
    /**
     * 亲属关系
     */
    @ApiModelProperty("亲属关系")
    @NotBlank
    private  String relatives_relation;

    @ApiModelProperty("亲属名称")
    @NotBlank
    private  String relatives_name;

    @ApiModelProperty("亲属地址")
    @NotBlank
    private  String  relatives_place;

    @ApiModelProperty("亲属电话")
    @NotBlank
    private  String relatives_phone;

}
