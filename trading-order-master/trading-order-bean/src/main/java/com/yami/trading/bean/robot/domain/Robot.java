package com.yami.trading.bean.robot.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.domain.UUIDEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 下单机器人Entity
 *
 * @author lucas
 * @version 2023-05-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_robot")
public class Robot extends UUIDEntity {

    /**
     * 交易对
     */
    private String symbol;

    @TableField("`user`")
    private String user;

    @ApiModelProperty("状态 1 正常 0 无效")
    @TableField("`status`")
    private Integer status;

    /**
     * 价格变化步长
     */
    @ApiModelProperty("价格变化步长")
    private String step;

    /**
     * 最大下单间隔,单位秒
     */
    @ApiModelProperty("最大下单间隔,单位秒")
    private Long maxmunInterval;

    /**
     * 最小下单间隔,单位秒
     */
    @ApiModelProperty("最小下单间隔,单位秒")
    private Long minmunInterval;

    /**
     * 最大下单数量
     */
    @ApiModelProperty("最大下单数量")
    private Double maxmunNum;

    /**
     * 最小下单数量
     */
    @ApiModelProperty("最小下单数量")
    private Double minmunNum;

    /**
     * 机器人资金
     */
    @ApiModelProperty("机器人资金")
    private Double money;

    /**
     * 最小交易量
     */
    @ApiModelProperty("最小交易量")
    private Double minmuanAmount;

    @ApiModelProperty()
    private Double random1;

    @ApiModelProperty()
    private Double random2;

    @ApiModelProperty()
    private Double random3;

    @ApiModelProperty()
    private Double random4;

    @ApiModelProperty()
    private Double random5;

    @ApiModelProperty()
    private Double random6;

    /**
     * 价格精度
     */
    @ApiModelProperty("价格精度")
    private Long priceDecimals;

    /**
     * 数量精度
     */
    @ApiModelProperty("数量精度")
    private Long numDecimals;

    /**
     * 买卖最高差价
     */
    @ApiModelProperty("买卖最高差价")
    private Double maxumPriceDiff;

    /**
     * 买单数量
     */
    @ApiModelProperty("买单数量")
    private Double buyNum;

    /**
     * 卖单数量
     */
    @ApiModelProperty("卖单数量")
    private Double sellNum;

    /**
     * 高频量比
     */
    @ApiModelProperty("高频量比")
    private Double highFrequency;

    /**
     * 低频量比
     */
    @ApiModelProperty("低频量比")
    private Double lowFrequency;

    /**
     * 权重
     */
    @ApiModelProperty("权重")
    private Double weight;

    /**
     * 运行状态
     */
    @ApiModelProperty("运行状态")
    private Long runningStatus;

    /**
     * 跟随大盘
     */
    @ApiModelProperty("跟随大盘")
    private String followMarket;

    /**
     * 大盘涨幅
     */
    @ApiModelProperty("大盘涨幅")
    private String marketIncrease;

    /**
     * 扩大倍数
     */
    @ApiModelProperty("扩大倍数")
    private String multiple;

    /**
     * 修改时间
     */
    @ApiModelProperty("修改时间")
    private Date updateTime;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private Date createTime;

    private Item item;

    private String username;
    private String password;

}
