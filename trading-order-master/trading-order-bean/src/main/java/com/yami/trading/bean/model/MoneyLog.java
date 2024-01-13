package com.yami.trading.bean.model;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("t_money_log")
public class MoneyLog extends BaseEntity {
    private static final long serialVersionUID = -5914896022101327097L;
    /**
     * 账号类型
     */
    private String walletType;
    /**
     * 账变币对
     */
    private String symbol;
    @TableField(exist = false)
    private  String wallet_type;
    /**
     * 交易类型
     */
    private String category;
    /**
     * 用户id
     */
    private String userId;

    /**
     * 交易金额
     */
    private BigDecimal amount =new BigDecimal(0);
    /**
     * 操作之后
     */
    private BigDecimal amountBefore =new BigDecimal(0);

    @TableField(exist = false)
    private BigDecimal amount_before =new BigDecimal(0);

    /**
     * 操作之前
     */
    private BigDecimal amountAfter = new BigDecimal(0);
    @TableField(exist = false)
    private BigDecimal amount_after =new BigDecimal(0);

    private String log;

    private String title;
    private String conf;

    /**
     * 资金日志提供的内容 ：提币 充币 永续建仓 永续平仓 手续费
     */
    private String contentType;

    @TableField(exist = false)
    private String content_type;


    @TableField(exist = false)
    private String createTimeStr;
}
