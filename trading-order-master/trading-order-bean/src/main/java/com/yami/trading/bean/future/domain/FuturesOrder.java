package com.yami.trading.bean.future.domain;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 交割合约订单Entity
 * @author lucas
 * @version 2023-04-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_futures_order")
public class FuturesOrder extends BaseEntity {
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
     * ORDER_NO
     */
	private String orderNo;
	/**
     * "buy":买(多) "sell":卖(空)
     */
	private String direction;
	/**
     * 时间
     */
	private Integer timenum;
	/**
     * 时间单位
     */
	private String timeunit;
	/**
     * 每手金额
     */
	private BigDecimal unitAmount;
	/**
     * 委托数量
     */
	private Double volume;
	/**
     * 手续费
     */
	private double fee;
	/**
     * 收益率
     */
	private double profitRatio;
	/**
     * 收益
     */
	private double profit;
	/**
     * 成交均价(成本)
     */
	private Double tradeAvgPrice;
	/**
     * 平仓均价
     */
	private Double closeAvgPrice;
	/**
     * 状态。submitted 已提交（持仓）， created 完成（平仓）
     */
	private String state;
	/**
     * 平仓时间
     */
	private Long closeTime;
	/**
     * 结算时间
     */
	private Long settlementTime;


	/**
	 * 剩余时间 h:m:s
	 */
	@TableField(exist = false)
	private String remainTime;
	/**
     * 购买时控制场控
     */
	private String profitLoss;

}
