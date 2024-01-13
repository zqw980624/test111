package com.yami.trading.bean.item.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 自选分组DTO
 * @author lucas
 * @version 2023-03-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ItemUserOptionalListDTO {

	private static final long serialVersionUID = 1L;


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
