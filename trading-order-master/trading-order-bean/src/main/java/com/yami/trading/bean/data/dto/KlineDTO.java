package com.yami.trading.bean.data.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * k线图数据DTO
 * @author lucas
 * @version 2023-03-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class KlineDTO extends BaseDTO {

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
     * TS
     */
	private Long ts;
	/**
     * OPEN
     */
	private BigDecimal open;
	/**
     * HIGH
     */
	private BigDecimal high;
	/**
     * LOW
     */
	private BigDecimal low;
	/**
     * CLOSE
     */
	private BigDecimal close;
	/**
     * AMOUNT
     */
	private BigDecimal amount;
	/**
     * VOLUME
     */
	private BigDecimal volume;
	/**
     * PERIOD
     */
	private BigDecimal period;

}
