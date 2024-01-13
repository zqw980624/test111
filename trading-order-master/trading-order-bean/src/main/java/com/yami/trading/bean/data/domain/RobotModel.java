package com.yami.trading.bean.data.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 机器人k线图模型表
 * </p>
 *
 * @author HT
 * @since 2023-05-09 20:52:32
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_robot_model")
public class RobotModel {

    @TableId(type = IdType.ASSIGN_ID)
    private String uuid;

    /**
     * 股票代码
     */
    private String symbol;

    /**
     * 股票名称
     */
    @TableField("`name`")
    private String name;

    /**
     * 当前价	
     */
    @TableField("`current`")
    private Double current;

    /**
     * 涨跌额
     */
    private Double chg;

    /**
     * 日期字符
     */
    private String dateStr;

    /**
     * 涨跌幅	
     */
    private Double percent;

    /**
     * 年初至今涨跌幅
     */
    @JSONField(name = "current_year_percent")
    private Double currentYearPercent;

    /**
     * 成交量
     */
    private Long volume;

    /**
     * 成交额
     */
    private Long amount;

    /**
     * 换手率
     */
    @JSONField(name = "turnover_rate")
    private String turnoverRate;

//    /**
//     * 市盈率
//     */
//    @JSONField(name = "pe_ttm")
//    private String peTtm;

//    /**
//     * 股息率
//     */
//    @JSONField(name = "dividend_yield")
//    private String dividendYield;

    /**
     * 市值
     */
    @JSONField(name = "market_capital")
    private Long marketCapital;

    private String kLineData;

    private String delFlag;


}
