package com.yami.trading.bean.syspara.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.yami.trading.common.domain.UUIDEntity;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 配置参数DTO
 * @author lucas
 * @version 2023-03-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysparaDTO  {

	private static final long serialVersionUID = 1L;

	@NotBlank
	private String code;
	//@NotBlank
	private String value;

}
