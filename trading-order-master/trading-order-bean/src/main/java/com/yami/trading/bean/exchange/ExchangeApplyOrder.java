package com.yami.trading.bean.exchange;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
@TableName("t_exchange_apply_order")
public class ExchangeApplyOrder extends UUIDEntity {


    private static final long serialVersionUID = -7263336511778693149L;
    public final static String STATE_SUBMITTED = "submitted";
    public final static String STATE_CREATED = "created";
    public final static String STATE_CANCELED = "canceled";

    /**
     * 开仓
     */
    public final static String OFFSET_OPEN = "open";

    /**
     * 平仓
     */
    public final static String OFFSET_CLOSE = "close";

    /**
     * 限价单
     */
    public final static String ORDER_PRICE_TYPE_LIMIT = "limit";

    /**
     * 对手价（市价）
     */
    public final static String ORDER_PRICE_TYPE_OPPONENT = "opponent";

    private String partyId;


    /**
     * 订单 号
     */
    private String relationOrderNo;

    /**
     * 订单 号
     */
    private String orderNo;

    private String symbol;
    private String pid;
    /**
     * 币种数量
     */
    private Double symbolValue;

    /**
     * "open":买入 "close":卖出
     */
    @TableField(value = "off_set")
    private String offset;
    /**
     * 委托数量
     */
    private Double volume;

    /**
     * 手续费
     */
    private double fee;

    /**
     * 金额（USDT计价）
     */
    private Double amount;

    /**
     * 手续费(USDT计价)
     */
    private Double walletFee;

    /**
     * limit order的交易价格
     */
    private Double price;

    /**
     * 订单报价类型。 "limit":限价 "opponent":对手价（市价）
     */
    private String orderPriceType;
    /**
     * 状态。submitted 已提交，canceled 已撤销， created 委托完成
     */
    private String state = "submitted";
    /**
     * 创建时间
     */

   // @JsonFormat(locale = "zh", timezone = "GMT+5", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 成交时行情点位
     */
    private Double closePrice;
    /**
     * 成交时间
     */
    private Date closeTime;
    /**
     * 是否计划委托
     */
    private boolean isTriggerOrder = false;
    /**
     * 计划委托的触发价
     */
    private Double triggerPrice;

    /**
     * 创建日期
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createTimeTs;

    /**
     * 更新日期
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateTimeTs;
}
