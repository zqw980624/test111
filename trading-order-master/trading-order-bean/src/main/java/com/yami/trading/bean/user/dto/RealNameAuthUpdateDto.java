package com.yami.trading.bean.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class RealNameAuthUpdateDto {

    private String uuid;

    @ApiModelProperty("用户ID")
    private  String userId;

    @ApiModelProperty("用户")
    private String userName;

    /**
     * 国籍
     */

    @ApiModelProperty("国籍")
    private String nationality;
    /**
     * ID名称，如身份证等
     */
    @ApiModelProperty("ID名称，如身份证等")

    private String idName;
    /**
     * 证件号码
     */
    @ApiModelProperty("证件号码")
    private String idNumber;
    /**
     * 实名姓名
     */
    @ApiModelProperty("实名姓名")
    private String name;
    /**
     * 证件正面照
     */
    @ApiModelProperty("证件正面照")
    private String idFrontImg;
    /**
     * 证件背面照
     */
    @ApiModelProperty("证件背面照")
    private String idBackImg;
    /**
     * 手持证件正面照
     */
    @ApiModelProperty("手持证件正面照")
    private String handheldPhoto;
}
