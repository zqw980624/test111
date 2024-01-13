package com.yami.trading.bean.data.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.yami.trading.common.domain.UUIDEntity;
import com.yami.trading.common.util.DateUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 实时价格Entity
 *
 * @author lucas
 * @version 2023-03-16
 */
@Data
@ApiModel
@Slf4j
public class RealtimeDe  {

    /**
     * 产品代码
     */
    @ApiModelProperty("产品代码")
    private String symbol;

    /**
     * 产品名称
     */
    @ApiModelProperty("产品名称")
    private String name;

    /**
     * 最高价
     */
    @ApiModelProperty("最高价")
    private BigDecimal high;
    /**
     * 最低价
     */
    @ApiModelProperty("最低价")
    private BigDecimal low;
    /**
     * 最新价
     */
    @ApiModelProperty("最新价")
    private BigDecimal close;
    /**
     * type
     */
    private String type;
    @ApiModelProperty("产品代码")
    private String symbolData;
    /**
     * 涨跌幅
     */
    @ApiModelProperty("涨跌幅")
    private BigDecimal changeRatio;


    private String chg;

    private String pid;
}
