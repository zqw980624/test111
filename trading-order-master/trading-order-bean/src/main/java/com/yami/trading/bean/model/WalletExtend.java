package com.yami.trading.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("t_wallet_extend")
public class WalletExtend extends UUIDEntity {

    @Version
    private  int version;
    private static final long serialVersionUID = -926374250240199976L;
    private String partyId;
    /**
     * 币种，见Constants定义
     */
    private String wallettype;
    /**
     * 金额
     */
    private double amount= 0.0D;

    /**
     * 锁定金额
     */
    private double lockAmount = 0.0D;

    /**
     * 冻结金额
     */
    private double freezeAmount = 0.0D;


    private String name;
}
