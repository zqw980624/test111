package com.yami.trading.bean.item.query;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.annotations.Param;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 产品DTO
 * @author lucas
 * @version 2023-03-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ItemQuery{

	private static final long serialVersionUID = 1L;

	/**
	 * name
	 */
	@ApiParam(value = "name")
	@Query(type = QueryType.LIKE)
	private String name;

	/**
	 * 代码
	 */
	@ApiParam(value = "symbol")
	@Query(type = QueryType.LIKE)
	private String symbol;

	/**
	 * 市场
	 */
	@ApiParam(value = "币对类型，外汇盘显示外汇，大宗商品，指数，虚拟货币  ,forex->外汇,commodities->大宗商品，指数->indices,  A-stocks->A股,  HK-stocks->港股.US-stocks->美股，cryptos->虚拟货币 ")
	@Query(type = QueryType.LIKE)
	private String type;

	/**
	 * etf分类
	 */
	@ApiParam(value = "global->全球ETF,gold->黄金ETF,ai->人工智能ETF,energy->能源ETF")
	@Query(type = QueryType.LIKE)
	private String category;

	private String boardType;


	@ApiModelProperty("前端显示状态，1显示，0不显示")
	@Query(type = QueryType.EQ)
	private String showStatus;
	@ApiModelProperty("交易状态，1显示，0不显示")
	@Query(type = QueryType.EQ)
	private String tradeStatus;
	@ApiModelProperty("报价货币")
	@Query(type = QueryType.EQ)
	private String quoteCurrency;

	@ApiModelProperty("涨帖查询 in-涨 on-贴")
	@Query(type = QueryType.EQ)
	private String scene;

	private Integer pageNum;

	private Integer pageSize;


}
