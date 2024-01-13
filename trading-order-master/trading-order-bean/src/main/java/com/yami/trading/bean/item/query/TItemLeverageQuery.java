package com.yami.trading.bean.item.query;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 产品杠杠倍数DTO
 * @author lucas
 * @version 2023-03-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TItemLeverageQuery{

	private static final long serialVersionUID = 1L;

	/**
     * ITEM_ID
     */
    @Query(tableColumn = "a.ITEM_ID", type = QueryType.EQ)
	private String itemId;


}
