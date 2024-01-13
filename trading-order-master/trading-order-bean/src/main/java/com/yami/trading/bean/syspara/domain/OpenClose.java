package com.yami.trading.bean.syspara.domain;

import java.util.Date;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import com.yami.trading.common.domain.UUIDEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 开盘停盘时间设置Entity
 * @author lucas
 * @version 2023-05-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_open_close")
public class OpenClose extends UUIDEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * 代码
	 */
	@TableField(exist = false)
	@ApiModelProperty("名称")
	private String symbolName;
	/**
     * 代码
     */
     	@ApiModelProperty("代码")
	private String symbol;
	/**
     * 开盘时间，北京时间
     */
     	@ApiModelProperty("开盘时间，北京时间")
	private Date openBjDate;
	/**
     * 停盘时间，北京时间
     */
     	@ApiModelProperty("停盘时间，北京时间")
	private Date closeBjDate;
	private String startDate;

	private String endDate;
	private String flag;
	/**
     * 开盘时间戳
     */
     	@ApiModelProperty("开盘时间戳")
	private Long openTs;
	/**
     * 停盘时间戳
     */
     	@ApiModelProperty("停盘时间戳")
	private Long closeTs;

}
