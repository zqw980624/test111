package com.yami.trading.bean.rate.query;

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
public class ExchangeRateQuery{

	private static final long serialVersionUID = 1L;

	/**
     * 主键
     */
	@NotNull(message="主键不能为空")
	private String uuid;
	/**
     * OUT_OR_IN
     */
    @Query(type = QueryType.EQ)
	private String outOrIn;
	/**
     * RATA
     */
	private BigDecimal rata;
	/**
     * CURRENCY
     */
    @Query(type = QueryType.EQ)
	private String currency;
	/**
     * NAME
     */
    @Query(type = QueryType.LIKE)
	private String name;
	/**
     * CURRENCY_SYMBOL
     */
    @Query(type = QueryType.EQ)
	private String currencySymbol;
   

}
