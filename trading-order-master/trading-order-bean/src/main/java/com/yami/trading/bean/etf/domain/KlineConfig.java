package com.yami.trading.bean.etf.domain;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.bean.data.domain.Kline;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.domain.BaseEntity;
import com.yami.trading.common.domain.UUIDEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * etfK线图配置表Entity
 *
 * @author lucas
 * @version 2023-05-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_kline_config")
public class KlineConfig extends UUIDEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 交易对
     */
    @ApiModelProperty("交易对")
    private String symbol;
    /**
     * 开市时间
     */
    @ApiModelProperty("开市时间")
    private Long openTimeTs;
    /**
     * 闭市时间
     */
    @ApiModelProperty("闭市时间")
    private Long closeTimeTs;
    /**
     * 开盘价
     */
    @ApiModelProperty("开盘价")
    private String openPrice;
    /**
     * 收盘价
     */
    @ApiModelProperty("收盘价")
    private String closePrice;
    /**
     * 今日最高价
     */
    @ApiModelProperty("今日最高价")
    private String high;
    /**
     * 今日最低价
     */
    @ApiModelProperty("今日最低价")
    private String low;
    /**
     * 今日最高成交量
     */
    @ApiModelProperty("今日最高成交量")
    private String turnoverHigh;
    /**
     * 今日最低成交量
     */
    @ApiModelProperty("今日最低成交量")
    private String turnoverLow;
    /**
     * 控盘策略
     */
    @ApiModelProperty("控盘策略")
    private Integer strategy;
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private Date createTime;
    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("根据这个配置生成的全天分钟k线图数据")
    private String minuteKline;

    @ApiModelProperty("根据这个配置生成的全天秒级k线图数据")
    private String secKline;

}
