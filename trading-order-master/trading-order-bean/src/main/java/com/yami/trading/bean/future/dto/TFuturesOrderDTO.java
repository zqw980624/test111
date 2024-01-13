package com.yami.trading.bean.future.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.checkerframework.checker.units.qual.A;

/**
 * 交割合约订单DTO
 * @author lucas
 * @version 2023-04-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TFuturesOrderDTO extends BaseDTO {

	private static final long serialVersionUID = 1L;

	/**
     * 主键
     */
	@NotNull(message="主键不能为空")
	private String uuid;
	/**
     * PARTY_ID
     */
	@ApiModelProperty("下单用户id")
	private String partyId;
	private String userCode;
	@ApiModelProperty("下单用户角色")
	private String roleName;
	@ApiModelProperty("下单用户名")
	private String userName;
	@ApiModelProperty("剩余时间 h:m:s")
	private String remainTime;

	/**
     * 代码
     */
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
	@ApiModelProperty("\"buy\":买(多) \"sell\":卖(空)")
	private String direction;
	/**
     * TIMENUM
     */


	@ApiModelProperty("合约时间")
	private Long timenum;
	private String timenumStr;
	/**
     * TIMEUNIT
     */


	@ApiModelProperty("时间单位")
	private String timeunit;
	/**
     * UNIT_AMOUNT
     */
	@ApiModelProperty("每手金额")
	private BigDecimal unitAmount;
	/**
     * 委托数量
     */
	@ApiModelProperty("购买金额")
	private BigDecimal volume;
	/**
     * FEE
     */
	@ApiModelProperty("手续费")
	private BigDecimal fee;
	/**
     * PROFIT_RATIO
     */
	@ApiModelProperty("收益率")
	private BigDecimal profitRatio;
	/**
     * PROFIT
     */
	@ApiModelProperty("盈亏")
	private BigDecimal profit;
	/**
     * TRADE_AVG_PRICE
     */
	@ApiModelProperty("购买价")
	private BigDecimal tradeAvgPrice;
	/**
     * CLOSE_AVG_PRICE
     */
	@ApiModelProperty("结算价")
	private BigDecimal closeAvgPrice;
	/**
     * STATE
     */
	@ApiModelProperty("状态。submitted 已提交（持仓）， created 完成（平仓）")
	private String state;
	/**
     * CREATE_TIME
     */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	/**
     * CLOSE_TIME
     */
	@ApiModelProperty("平仓时间，前端根据时区计算具体时间")
	private Long closeTime;
	/**
     * SETTLEMENT_TIME
     */
	@ApiModelProperty("交割时间，前端根据时区计算具体时间")
	private Long settlementTime;
	/**
     * PROFIT_LOSS
     */
	@ApiModelProperty("购买时控制场控")
	private String profitLoss;


	/**
     * 更新时间
     */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;
	/**
     * 备注信息
     */
	private String remarks;
	/**
     * 时间戳
     */
	@ApiModelProperty("购买时间")
	private Long createTimeTs;
	/**
     * 更新时间戳
     */
	private Long updateTimeTs;

	@ApiModelProperty("订单盈亏控制情况（优先级高于交割场控设置）")
	private String profitLosssStr;

	public String getProfitLosssStr() {
		if("profit".equalsIgnoreCase(profitLoss)){
			return "盈利";
		}
		if ("loss".equalsIgnoreCase(profitLoss)){
			return "亏损";
		}
		return "";
	}
}
