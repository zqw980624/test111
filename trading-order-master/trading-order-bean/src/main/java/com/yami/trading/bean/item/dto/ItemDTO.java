package com.yami.trading.bean.item.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.dto.BaseDTO;
import com.yami.trading.common.lang.LangUtils;
import com.yami.trading.common.util.StringUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 永续合约
 * @author lucas
 * @version 2023-03-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ItemDTO {

	private static final long serialVersionUID = 1L;

	private String uuid;

	/**
	 * 币种全称
	 */
	@ApiModelProperty("币种")
	private String symbolFullName;

	/**
	 * 币种名称
	 */
	//@NotBlank(message = "名称不能为空")
	@ApiModelProperty("交易对名称")
	private String name;
	@ApiModelProperty("英文名")
	private String enName;
	/**
	 * 代码
	 */
	@ApiModelProperty("代码/交易对")
//	@NotBlank(message = "代码不能为空")
	private String symbol;
	/**
	 * 数据源编码
	 */
	private String symbolData;
	/**
	 * 小数位精度
	 */
//	@NotNull
//	@Min(value = 0, message = "精度必须大于等于0")
	@ApiModelProperty("价格精度")
	private Integer decimals;
	/**
	 * 最小浮动
	 */
	//@NotNull(message = "最小浮动")
	//@DecimalMin(value = "0", message = "最小浮动不能小于0")
	@ApiModelProperty("最小浮动/最小交易量")
	private BigDecimal pips;


	/**
	 * 最小浮动金额
	 */
	//@NotNull(message = "最小浮动金额必填")
	@ApiModelProperty("最小浮动金额/最小交易额/最小挂单金额")
	//@DecimalMin(value = "0", message = "最小浮动金额不能小于0")
	private BigDecimal pipsAmount;


	/**
	 * 每手金额
	 */
	//@NotNull(message = "每手金额必填")
//	@DecimalMin(value = "0", message = "每手金额不能小于0")
	@ApiModelProperty("每手金额")
	private BigDecimal unitAmount;


	/**
	 * 每手的手续费
	 */
	//@NotNull(message = "每手的手续费必填")
//	@DecimalMin(value = "0", message = "每手的手续费必填")
	@ApiModelProperty("每手的手续费")
	private Double unitFee;

//	@NotBlank
//	private String loginSafeword;

	/**
	 * 交易量倍数
	 */
	@ApiModelProperty("交易倍数")
	private BigDecimal multiple;
	/**
	 * 借贷利率
	 */
	@ApiModelProperty("借贷利率")
	private BigDecimal borrowingRate;

	private List<ItemLeverageDTO> levels;
	@ApiModelProperty("是否置顶")
	private String isTop;

	@ApiModelProperty("类型/交易模块")
	private String type;

	@ApiModelProperty("最小下单价")
	private String minimumPrice;

	@ApiModelProperty("最大下单价/最高买单价")
	private String maxmumPrice;

	@ApiModelProperty("最小下单量")
	private String minimumOrder;

	@ApiModelProperty("最大下单量")
	private String maxmumOrder;

	@ApiModelProperty("排序")
	private String sorted;
	@ApiModelProperty("报价货币")
	private String quoteCurrency;

	@ApiModelProperty("前端显示状态，1显示，0不显示")
	private String showStatus;
	@ApiModelProperty("交易状态，1显示，0不显示")
	private String tradeStatus;
	@ApiModelProperty("创建时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private String createTime;
	@ApiModelProperty("状态，1启用，0禁止")
	private String enable;
	@ApiModelProperty("市价买，1是，0否")
	private String canBuyAtMarketPrice;
	@ApiModelProperty("市价卖，1是，0否")
	private String canSellAtMarketPrice;
	@ApiModelProperty("限价可买，1是，0否")
	private String limitCanBuy;
	@ApiModelProperty("限价可卖，1是，0否")
	private String limitCanSell;

	public void transName(){
		if(StringUtils.isNotEmpty(enName)){
			this.name = enName;
		}
	}
	public String getName() {
		if (LangUtils.isEnItem() && StringUtils.isNotEmpty(enName)) {
			this.name = enName;
		}
		return name;


	}

}
