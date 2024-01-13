package com.yami.trading.bean.data.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 币对DTO
 * @author lucas
 * @version 2023-03-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SymbolsDTO extends BaseDTO {

	private static final long serialVersionUID = 1L;

	/**
     * UUID
     */
	@NotNull(message="UUID不能为空")
	private String uuid;
	/**
     * SYMBOL
     */
	private String symbol;
	/**
     * BASE_CURRENCY
     */
	private String baseCurrency;
	/**
     * QUOTE_CURRENCY
     */
	private String quoteCurrency;
	/**
     * PRICE_PRECISION
     */
	private Long pricePrecision;
	/**
     * STATE
     */
	private String state;
	/**
     * LEVERAGE_RATIO
     */
	private BigDecimal leverageRatio;

}
