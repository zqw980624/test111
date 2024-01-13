package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class CmsModel {

    @ApiModelProperty("修改传id  新增不传")
    private String id;

    @ApiModelProperty("资金密码")
    @NotBlank
    private String loginSafeword;


    @ApiModelProperty("模块")
    @NotBlank
    private  String model;


    @ApiModelProperty("语言")
    @NotBlank
    private String language;

    @ApiModelProperty("业务代码， 同种内容 不同语言下的code相同")
    @NotBlank
    private  String contentCode;

    @ApiModelProperty("标题")
    @NotBlank
    private  String title;


    @ApiModelProperty("内容")
    @NotBlank
    private  String content;



}
