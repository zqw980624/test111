package com.yami.trading.bean.robot.query;

import javax.validation.constraints.NotNull;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 下单机器人DTO
 * @author lucas
 * @version 2023-05-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RobotQuery{

	private static final long serialVersionUID = 1L;

	/**
     * 交易对
     */
    @Query(type = QueryType.EQ)
	private String symbol;

	/**
     * 状态 1 正常 0 无效
     */
    @Query(type = QueryType.EQ)
	private Integer status;
}
