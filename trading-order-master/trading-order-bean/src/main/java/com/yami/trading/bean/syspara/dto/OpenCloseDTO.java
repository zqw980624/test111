package com.yami.trading.bean.syspara.dto;

import javax.validation.constraints.NotNull;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.domain.UUIDEntity;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 开盘停盘时间设置DTO
 * @author lucas
 * @version 2023-05-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OpenCloseDTO extends UUIDEntity {

	private static final long serialVersionUID = 1L;

	/**
     * UUID
     */
     	@ApiModelProperty("UUID,新增时候为空，修改不为空")
	private String uuid;
	/**
     * 代码
     */
	@NotNull(message="代码不能为空")
     	@ApiModelProperty("代码")
	private String symbol;
	/**
     * 开盘时间，北京时间
     */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="开盘时间，北京时间不能为空")
     	@ApiModelProperty("开盘时间，北京时间")
	private Date openBjDate;
	/**
     * 停盘时间，北京时间
     */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="停盘时间，北京时间不能为空")
     	@ApiModelProperty("停盘时间，北京时间")
	private Date closeBjDate;
	private String flag;
	private String startDate;

	private String endDate;
}
