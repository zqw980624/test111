package com.yami.trading.bean.rate.dto;

import javax.validation.constraints.NotNull;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 用户汇率管理DTO
 * @author lucas
 * @version 2023-03-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserRateConfigDTO {

	private static final long serialVersionUID = 1L;

	/**
     * UUID
     */
	@NotNull(message="UUID不能为空")
	private String uuid;
	/**
     * OUT_OR_IN
     */
	private String outOrIn;
	/**
     * CURRENCY
     */
	private String currency;
	/**
     * PARTY_ID
     */
	private String partyId;

}
