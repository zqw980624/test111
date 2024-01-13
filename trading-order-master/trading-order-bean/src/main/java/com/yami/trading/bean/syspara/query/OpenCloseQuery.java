package com.yami.trading.bean.syspara.query;

import javax.validation.constraints.NotNull;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.query.Query;
import com.yami.trading.common.query.QueryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 开盘停盘时间设置DTO
 * @author lucas
 * @version 2023-05-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OpenCloseQuery{

	private static final long serialVersionUID = 1L;

	/**
     * UUID
     */
	@NotNull(message="UUID不能为空")
	private String uuid;
	/**
     * 代码
     */
	@NotNull(message="代码不能为空")
    @Query(type = QueryType.EQ)
	private String symbol;
	/**
     * 开盘时间，北京时间
     */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="开盘时间，北京时间不能为空")
	private Date openBjDate;
	/**
     * 停盘时间，北京时间
     */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="停盘时间，北京时间不能为空")
	private Date closeBjDate;
	/**
     * 开盘时间戳
     */
	@NotNull(message="开盘时间戳不能为空")
	private Long openTs;
	/**
     * 停盘时间戳
     */
	@NotNull(message="停盘时间戳不能为空")
	private Long closeTs;
   

}
