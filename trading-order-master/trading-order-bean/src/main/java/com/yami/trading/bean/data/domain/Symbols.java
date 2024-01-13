package com.yami.trading.bean.data.domain;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 币对Entity
 * @author lucas
 * @version 2023-03-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_symbols")
public class Symbols extends UUIDEntity {

	private static final long serialVersionUID = 1L;
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
	private Integer pricePrecision;
	/**
     * STATE
     */
	private String state;
	/**
     * LEVERAGE_RATIO
     */
	private BigDecimal leverageRatio;

}
