package com.yami.trading.bean.rate.query;

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
public class UserRateConfigQuery{

	private static final long serialVersionUID = 1L;

	/**
     * UUID
     */
	@NotNull(message="UUID不能为空")
	private String uuid;
	/**
     * OUT_OR_IN
     */
    @Query(type = QueryType.EQ)
	private String outOrIn;
	/**
     * CURRENCY
     */
    @Query(type = QueryType.EQ)
	private String currency;
	/**
     * PARTY_ID
     */
    @Query(type = QueryType.EQ)
	private String partyId;
   

}
