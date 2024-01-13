package com.yami.trading.bean.c2c;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_c2c_user")
public class C2cUser  extends UUIDEntity {

    /**
     * C2C管理员PARTY_ID
     */
    private String c2cManagerPartyId;

    /**
     * 1后台承兑商/2用户承兑商
     */
    private int c2cUserType;

    /**
     * 承兑商CODE
     */
    private String c2cUserCode;

    /**
     * 承兑商PARTY_ID
     */
    private String c2cUserPartyId;

    /**
     * 承兑商昵称
     */
    private String nickName;

    /**
     * 承兑商头像
     */
    private String headImg;

    /**
     * 剩余保证金
     */
    private double deposit;

    /**
     * 保证金
     */
    private double depositOpen;

    /**
     * 保证金赠送比率
     */
    private double depositGiftRate;

    /**
     * 30日成单数
     */
    private int thirtyDaysOrder;

    /**
     * 30日成单率
     */
    private double thirtyDaysOrderRatio;

    /**
     * 30日平均放行时间
     */
    private int thirtyDaysPassAverageTime;

    /**
     * 30日平均付款时间
     */
    private int thirtyDaysPayAverageTime;

    /**
     * 30日交易量
     */
    private double thirtyDaysAmount;

    /**
     * 买交易量
     */
    private double buyAmount;

    /**
     * 卖交易量
     */
    private double sellAmount;

    /**
     * 总交易量
     */
    private double totalAmount;

    /**
     * 账号创建天数
     */
    private int accountCreateDays;

    /**
     * 首次交易至今天数
     */
    private int firstExchangeDays;

    /**
     * 交易人数
     */
    private int exchangeUsers;

    /**
     * 买成单数
     */
    private int buySuccessOrders;

    /**
     * 卖成单数
     */
    private int sellSuccessOrders;

    /**
     * 总成单数
     */
    private int totalSuccessOrders;

    /**
     * 好评数
     */
    private int appraiseGood;

    /**
     * 差评数
     */
    private int appraiseBad;

    /**
     * 订单邮件通知：0关闭/1打开
     */
    private int orderMailNoticeOpen;

    /**
     * 订单短信通知：0关闭/1打开
     */
    private int orderSmsNoticeOpen;

    /**
     * 订单APP通知：0关闭/1打开
     */
    private int orderAppNoticeOpen;

    /**
     * 申诉邮件通知：0关闭/1打开
     */
    private int appealMailNoticeOpen;

    /**
     * 申诉短信通知：0关闭/1打开
     */
    private int appealSmsNoticeOpen;

    /**
     * 申诉APP通知：0关闭/1打开
     */
    private int appealAppNoticeOpen;

    /**
     * 聊天APP通知：0关闭/1打开
     */
    private int chatAppNoticeOpen;

    /**
     * 安全邮件通知：0关闭/1打开
     */
    private int securityMailNoticeOpen;

    /**
     * 安全短信通知：0关闭/1打开
     */
    private int securitySmsNoticeOpen;

    /**
     * 安全APP通知：0关闭/1打开
     */
    private int securityAppNoticeOpen;

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
