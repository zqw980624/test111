package com.yami.trading.bean.robot.query;

import javax.validation.constraints.NotNull;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 机器人下单DTO
 * @author lucas
 * @version 2023-05-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RobotOrderQuery{

	private static final long serialVersionUID = 1L;

	/**
     * uuid
     */
	@Query(type = QueryType.EQ)
	private String uuid;
	/**
     * 用户id
     */
	@Query(type = QueryType.EQ)
	private String uid;
	/**
     * symbol
     */
    @Query(type = QueryType.LIKE)
	private String symbol;
	/**
     * 项目名称
     */
	private String projectName;
	/**
     * 挂单量(USDT)
     */
	private Double orderQuantity;
	/**
     * 成交量(USDT)
     */
	private Double turnover;
	/**
     * 1现价单 2市价单
     */
    @Query(type = QueryType.EQ)
	private Integer orderType;
	/**
     * 1买 2卖
     */
    @Query(type = QueryType.EQ)
	private Integer direction;
	/**
     * 挂单价格
     */
	private Double price;
	/**
     * 订单状态
     */
    @Query(type = QueryType.EQ)
	private Integer status;
	/**
     * ts
     */
	private Long ts;

	@ApiModelProperty(value = "time",notes = "1：当天，2：昨天，3：近7天，4：近15天，5：近30天")
	private Integer time = 1;




}
