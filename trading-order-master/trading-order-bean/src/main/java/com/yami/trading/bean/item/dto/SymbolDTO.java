package com.yami.trading.bean.item.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 永续合约
 * @author lucas
 * @version 2023-03-10
 */
@Data
@ApiModel
@EqualsAndHashCode(callSuper = false)
public class SymbolDTO {

	private static final long serialVersionUID = 1L;

	private String uuid;

	/**
	 * 币种全称
	 */
	private String symbolFullName;

	/**
	 * 币种名称
	 */
	//@NotBlank(message = "名称不能为空")
	@ApiModelProperty("币种名称")
	private String name;

	private String enName;
	/**
	 * 代码
	 */
	@ApiModelProperty("代码")
	//@NotBlank(message = "代码不能为空")
	private String symbol;
	/**
	 * 数据源编码
	 */
	private String symbolData;
	/**
	 * 小数位精度
	 */
	//@NotNull
	@Min(value = 0, message = "精度必须大于等于0")
	@ApiModelProperty("精度")
	private Integer decimals;

	private List<ItemLeverageDTO> levels;
	@ApiModelProperty("是否置顶")
	private String isTop;

	@ApiModelProperty("类型")
	private String type;

	@ApiModelProperty("类别")
	private String category;

	/**
	 * 成交金额
	 */
	@ApiModelProperty("成交金额")
	private BigDecimal amount;
	/**
	 * 成交额量
	 */
	@ApiModelProperty("成交量")
	private BigDecimal volume;


	/**
	 * 涨跌幅
	 */
	@TableField(exist = false)
	@ApiModelProperty("涨跌幅")
	private BigDecimal changeRatio;

	/**
	 * 涨跌率
	 */
	@TableField(exist = false)
	@ApiModelProperty("涨跌率")
	private BigDecimal chg;
	/**
	 * 涨跌率
	 */
	@TableField(exist = false)
	@ApiModelProperty("涨跌率")
	private String chgs;
	/**
	 * 时间戳
	 */
	@ApiModelProperty("时间戳")
	@TableField(exist = false)
	private Long ts;
	@ApiModelProperty("时间戳")
	@TableField(exist = false)
	private String tss;
	/**
	 * 时间戳
	 */
	@ApiModelProperty("时间戳")
	@TableField(exist = false)
	private Long current_time;
	/**
	 * 最新价
	 */
	@ApiModelProperty("最新价")
	@TableField(exist = false)
	private BigDecimal close;

	/**
	 * 最高
	 */
	@ApiModelProperty("最高")
	@TableField(exist = false)
	private BigDecimal High;

	/**
	 * 股票ID
	 */
	@ApiModelProperty("股票ID")
	@TableField(exist = false)
	private String Id;

	/**
	 * 最低价格
	 */
	@ApiModelProperty("最低价格")
	@TableField(exist = false)
	private BigDecimal Low;

	/**
	 * 今开
	 */
	@ApiModelProperty("今开")
	@TableField(exist = false)
	private BigDecimal open;

	/**
	 * 昨收
	 */
	@ApiModelProperty("昨收")
	@TableField(exist = false)
	private BigDecimal PrevClose;


}
