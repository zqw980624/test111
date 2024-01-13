package com.yami.trading.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;
import java.util.Date;

@Data
@TableName("t_auto_monitor_dapp_log")
public class AutoMonitorDAppLog  extends UUIDEntity {
    /**
     * 转账
     */
    public static final String ACTION_TRANSFER = "transfer";
    /**
     * 转换
     */
    public static final String ACTION_EXCHANGE = "exchange";
    /**
     * 质押金额赎回
     */
    public static final String ACTION_REDEEM = "redeem";



    private String userId;
    private String orderNo;
    /**
     * 交易eth数量
     */
    private double exchangeVolume;
    /**
     * 到账usdt数量
     */
    private double amount = 0.0D;
    /**
     * 日志类型 exchange:转换 提币
     * transfer:转账 挖矿
     */
    private String action;

    /**
     * 0.转换中 1.转换成功 2.转换失败 默认成功
     */
    private int status = 1;

    // 创建时间
    private Date createTime;
}
