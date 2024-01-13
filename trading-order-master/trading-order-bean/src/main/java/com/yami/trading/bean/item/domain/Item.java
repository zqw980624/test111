package com.yami.trading.bean.item.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.google.common.collect.Lists;
import com.yami.trading.common.domain.BaseEntity;
import com.yami.trading.common.lang.LangUtils;
import com.yami.trading.common.util.StringUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

/**
 * 产品Entity
 *
 * @author lucas
 * @version 2023-03-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_item")
public class Item extends BaseEntity {

    /**
     * 外汇
     */
    public final static String forex = "forex";
    /**
     * 指数
     */
    public final static String indices = "indices";

    /**
     * 大宗商品
     */
    public final static String commodities = "commodities";

    /**
     * 虚拟货币
     */
    public final static String cryptos = "cryptos";

    /**
     * 英股
     */
    public final static String EN_STOCKS = "EN-stocks";
    /**
     * 美股
     */
    public final static String US_STOCKS = "US-stocks";

    /**
     * 港股
     */
    public final static String HK_STOCKS = "HK-stocks";
    /**
     * 印股
     */
    public final static String YD_STOCKS = "YD-stocks";

    /**
     * A股
     */
    public final static String A_STOCKS = "A-stocks";


    /**
     * 全球ETF
     */
    public final static String CATEGORY_GLOBAL = "global";


    /**
     * 黄金ETF
     */
    public final static String CATEGORY_GOLD = "gold";

    /**
     * 人工智能ETF
     */
    public final static String CATEGORY_AI = "ai";

    /**
     * 能源ETF
     */
    public final static String CATEGORY_ENERGY = "energy";


    private static final long serialVersionUID = 1L;


    public static List<String> types = Lists.newArrayList(forex, commodities, cryptos, indices, US_STOCKS, HK_STOCKS, A_STOCKS,YD_STOCKS);
    /**
     * 币种名称
     */
    //@NotBlank
    private String name;
    private String enName;
    /**
     * 代码
     */
    @NotBlank
    private String symbol;
    /**
     * 数据源编码
     */
    private String symbolData;
    /**
     * 最小浮动
     */
    private BigDecimal pips;
    /**
     * 最小浮动金额（以交易金额计算）
     */
    private BigDecimal pipsAmount;
    /**
     * ADJUSTMENT_VALUE
     */
    private BigDecimal adjustmentValue;
    /**
     * 每张金额
     */
    private BigDecimal unitAmount;
    /**
     * 每张手续费
     */
    private BigDecimal unitFee;
    /**
     * 市场
     */
    private String market;
    /**
     * 小数位精度
     */
    private Integer decimals;
    /**
     * 交易量倍数
     */
    private BigDecimal multiple;
    /**
     * 借贷利率
     */
    private BigDecimal borrowingRate;
    /**
     * 币种全称
     */
    private String symbolFullName;

    private String type;
    private String category;


    private String openCloseType;
    private String fake;

    private String pid;//第三方id

    public BigDecimal getAdjustmentValue() {
        if (adjustmentValue == null) {
            return BigDecimal.ZERO;
        }
        return adjustmentValue;
    }

    /**
     * 板块
     */
    private String board;

    private String sorted;

    @ApiModelProperty("报价货币")
    private String quoteCurrency;

    @ApiModelProperty("前端显示状态，1显示，0不显示")
    private String showStatus;
    @ApiModelProperty("交易状态，1显示，0不显示")
    private String tradeStatus;

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
    @TableField(exist = false)
    private boolean isOpen;

    public String getName() {
        if (LangUtils.isEnItem() && StringUtils.isNotEmpty(enName)) {
            this.name = enName;
        }
        return name;


    }

    public void transName() {
        if (StringUtils.isNotEmpty(enName)) {
            this.name = enName;
        }
    }

    public BigDecimal getUnitAmount() {
        return unitAmount;
    }

    public void setUnitAmount(BigDecimal unitAmount) {
        this.unitAmount = unitAmount;
    }
}
