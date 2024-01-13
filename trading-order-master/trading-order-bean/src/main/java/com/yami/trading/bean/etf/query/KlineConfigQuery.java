package com.yami.trading.bean.etf.query;

import javax.validation.constraints.NotNull;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * etfK线图配置表DTO
 * @author lucas
 * @version 2023-05-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class KlineConfigQuery{

	private static final long serialVersionUID = 1L;
	/**
     * 交易对
     */
    @Query(type = QueryType.EQ)
	private String symbol;

}
