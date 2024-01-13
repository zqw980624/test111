package com.yami.trading.admin.model.purchasing;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel
public class PurchasingAddModel {


    /**
     * 预计上线时间
     */
    @ApiModelProperty("预计上线时间 2023-03-22 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expectedLaunchTime;
    /**
     * 开始申购时间
     */
    @ApiModelProperty("开始申购时间 2023-03-22 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date subscriptionStartTime;
    /**
     * 结束申购时间
     */
    @ApiModelProperty("结束申购时间 2023-03-22 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date subscriptionEndTime;
    /**
     *项目ID
     */
    @ApiModelProperty("项目ID")
    private  String idCode;
    /**
     * 项目总类
     */
    @ApiModelProperty("项目总类Symbol")
    private  String projectTypeSymbol;




    /**
     * 申购项目名称
     */
    @ApiModelProperty("申购项目名称")
    private  String projectName;
    /**
     * 数据源类别
     */
    @ApiModelProperty("数据源类别 1 机器人刷单 2 采集 ")
    private int dataType;
    /**
     * 发行价
     */
    @ApiModelProperty("发行价")
    private String issuePrice;
    /**
     * 接受申购的币种
     */
    @ApiModelProperty("接受申购的币种")
    private  String currency;


    @ApiModelProperty("公布时间")
    private Date  publishTime;
    /**
     * 最小申购数量
     */
    @ApiModelProperty("最小申购数量")
    private  int minQuantity;
    /**
     * 最大申购数量
     *
     */

    @ApiModelProperty("最大申购数量")
    private  int maxQuantity;
    /**
     *
     */
    @ApiModelProperty("白皮书地址")
    private  String whitePagerAddress;
}
