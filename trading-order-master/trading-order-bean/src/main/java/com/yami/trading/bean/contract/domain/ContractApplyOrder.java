package com.yami.trading.bean.contract.domain;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 订单表Entity
 * @author lucas
 * @version 2023-03-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_contract_apply_order")
public class ContractApplyOrder extends BaseEntity {


	public final static String STATE_SUBMITTED = "submitted";
	public final static String STATE_CANCELED = "canceled";
	public final static String STATE_CREATED = "created";
	/**
	 * 多仓
	 */
	public final static String DIRECTION_BUY = "buy";
	/**
	 * 空仓
	 */
	public final static String DIRECTION_SELL = "sell";
	/**
	 * 开仓
	 */
	public final static String OFFSET_OPEN = "open";

	/**
	 * 平仓
	 */
	public final static String OFFSET_CLOSE = "close";

	/**
	 * 限价单
	 */
	public final static String ORDER_PRICE_TYPE_LIMIT = "limit";

	/**
	 * 对手价（市价）
	 */
	public final static String ORDER_PRICE_TYPE_OPPONENT = "opponent";

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
     *  订单 号
     */
	private String orderNo;
	/**
     * "buy":多 "sell":空
     */
	private String direction;
	/**
     *  "open":开 "close":平
     */
	private String offSet;
	/**
     * 委托数量(剩余)(张)
     */
	private BigDecimal volume;
	/**
     * 委托数量(张)
     */
	private BigDecimal volumeOpen;
	/**
     * 杠杆倍数[“开仓”若有10倍多单，就不能再下20倍多单]
     */
	private BigDecimal leverRate;
	/**
     * limit order的交易价格
     */
	private BigDecimal price;
	/**
     * 止盈触发价格
     */
	private BigDecimal stopPriceProfit;
	/**
     * 止损触发价格
     */
	private BigDecimal stopPriceLoss;
	/**
     * 订单报价类型。 "limit":限价 "opponent":对手价（市价）
     */
	private String orderPriceType;
	/**
     * 状态。submitted 已提交，canceled 已撤销， created 委托完成
     */
	private String state;
	/**
     * 每手金额
     */
	private BigDecimal unitAmount;
	/**
     * 手续费
     */
	private BigDecimal fee;
	/**
     * 保证金
     */
	private BigDecimal deposit;


}
