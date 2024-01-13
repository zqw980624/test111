package com.yami.trading.bean.purchasing;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.UUIDEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_purchasing")
public class Purchasing extends UUIDEntity {

    /**
     * 创建日期
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新日期
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;



    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private int delFlag;

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


    @ApiModelProperty("项目总类Symbol")
    @TableField(exist = false)
    private  String projectTypeName;

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
    /**
     * 预计上线时间
     */
    @ApiModelProperty("预计上线时间")
    private  Date expectedLaunchTime;
    /**
     * 开始申购时间
     */
    @ApiModelProperty("开始申购时间")
    private  Date subscriptionStartTime;
    /**
     * 结束申购时间
     */
    @ApiModelProperty("结束申购时间")
    private  Date subscriptionEndTime;
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
