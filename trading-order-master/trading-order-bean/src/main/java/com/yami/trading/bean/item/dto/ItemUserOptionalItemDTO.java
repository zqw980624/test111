package com.yami.trading.bean.item.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 自选组产品DTO
 * @author lucas
 * @version 2023-03-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ItemUserOptionalItemDTO extends BaseDTO {

	private static final long serialVersionUID = 1L;


	/**
     * PARTY_ID
     */
	private String partyId;
	/**
     * SYMBOL
     */
	private String symbol;
	/**
     * LIST_ID
     */
	private String listId;
	private String name;
	@ApiModelProperty("法币币种")
	private String currency;
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
	 * 最新价
	 */
	@ApiModelProperty("最新价")
	private BigDecimal close;

}
