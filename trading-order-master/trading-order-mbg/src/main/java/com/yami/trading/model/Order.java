package com.yami.trading.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 订单表
 * </p>
 *
 * @author HT
 * @since 2023-02-25 18:59:24
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("tz_order")
public class Order {

    /**
     * 订单ID
     */
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    /**
     * 店铺id
     */
    private Long shopId;

    /**
     * 产品名称,多个产品将会以逗号隔开
     */
    private String prodName;

    /**
     * 订购用户ID
     */
    private String userId;

    /**
     * 订购流水号
     */
    private String orderNumber;

    /**
     * 总值
     */
    private BigDecimal total;

    /**
     * 实际总值
     */
    private BigDecimal actualTotal;

    /**
     * 支付方式 0 手动代付 1 微信支付 2 支付宝
     */
    private Integer payType;

    /**
     * 订单备注
     */
    private String remarks;

    /**
     * 订单状态 1:待付款 2:待发货 3:待收货 4:待评价 5:成功 6:失败
     */
    @TableField("`status`")
    private Integer status;

    /**
     * 配送类型
     */
    private String dvyType;

    /**
     * 配送方式ID
     */
    private Long dvyId;

    /**
     * 物流单号
     */
    private String dvyFlowId;

    /**
     * 订单运费
     */
    private BigDecimal freightAmount;

    /**
     * 用户订单地址Id
     */
    private Long addrOrderId;

    /**
     * 订单商品总数
     */
    private Integer productNums;

    /**
     * 订购时间
     */
    private LocalDateTime createTime;

    /**
     * 订单更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 付款时间
     */
    private LocalDateTime payTime;

    /**
     * 发货时间
     */
    private LocalDateTime dvyTime;

    /**
     * 完成时间
     */
    private LocalDateTime finallyTime;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 是否已经支付，1：已经支付过，0：，没有支付过
     */
    private Boolean isPayed;

    /**
     * 用户订单删除状态，0：没有删除， 1：回收站， 2：永久删除
     */
    private Integer deleteStatus;

    /**
     * 0:默认,1:在处理,2:处理完成
     */
    private Integer refundSts;

    /**
     * 优惠总额
     */
    private BigDecimal reduceAmount;

    /**
     * 订单类型
     */
    private Integer orderType;

    /**
     * 订单关闭原因 1-超时未支付 2-退款关闭 4-买家取消 15-已通过货到付款交易
     */
    private Integer closeType;


}
