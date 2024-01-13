package com.yami.trading.bean.future.domain;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 交割合约Entity
 * @author lucas
 * @version 2023-04-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_profit_loss_config")
public class ProfitLossConfig extends BaseEntity {

	/**
	 * 盈利
	 */
	public final static String TYPE_PROFIT = "profit";
	public final static String TYPE_LOSS = "loss";
	public final static String TYPE_BUY_PROFIT = "buy_profit";
	public final static String TYPE_SELL_PROFIT = "sell_profit";

	private static final long serialVersionUID = 1L;

	/**
	 * 买多盈利并且买空亏损
	 */
	public final static String TYPE_BUY_PROFIT_SELL_LOSS = "buy_profit_sell_loss";
	/**
	 * 买空盈利并且买多亏损
	 */
	public final static String TYPE_SELL_PROFIT_BUY_LOSS = "sell_profit_buy_loss";
	/**
     * PARTY_ID
     */
	private String partyId;
	/**
     * REMARK
     */
	private String remark;
	/**
     *	 * 见TYPE_* 1 盈利，2亏损，3 买多盈利，4买空盈利
     */
	private String type;

}
