package com.yami.trading.bean.contract.query;

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
 * 非按金额订单DTO
 * @author lucas
 * @version 2023-03-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ContractOrderQuery{

	@ApiModelProperty("uid")
	private String userCode;

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("用户id")
	private String userId;
	@ApiModelProperty("ip")
	private String ip;
	@ApiModelProperty("用户名")
	private String userName;

	@ApiModelProperty("用户角色")
	private String roleName;
	/**
	 * ORDER_NO
	 */
	@ApiModelProperty("订单号")
	private String orderNo;


	@ApiModelProperty("submitted已经提交，canceled已经撤销,created 已经完成")
	private String state;
	@ApiModelProperty("开始时间, 2022-01-01 12:33:00")
	private String startTime;
	@ApiModelProperty("结束时间, 2022-01-01 12:33:00")
	private String endTime;
	@ApiModelProperty("forex->外汇,commodities->大宗商品，指数/ETF->indices,  A-stocks->A股,  HK-stocks->港股.US-stocks->美股，cryptos->虚拟货币 ")
	private String type;
	private List<String> children;



}
