package com.yami.trading.bean.model;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.BaseEntity;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("t_recharge_blockchain_order")
public class RechargeBlockchainOrder extends UUIDEntity {

    /**
     *
     */
    private static final long serialVersionUID = -4483090797419171871L;

    /**
     * 订单号
     */
    private String orderNo;

    private String partyId;

    /**
     * 充值数量
     */
    @TableField("channel_amount")

    private Double volume;

    /**
     * 充值币种
     */
    @TableField("coin")
    private String symbol;


    /**
     * 充值状态 0 初始状态，未知或处理中 1 成功 2 失败
     */
    private int succeeded = 0;
    /**
     * 创建时间
     */
    private Date created;
    /**
     * 审核操作时间
     */
    @TableField("reviewtime")
    private Date reviewTime;

    /**
     * 备注说明，管理员操作
     */
    private String description;
    private String orderType;
    /**
     * 区块链充值地址
     */
    private String blockchainName;
    /**
     * 已充值的上传图片
     */
    private String img;

    /**
     * 客户自己的区块链地址
     */
    private String address;
    /**
     * 通道充值地址
     */
    private String channelAddress;

    /**
     * 转账hash
     */
    private String tx;


 }
