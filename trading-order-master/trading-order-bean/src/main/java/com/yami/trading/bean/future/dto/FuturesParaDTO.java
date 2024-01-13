package com.yami.trading.bean.future.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 交割合约管理DTO
 * @author lucas
 * @version 2023-04-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FuturesParaDTO{

	private String uuid;

	@NotNull(message = "请选择合约代码")
	private String symbol;

	@NotNull(message = "时间必填")
	@DecimalMin(value = "1", message = "时间不能小于等于0")
	private Integer timenum;
	/**
     * TIMEUNIT
     */
	@NotNull(message = "请选择时间单位")
	private String timeunit;
	/**
     * UNIT_AMOUNT
     */
	@NotNull(message = "每手金额必填")
	@DecimalMin(value = "0.0001",  message = "每手金额不能小于等于0")
	private BigDecimal unitAmount;
	/**
     * UNIT_MAX_AMOUNT
     */
	//@NotNull(message = "最高购买金额必填")
	//@DecimalMin(value = "0.0001",  message = "最高购买金额不是浮点数")
	private BigDecimal unitMaxAmount;
	/**
     * PROFIT_RATIO
     */
	@NotNull(message = "最小收益率不是浮点数")
	@DecimalMin(value = "0.00001",  message = "最小收益率不能小于等于0")
	private BigDecimal profitRatio;
	/**
     * UNIT_FEE
     */
	private BigDecimal unitFee;
	/**
     * PROFIT_RATIO_MAX
     */
	@NotNull(message = "最大收益率必填")
	@DecimalMin(value = "0.00001",  message = "最大收益率不能小于等于0")
	private BigDecimal profitRatioMax;

	@NotNull(message = "资金密码")
	private String loginSafeword;

	public void mutiply(){
		this.setProfitRatio(profitRatio.multiply(BigDecimal.valueOf(100)));
		this.setProfitRatioMax(profitRatioMax.multiply(BigDecimal.valueOf(100)));
		this.setUnitFee(unitFee.multiply(BigDecimal.valueOf(100)));
	}


	public void divide(){
		this.setProfitRatio(profitRatio.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
		this.setProfitRatioMax(profitRatioMax.divide(BigDecimal.valueOf(100),2, RoundingMode.HALF_UP));
		this.setUnitFee(unitFee.divide(BigDecimal.valueOf(100),2, RoundingMode.HALF_UP));
	}

}
