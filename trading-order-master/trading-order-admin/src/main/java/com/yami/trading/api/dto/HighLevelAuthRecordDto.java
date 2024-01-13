package com.yami.trading.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel
@Data
public class HighLevelAuthRecordDto {


    @ApiModelProperty("真实姓名")
    private String realName;

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



    @ApiModelProperty("0已申请未审核 ，1审核中 ，2 审核通过,3审核未通过")
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
}
