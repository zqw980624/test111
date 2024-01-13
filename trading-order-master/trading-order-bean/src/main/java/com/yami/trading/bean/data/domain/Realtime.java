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
@EqualsAndHashCode(callSuper = false)
@TableName("t_realtime")
@ApiModel
@Slf4j
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Realtime extends UUIDEntity implements Comparable<Realtime>, Cloneable {
    @TableField(exist = false)
    private String     fundamentalDividend;
    @TableField(exist = false)
    private String     fundamentalEps;
    @TableField(exist = false)
    private String     fundamentalMarketCap;
    @TableField(exist = false)
    private String     fundamentalRatio;
    @TableField(exist = false)
    private String     fundamentalRevenue;
    @TableField(exist = false)
    private String     prevClose;

    private static final long serialVersionUID = 1L;
    /**
     * 产品代码
     */
    @ApiModelProperty("产品代码")
    private String symbol;
    /**
     * 时间戳
     */
    @ApiModelProperty("时间戳")
    private Long ts;

    /**
     * 产品名称
     */
    @ApiModelProperty("产品名称")
    private String name;
    /**
     * 开盘价
     */
    @ApiModelProperty("开盘价")
    private BigDecimal open;
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
     * 成交量 币个数
     */

    @ApiModelProperty(" 成交量 币个数")
    private BigDecimal amount;
    /**
     * 成交额 金额
     *
     */

    @ApiModelProperty(" 成交额 金额")
    private BigDecimal volume;
    /**
     * type
     */
    private String type;

    /**
     * 涨跌幅
     */
    @TableField(exist = false)
    @ApiModelProperty("涨跌幅")
    private BigDecimal changeRatio;

    @TableField(exist = false)
    @ApiModelProperty("涨跌幅")
    private String changeRatios;
    /**
     * 净值涨跌幅
     */
    @TableField(exist = false)
    @ApiModelProperty("净值涨跌幅")
    private BigDecimal netChange;

    /**
     * 市值
     */
    @TableField(exist = false)
    @JSONField(name = "market_capital")
    private Long marketCapital;

    /**
     * 流通市值
     */
    @TableField(exist = false)
    @JSONField(name = "float_market_capital")
    private Long floatMarketCapital;

    /**
     * 市盈率
     */
    @TableField(exist = false)
    @JSONField(name = "pe_forecast")
    private BigDecimal peForecast;

    /**
     * 量比
     */
    @TableField(exist = false)
    @JSONField(name = "volume_ratio")
    private BigDecimal volumeRatio ;

    /**
     * 换手率
     */
    @TableField(exist = false)
    @JSONField(name = "turnover_rate")
    private BigDecimal turnoverRate ;

    @TableField(exist = false)
    @ApiModelProperty("产品代码")
    private String symbolData;
    /**
     * 时间戳的"yyyy-MM-dd HH:mm:ss"格式
     */
    @TableField(exist = false)
    private String currentTime;

    @TableField(exist = false)
    private String chg;
    /**
     * 卖价
     */
    private BigDecimal bid;

    /**
     * 买价格
     */
    private BigDecimal ask;


    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public int compareTo(Realtime realtime) {
        if (this.ts > realtime.getTs()) {
            return 1;
        } else if (this.ts < realtime.getTs()) {
            return -1;
        }
        return 0;
    }

    public BigDecimal getChangeRatio() {
        if (BigDecimal.ZERO.compareTo(open) == 0) {
            return BigDecimal.ZERO;

        }
        BigDecimal changeRatio = close.subtract(open).divide(open, 10, RoundingMode.HALF_UP);
        changeRatio = changeRatio.multiply(new BigDecimal(100)).setScale(2, RoundingMode.DOWN);
        return changeRatio;


    }

    public BigDecimal getNetChange() {
        BigDecimal netChange = close.multiply(getChangeRatio()).divide(new BigDecimal(100), 10, RoundingMode.HALF_UP);
        netChange = netChange.setScale(4, RoundingMode.DOWN);
        return netChange;
    }

    public String getCurrentTime() {
        currentTime = DateUtils.timeStamp2Date(String.valueOf(ts));
        return currentTime;
    }


}
