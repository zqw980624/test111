package com.yami.trading.bean.future.query;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 交割合约管理DTO
 * @author lucas
 * @version 2023-04-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FuturesParaQuery {
	private static final long serialVersionUID = 1L;
	private String symbol;
}
