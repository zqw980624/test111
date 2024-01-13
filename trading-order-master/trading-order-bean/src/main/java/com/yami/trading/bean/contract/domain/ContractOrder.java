package com.yami.trading.bean.contract.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 非按金额订单Entity
 *
 * @author lucas
 * @version 2023-03-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_contract_order")
public class ContractOrder extends BaseEntity {
    public final static String STATE_SUBMITTED = "submitted";
    public final static String STATE_CREATED = "created";
    /**
     * 多仓
     */
    public final static String DIRECTION_BUY = "buy";
    /**
     * 空仓
     */
    public final static String DIRECTION_SELL = "sell";
    private static final long serialVersionUID = 1L;
    /**
     * PARTY_ID
     */
    private String partyId;
    /**
     * 代码
     */
    private String symbol;
    /**
     * 订单 号
     */
    private String orderNo;
    /**
     * "buy":买(多) "sell":卖(空)
     */
    private String direction;
    /**
     * 每手金额
     */
    private BigDecimal unitAmount;
    /**
     * 平仓退回金额
     */
    private BigDecimal amountClose;
    /**
     * 手续费
     */
    private BigDecimal fee;
    /**
     * 保证金(剩余)
     */
    private BigDecimal deposit ;
    /**
     * 保证金
     */
    private BigDecimal depositOpen ;
    /**
     * 收益
     */
    private BigDecimal profit;
    /**
     * 成交均价(成本)
     */
    private BigDecimal tradeAvgPrice;
    /**
     * 止盈触发价格
     */
    private BigDecimal stopPriceProfit;
    /**
     * 止损触发价格
     */
    private BigDecimal stopPriceLoss;
    /**
     * 最小浮动
     */
    private BigDecimal pips;
    /**
     * 最小浮动金额（以交易金额计算）
     */
    private BigDecimal pipsAmount;
    /**
     * 状态。submitted 已提交（持仓）， created 完成（平仓）
     */
    private String state = "submitted";
    /**
     * LEVER_RATE
     */
    private BigDecimal leverRate;
    /**
     * 委托数量(剩余)(张)
     */
    private BigDecimal volume;
    /**
     * 委托数量(张)
     */
    private BigDecimal volumeOpen;
    /**
     * 平仓时间
     */
    private Long closeTime;
    /**
     * 平仓均价
     */
    private BigDecimal closeAvgPrice;

    /**
     * 平仓时间戳
     */
    private Long closeTimeTs;
    /**
     * 强平价格
     */
    private String forceClosePrice;
    /**
     * 涨跌幅
     */
    @TableField(exist = false)
    private BigDecimal changeRatio;

    private String orderPriceType;

    public BigDecimal getAmountClose() {
        if(amountClose == null){
            amountClose = BigDecimal.ZERO;
        }
        return amountClose;
    }



    public BigDecimal getDeposit() {
        if(deposit == null){
            deposit = BigDecimal.ZERO;
        }
        return deposit;
    }

    public BigDecimal getDepositOpen() {
        if(depositOpen == null){
            depositOpen = BigDecimal.ZERO;
        }
        return depositOpen;
    }

    public BigDecimal getTradeAvgPrice() {
        if(tradeAvgPrice == null){
            tradeAvgPrice = BigDecimal.ZERO;
        }
        return tradeAvgPrice;
    }

    public BigDecimal getStopPriceProfit() {
        if(stopPriceProfit == null){
            stopPriceProfit = BigDecimal.ZERO;
        }
        return stopPriceProfit;
    }

    public BigDecimal getStopPriceLoss() {
        if(stopPriceLoss == null){
            stopPriceLoss = BigDecimal.ZERO;
        }
        return stopPriceLoss;
    }

    public BigDecimal getChangeRatio() {
        if(amountClose == null){
            amountClose = BigDecimal.ZERO;
        }
        if(profit == null){
            profit = BigDecimal.ZERO;
        }
        if(deposit == null){
            deposit = BigDecimal.ZERO;
        }
        if(depositOpen == null){
            depositOpen = BigDecimal.ZERO;
        }
        if (STATE_SUBMITTED.equals(state)) {
            changeRatio = amountClose.add(profit).add(deposit).subtract(depositOpen).divide(depositOpen,10 , RoundingMode.HALF_UP);
        } else {
            changeRatio = amountClose.add(deposit).subtract(depositOpen).divide(depositOpen, 10 , RoundingMode.HALF_UP);
        }

        changeRatio = changeRatio.multiply(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP);
        return changeRatio;
    }

    public BigDecimal getProfit() {
        if(profit == null){
            return BigDecimal.ZERO;
        }
        return profit;
    }
}
