package com.yami.trading.bean.c2c;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_c2c_advert")
public class C2cAdvert extends UUIDEntity {

    /**
     * 买入
     */
    public final static String DIRECTION_BUY = "buy";

    /**
     * 卖出
     */
    public final static String DIRECTION_SELL = "sell";

    /**
     * 承兑商ID
     */
    private String c2cUserId;

    /**
     * 买卖方式：buy买/sell卖
     */
    private String direction;

    /**
     * 支付币种
     */
    private String currency;

    /**
     * 上架币种
     */
    private String symbol;

    /**
     * 上架币种实时行情价
     */
    private double symbolClose;

    /**
     * 支付比率
     */
    private double payRate;

    /**
     * 支付方式模板id列表：不同id逗号分隔
     */
    private String payType;

    /**
     * 支付方式模板名称
     */
    @TableField(exist = false)
    private String payTypeName;

    /**
     * 币种单价
     */
    private double symbolValue;

    /**
     * 币种数量
     */
    private double coinAmount;

    /**
     * 单笔订单最低限额
     */
    private double investmentMin;

    /**
     * 单笔订单最高限额
     */
    private double investmentMax;

    /**
     * 剩余派单金额
     */
    private double deposit;

    /**
     * 派单金额
     */
    private double depositOpen;

    /**
     * 是否上架：0下架/1上架
     */
    private int onSale;

    /**
     * 是否关闭：0否/1是
     */
    private int closed;

    /**
     * 排序索引
     */
    private int sortIndex;

    /**
     * 支付时效（单位：分钟）
     */
    private int expireTime;

    /**
     * 交易条款
     */
    private String transactionTerms;

    /**
     * 订单自动消息
     */
    private String orderMsg;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
