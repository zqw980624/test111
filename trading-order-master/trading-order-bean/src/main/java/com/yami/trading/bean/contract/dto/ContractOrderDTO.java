package com.yami.trading.bean.contract.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 非按金额订单DTO
 * @author lucas
 * @version 2023-03-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ContractOrderDTO  {

	private static final long serialVersionUID = 1L;

	/**
	 * PARTY_ID
	 */
	@ApiModelProperty("用户")
	private String userName;

	@ApiModelProperty("角色名称")
	private String roleName;

	private String pid;
	/**
     * 主键
     */
	@NotNull(message="主键不能为空")
	private String uuid;
	/**
     * PARTY_ID
     */
	@ApiModelProperty("partyId")
	private String partyId;
	/**
	 * PARTY_ID
	 */
	@ApiModelProperty("UID")
	private String userCode;
	/**
     * 代码
     */
	@ApiModelProperty("品种")
	private String symbol;
	/**
	 * 代码
	 */
	@ApiModelProperty("品种名称")
	private String symbolName;
	/**
     * ORDER_NO
     */
	@ApiModelProperty("订单号")
	private String orderNo;
	/**
     * DIRECTION
     */
	@ApiModelProperty("buy:买(多) sell:卖(空)")
	private String direction;
	/**
     * UNIT_AMOUNT
     */
	@ApiModelProperty("每手金额")
	private BigDecimal unitAmount;
	/**
     * AMOUNT_CLOSE
     */
	@ApiModelProperty("平仓退回金额")
	private BigDecimal amountClose;
	/**
     * 手续费
     */
	@ApiModelProperty("手续费")
	private BigDecimal fee;
	/**
     *  保证金(剩余)
     */
	@ApiModelProperty("保证金(剩余)")
	private BigDecimal deposit;
	/**
     *保证金
     */
	@ApiModelProperty("保证金")
	private BigDecimal depositOpen;
	/**
     * PROFIT
     */
	@ApiModelProperty("收益")
	private BigDecimal profit;
	/**
     * TRADE_AVG_PRICE
     */
	@ApiModelProperty("成交均价(成本)")
	private BigDecimal tradeAvgPrice;
	/**
     * STOP_PRICE_PROFIT
     */
	@ApiModelProperty("止盈触发价格")
	private BigDecimal stopPriceProfit;
	/**
     * STOP_PRICE_LOSS
     */
	@ApiModelProperty("止损触发价格")
	private BigDecimal stopPriceLoss;

	/**
     * STATE
     */
	@ApiModelProperty(" 状态。submitted 已提交（持仓）， created 完成（平仓）\n")
	private String state;

	/**
     * VOLUME
     */
	@ApiModelProperty("委托数量(剩余)(张)")
	private BigDecimal volume;
	/**
     * VOLUME_OPEN
     */
	@ApiModelProperty("委托数量(张)")
	private BigDecimal volumeOpen;

	/**
     * 创建时间
     */
	private Date createTime;
	@ApiModelProperty("钱包余额")
	private BigDecimal money;
	@ApiModelProperty("止盈止损")
	private String stopProfitLoss;

	public String getStopProfitLoss() {
		return StrUtil.format("{}/{}",  stopPriceProfit, stopPriceLoss);
	}

	@ApiModelProperty("盈亏")
	private BigDecimal profitLoss;

	@ApiModelProperty("平仓时间")
	private Long closeTime;
	/**
	 * 平仓均价
	 */
	private BigDecimal closeAvgPrice;

	/**
	 * 当前价格
	 */
	private BigDecimal mark_price;
	/**
	 * 当前价格
	 */
	private BigDecimal close;


	/**
	 * 平仓时间戳
	 */
	private Long closeTimeTs;
	/**
	 * 强平价格
	 */
	private String forceClosePrice;

	public BigDecimal getProfitLoss() {
		if("submitted".equalsIgnoreCase(state)){
			return amountClose.add(profit).add(deposit).subtract(depositOpen).setScale(4, BigDecimal.ROUND_HALF_UP);
		}else{
			return amountClose.add(deposit).subtract(depositOpen).setScale(4, BigDecimal.ROUND_HALF_UP);
		}
	}

	/**
	 * 剩余/委托金额
	 */
	private String vnvu;

	private String volumeUnitAmount;
	private String volumeOpenUnitAmount;

	public String getVolumeUnitAmount() {
		if(volume == null){
			volume = BigDecimal.ZERO;
		}
		if(unitAmount == null){
			unitAmount = BigDecimal.ZERO;
		}
		BigDecimal bigDecimal1 = volume.multiply(unitAmount).setScale(2, RoundingMode.HALF_UP);
		return bigDecimal1.toPlainString();
	}

	public String getVolumeOpenUnitAmount() {

		if(volumeOpen == null){
			volumeOpen = BigDecimal.ZERO;
		}
		if(unitAmount == null){
			unitAmount = BigDecimal.ZERO;
		}
		BigDecimal bigDecimal2 = volumeOpen.multiply(unitAmount).setScale(2, RoundingMode.HALF_UP);
		return bigDecimal2.toPlainString();
	}

	/**
	 * 剩余/委托保证金
	 */
	private String dd;
	public String getVnvu() {
		if(volume == null){
			volume = BigDecimal.ZERO;
		}
		if(volumeOpen == null){
			volumeOpen = BigDecimal.ZERO;
		}
		if(unitAmount == null){
			unitAmount = BigDecimal.ZERO;
		}
		BigDecimal bigDecimal1 = volume.multiply(unitAmount).setScale(2, RoundingMode.HALF_UP);
		BigDecimal bigDecimal2 = volumeOpen.multiply(unitAmount).setScale(2, RoundingMode.HALF_UP);
		return bigDecimal1.toPlainString()+"/"+bigDecimal2.toPlainString();
	}

	public String getDd() {
		if(deposit == null){
			deposit = BigDecimal.ZERO;
		}
		if(depositOpen == null){
			depositOpen = BigDecimal.ZERO;
		}
		return deposit.setScale(2, RoundingMode.HALF_UP).toPlainString()+"/"+depositOpen.setScale(2, RoundingMode.HALF_UP).toPlainString();
	}
}
