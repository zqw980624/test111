package com.yami.trading.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class RealNameAuthRecordDto {




    @ApiModelProperty("国籍")
    private String nationality;


    @ApiModelProperty("ID名称，如身份证等")
    private String idName;

    @ApiModelProperty("证件号码")
    private String idNumber;

    @ApiModelProperty("实名姓名")
    private String name;

    @ApiModelProperty("证件正面照")
    private String idFrontImg;

    @ApiModelProperty("证件背面照")
    private String idBackImg;


    @ApiModelProperty("0已申请未审核 ，1审核中 ，2 审核通过,3审核未通过")
    private int status;
    /**
     * 审核消息，未通过原因
     *
     */
    @ApiModelProperty("审核消息，未通过原因")
    private String msg;

    /**
     * 手持正面证件照文件名
     */
    private String handheldPhoto;

    /**
     * ID名称，如身份证等
     */
    private String idname;
    /**
     * 证件号码
     */
    private String idnumber;

    /**
     * 证件正面照文件名
     */
    private String idimg_1_path;
    /**
     * 证件背面照文件名
     */
    private String idimg_2_path;
    /**
     * 手持正面证件照文件名
     */
    private String idimg_3_path;



}
