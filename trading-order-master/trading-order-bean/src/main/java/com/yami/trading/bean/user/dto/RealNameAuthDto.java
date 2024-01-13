package com.yami.trading.bean.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel
public class RealNameAuthDto {

    private  String uuid;

    @ApiModelProperty("用户")
    private String userName;

    @ApiModelProperty("UID")
    private String userCode;

    @ApiModelProperty("账户类型")
    private String roleName;


    @ApiModelProperty("绑定手机")
    private String userMobile;

    @ApiModelProperty("用户邮箱")
    private String userMail;


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
    /**
     * 0已申请未审核 ，1审核中 ，2 审核通过,3审核未通过
     */
    @ApiModelProperty("0已申请未审核 ，1 审核通过,2审核未通过")
    private int status;
    /**
     * 审核消息，未通过原因
     */
    @ApiModelProperty("审核消息，未通过原因")
    private String msg;






    /**
     * 审核时间
     */
    @ApiModelProperty("审核时间")
    private Date operationTime;


    @ApiModelProperty("申请时间")
    private  Date createTime;


    @ApiModelProperty("推荐用户")
    private  String recomUserName;

    @ApiModelProperty("推荐用户code")
    private  String recomUserCode;
}
