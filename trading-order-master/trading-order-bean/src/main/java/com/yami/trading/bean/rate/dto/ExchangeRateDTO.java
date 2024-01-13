package com.yami.trading.bean.rate.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 汇率管理DTO
 * @author lucas
 * @version 2023-03-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ExchangeRateDTO  {

	private static final long serialVersionUID = 1L;

	/**
     * 主键
     */
	private String uuid;
	/**
     * OUT_OR_IN
     */
	private String outOrIn;
	/**
     * RATA
     */
	@NotNull(message = "汇率不能为空")
	@DecimalMin(value = "0", message = "汇率不能小于0")
	private BigDecimal rata;
	/**
     * CURRENCY
     */
	private String currency;
	/**
     * NAME
     */
	private String name;
	/**
     * CURRENCY_SYMBOL
     */
	private String currencySymbol;

}
