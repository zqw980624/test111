package com.yami.trading.bean.c2c;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import com.yami.trading.common.domain.UUIDEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Date;



@TableName("t_c2c_order")
@Data
public class C2cOrder extends UUIDEntity {
    private static final long serialVersionUID = -3219486331695837115L;

    /**
     * 订单类型：按支付金额
     */
    public final static String ORDER_TYPE_BY_AMOUNT = "by_amount";

    /**
     * 订单类型：按币种数量
     */
    public final static String ORDER_TYPE_BY_NUM = "by_num";

    /**
     * 买入
     */
    public final static String DIRECTION_BUY = "buy";

    /**
     * 卖出
     */
    public final static String DIRECTION_SELL = "sell";

    /**
     * 按金额支付
     */
    public final static String PAY_METHOD_CURRENCY = "currency";

    /**
     * 按币种数量支付
     */
    public final static String PAY_METHOD_COIN = "coin";

    /**
     * 用户PARTY_ID
     */
    private String partyId;

    /**
     * 承兑商ID
     */
    private String c2cUserId;

    /**
     * 广告ID
     */
    private String c2cAdvertId;

    /**
     * 支付方式ID：购买为承兑商收款方式ID，出售为用户收款方式ID
     */
    private String paymentMethodId;

    /**
     * 订单类型：by_amount按支付金额/by_num按币种数量
     */
    private String orderType;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单状态：0未付款/1已付款/2申诉中/3已完成/4已取消/5已超时
     */
    private String state;

    /**
     * 承兑商类型：1后台承兑商/2用户承兑商
     */
    private int c2cUserType;

    /**
     * 承兑商CODE
     */
    private String c2cUserCode;

    /**
     * 承兑商昵称
     */
    private String c2cUserNickName;

    /**
     * 承兑商头像
     */
    private String c2cUserHeadImg;

    /**
     * 承兑商用户PARTY_ID
     */
    private String c2cUserPartyId;

    /**
     * 承兑商用户CODE
     */
    private String c2cUserPartyCode;

    /**
     * 承兑商用户名
     */
    private String c2cUserPartyName;

    /**
     * 买卖方式：buy买/sell卖
     */
    private String direction;
    /**
     * 国家/地区
     */
    private  String nationality;
    /**
     * 付款凭证
     */
    private  String img;

    /**
     * 支付币种
     */
    private String currency;

    /**
     * 上架币种
     */
    private String symbol;

    /**
     * 支付比率
     */
    private double payRate;

    /**
     * 币种单价
     */
    private double symbolValue;

    /**
     * 币种数量
     */
    private double coinAmount;

    /**
     * 支付时效（单位：分钟）
     */
    private int expireTime;

    /**
     * 确认收款超时时间秒（单位：秒）
     */
    private int expireTimeRemain;

    /**
     * 超时自动取消时间秒（单位：秒）
     */
    private int autoCancelTimeRemain;

    /**
     * 支付金额
     */
    private double amount;

    /**
     * 换算成USDT金额
     */
    private double amountUsdt;

    /**
     * 支付方式类型：0其它/1银行卡/2虚拟货币/3微信/4支付宝/5PayPal/6西联汇款/7SWIFT国际汇款
     */
    private int methodType;

    /**
     * 支付方式类型名称
     */
    private String methodTypeName;

    /**
     * 支付方式名称
     */
    private String methodName;

    /**
     * 支付方式图片
     */
    private String methodImg;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 参数名1
     */
    private String paramName1;

    /**
     * 参数值1
     */
    private String paramValue1;

    /**
     * 参数名2
     */
    private String paramName2;

    /**
     * 参数值2
     */
    private String paramValue2;

    /**
     * 参数名3
     */
    private String paramName3;

    /**
     * 参数值3
     */
    private String paramValue3;

    /**
     * 参数名4
     */
    private String paramName4;

    /**
     * 参数值4
     */
    private String paramValue4;

    /**
     * 参数名5
     */
    private String paramName5;

    /**
     * 参数值5
     */
    private String paramValue5;

    /**
     * 参数名6
     */
    private String paramName6;

    /**
     * 参数值6
     */
    private String paramValue6;

    /**
     * 参数名7
     */
    private String paramName7;

    /**
     * 参数值7
     */
    private String paramValue7;

    /**
     * 参数名8
     */
    private String paramName8;

    /**
     * 参数值8
     */
    private String paramValue8;

    /**
     * 参数名9
     */

    private String paramName9;

    /**
     * 参数值9
     */
    private String paramValue9;

    /**
     * 参数名10
     */
    private String paramName10;

    /**
     * 参数值10
     */
    private String paramValue10;

    /**
     * 参数名11
     */
    private String paramName11;

    /**
     * 参数值11
     */
    private String paramValue11;

    /**
     * 参数名12
     */
    private String paramName12;

    /**
     * 参数值12
     */
    private String paramValue12;

    /**
     * 参数名13
     */
    private String paramName13;

    /**
     * 参数值13
     */
    private String paramValue13;

    /**
     * 参数名14
     */
    private String paramName14;

    /**
     * 参数值14
     */
    private String paramValue14;

    /**
     * 参数名15
     */
    private String paramName15;

    /**
     * 参数值15
     */
    private String paramValue15;

    /**
     * 支付二维码
     */
    private String qrcode;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 处理时间
     */
    private Date handleTime;

    /**
     * 关闭时间
     */
    private Date closeTime;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 取消时间
     */
    private Date cancelTime;

    private  double coinAmountFee;
}
