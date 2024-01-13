package com.yami.trading.bean.item.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 用户自选DTO
 * @author lucas
 * @version 2023-03-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@ApiModel
public class ItemUserOptionalDTO {

	private static final long serialVersionUID = 1L;


	/**
     * PARTY_ID
     */
	private String partyId;
	/**
     * SYMBOL
     */
	@ApiModelProperty("币对")
	private String symbol;

	/**
	 * SYMBOL
	 */
	@ApiModelProperty("币对名词")
	private String name;
	/**
     * 创建时间
     */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	/**
     * 更新时间
     */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;
	@ApiModelProperty("法币币种")
	private String currency;

	/**
	 * 备注信息
	 */
	private String remarks;

	/**
	 * 量比
	 */
	@JSONField(name = "volume_ratio")
	private BigDecimal volumeRatio ;

	/**
	 * 量比
	 */
	@JSONField(name = "change_ratio")
	private BigDecimal changeRatio ;

	/**
	 * 换手率
	 */
	@JSONField(name = "turnover_rate")
	private BigDecimal turnoverRate ;

	/**
	 * 换手率
	 */
	@JSONField(name = "turnover_rate")
	private String turnoverRates ;
	/**
	 * 最新价
	 */
	@ApiModelProperty("最新价")
	private BigDecimal close;

	private String pid;

}
