package com.yami.trading.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class ApplyRealNameAuthModel {

    /**
     * 国籍
     */
    @ApiModelProperty("国籍")
    @NotBlank
    private String nationality;

    /**
     * ID名称，如身份证等
     */
    @ApiModelProperty("ID名称")
    @NotBlank
    private String idName;
    /**
     * 证件号码
     */
    @NotBlank
    @ApiModelProperty("证件号码")
    private String idNumber;
    /**
     * 实名姓名
     */
    @NotBlank
    @ApiModelProperty("实名姓名")
    private String name;
    /**
     * 证件正面照
     */
    @ApiModelProperty("证件正面照")
    @NotBlank
    private String idFrontImg;
    /**
     * 证件背面照
     */
    @ApiModelProperty("证件背面照")
    @NotBlank
    private String idBackImg;


    private String handheldPhoto;

}
