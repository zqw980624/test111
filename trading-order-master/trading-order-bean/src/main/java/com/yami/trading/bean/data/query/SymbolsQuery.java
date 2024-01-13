package com.yami.trading.bean.data.query;

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
public class SymbolsQuery{

	private static final long serialVersionUID = 1L;

	/**
     * UUID
     */
	@NotNull(message="UUID不能为空")
	private String uuid;
	/**
     * SYMBOL
     */
    @Query(type = QueryType.EQ)
	private String symbol;
	/**
     * BASE_CURRENCY
     */
    @Query(type = QueryType.EQ)
	private String baseCurrency;
	/**
     * QUOTE_CURRENCY
     */
    @Query(type = QueryType.EQ)
	private String quoteCurrency;
	/**
     * PRICE_PRECISION
     */
    @Query(type = QueryType.EQ)
	private Long pricePrecision;
	/**
     * STATE
     */
    @Query(type = QueryType.EQ)
	private String state;
	/**
     * LEVERAGE_RATIO
     */
    @Query(type = QueryType.EQ)
	private BigDecimal leverageRatio;
   

}
