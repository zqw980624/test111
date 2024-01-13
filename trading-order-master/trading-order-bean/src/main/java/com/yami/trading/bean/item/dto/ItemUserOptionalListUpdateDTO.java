package com.yami.trading.bean.item.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

/**
 * 自选分组DTO
 * @author lucas
 * @version 2023-03-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ItemUserOptionalListUpdateDTO {

	private static final long serialVersionUID = 1L;

	/**
	 * NAME
	 */
	@NotEmpty
	@ApiModelProperty("自选组合id")
	private String uuid;
	/**
     * NAME
     */
	@NotEmpty
	@ApiModelProperty("自选组合名称")
	private String name;
	/**
	 * NAME
	 */
	@NotEmpty
	@ApiModelProperty("法币的简写")
	private String currency;

}
