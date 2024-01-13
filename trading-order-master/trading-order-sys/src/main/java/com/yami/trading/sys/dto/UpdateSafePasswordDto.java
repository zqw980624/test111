/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */

package com.yami.trading.sys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@ApiModel()
public class UpdateSafePasswordDto {

	@ApiModelProperty("用户id")
	private long id;

	@NotBlank(message="旧资金密码不能为空")
	@Size(max = 50)
	@ApiModelProperty(value = "旧资金密码",required=true)
	private String safePassword;

	@NotBlank(message="新资金密码不能为空")
	@Size(max = 50)
	@ApiModelProperty(value = "新资金密码",required=true)
	private String newSafePassword;
}
