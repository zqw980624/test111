package com.yami.trading.bean.contract.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

import cn.hutool.core.util.StrUtil;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 订单表DTO
 * @author lucas
 * @version 2023-03-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel("永续委托单")
public class ContractApplyOrderDTO extends BaseDTO {

	private static final long serialVersionUID = 1L;

	/**
     * 主键
     */
	@ApiModelProperty("主键")
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
	 * PARTY_ID
	 */
	@ApiModelProperty("用户")
	private String userName;

	@ApiModelProperty("账户类型,MEMBER 正式账号,GUEST 演示用户,TEST 试用用户")
	private String roleName;
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
	@ApiModelProperty("合约方向，buy:多 sell:空")
	private String direction;
	/**
     * OFFSET
     */
	private String offset;
	/**
     * VOLUME
     */
	private BigDecimal volume;
	/**
     * VOLUME_OPEN
     */
	@ApiModelProperty("委托张数")
	private BigDecimal volumeOpen;
	/**
     * LEVER_RATE
     */
	@ApiModelProperty("杠杠")
	private BigDecimal leverRate;
	/**
     * PRICE
     */
	@ApiModelProperty("限价")
	private BigDecimal price;
	/**
     * STOP_PRICE_PROFIT
     */
	@ApiModelProperty("止盈价格")
	private BigDecimal stopPriceProfit;
	/**
     * STOP_PRICE_LOSS
     */
	@ApiModelProperty("止损价格")
	private BigDecimal stopPriceLoss;
	/**
     * ORDER_PRICE_TYPE
     */
	@ApiModelProperty("报价类型。 limit:限价 opponent:市价")
	private String orderPriceType;
	/**
     * STATE
     */
	@ApiModelProperty("状态。submitted 已提交，canceled 已撤销， created 委托完成")
	private String state;
	/**
     * UNIT_AMOUNT
     */
	private BigDecimal unitAmount;
	/**
     * FEE
     */
	private BigDecimal fee;
	/**
     * DEPOSIT
     */
	private BigDecimal deposit;
	/**
     * 创建时间
     */
	private Date createTime;
	/**
     * 更新时间
     */
	private Date updateTime;
	/**
     * 备注信息
     */
	private String remarks;

	@ApiModelProperty("止盈止损")
	private String stopProfitLoss;


	public String getStopProfitLoss() {
		if(stopPriceLoss == null){
			stopPriceProfit = BigDecimal.ZERO;
		}
		if(stopPriceLoss == null){
			stopPriceLoss = BigDecimal.ZERO;
		}
		return StrUtil.format("{}/{}",  stopPriceProfit, stopPriceLoss);
	}
}
