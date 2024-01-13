package com.yami.trading.bean.future.query;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 交割合约订单DTO
 * @author lucas
 * @version 2023-04-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FuturesOrderQuery {
	@ApiModelProperty("uid")
	private String userCode;
	private String userId;
	private String roleName;
	private String userName;
	/**
     * 代码
     */
	private String symbol;
	/**
     * ORDER_NO
     */
	private String orderNo;
	/**
     * DIRECTION
     */
	private String direction;

	private String tradeAvgPrice;

	private BigDecimal volume;

	/**
     * STATE
     */
	private String state;
	private String ip;

	private List<String> children;
	@ApiModelProperty("forex->外汇,commodities->大宗商品，指数/ETF->indices,  A-stocks->A股,  HK-stocks->港股.US-stocks->美股，cryptos->虚拟货币 ")
	private String type;

}
