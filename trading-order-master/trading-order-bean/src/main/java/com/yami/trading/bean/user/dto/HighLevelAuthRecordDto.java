package com.yami.trading.bean.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel
public class HighLevelAuthRecordDto {
    @ApiModelProperty("用户")
    private String userName;

    @ApiModelProperty("推荐人")
    private  String recomUserName;

    @ApiModelProperty("UID")
    private String userCode;

    @ApiModelProperty("账户类型")
    private String roleName;
    @ApiModelProperty("客户提交时间")
    private Date createTime;

    /**
     * 工作地址
     */
    @ApiModelProperty("工作地址")
    private String workPlace;

    private String work_place;

    /**
     * 家庭地址
     */
    @ApiModelProperty("家庭地址")
    private String homePlace;
    private String home_place;

    /**
     * 亲属关系
     */
    @ApiModelProperty("亲属关系")
    private String relativesRelation;
    private String relatives_relation;

    /**
     * 亲属名称
     */
    @ApiModelProperty("亲属名称")
    private String relativesName;
    private String relatives_name;

    /**
     * 亲属地址
     */
    @ApiModelProperty("亲属地址")
    private String relativesPlace;

    private String relatives_place;

    /**
     * 亲属电话
     */
    @ApiModelProperty("亲属电话")
    private String relativesPhone;
    private String relatives_phone;


    /**
     * 0已申请未审核 ，1 审核通过,2审核未通过
     */
    @ApiModelProperty("0已申请未审核 ，1 审核通过,2审核未通过")
    private int status;
    /**
     * 审核消息，未通过原因
     *
     */
    @ApiModelProperty("审核消息，未通过原因")
    private String msg;

    /**
     * 审核时间
     */
    @ApiModelProperty("审核时间")
    private Date operationTime;
    private Date operation_time;


    private  String uuid;
}
