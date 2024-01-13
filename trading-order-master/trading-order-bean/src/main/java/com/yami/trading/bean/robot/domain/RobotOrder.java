package com.yami.trading.bean.robot.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import com.yami.trading.common.domain.UUIDEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 机器人下单Entity
 *
 * @author lucas
 * @version 2023-05-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_robot_order")
public class RobotOrder extends UUIDEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    @ApiModelProperty("用户id")
    private String uid;
    /**
     * symbol
     */
    @ApiModelProperty("symbol")
    private String symbol;
    /**
     * 委托量
     */
    @ApiModelProperty("委托量")
    private String projectName;
    /**
     * 挂单量(USDT)
     */
    @ApiModelProperty("挂单量(USDT)")
    private Double orderQuantity;
    /**
     * 成交量(USDT)
     */
    @ApiModelProperty("成交量(USDT)")
    private Double turnover;
    /**
     * 1现价单 2市价单
     */
    @ApiModelProperty("1现价单 2市价单")
    private Integer orderType;
    /**
     * 1买 2卖
     */
    @ApiModelProperty("1买 2卖")
    private Integer direction;
    /**
     * 挂单价格
     */
    @ApiModelProperty("挂单价格")
    private Double price;
    /**
     * 订单状态
     */
    @ApiModelProperty("订单状态 1 挂单中 2已成交")
    private Integer status;
    /**
     * ts
     */
    @ApiModelProperty("ts")
    private Long ts;

}
