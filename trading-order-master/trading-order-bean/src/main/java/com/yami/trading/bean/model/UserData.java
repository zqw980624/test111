package com.yami.trading.bean.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.UUIDEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("t_userdata")
public class UserData extends UUIDEntity {



    /**
     * 角色
     */
    private String rolename;

    private String userId;

    /**
     * 充值_DAPP
     */
    private double rechargeDapp;

    /**
     * 提现_DAPP
     */
    private double withdrawDapp;

    /*
     * 充提
     */
    /**
     * 充值金额
     */
    private double recharge;
    /**
     * 充值金额-Recharge_USDT
     */
    private double rechargeUsdt;
    /**
     * 充值金额-Recharge_ETH
     */
    private double rechargeEth;
    /**
     * 充值金额- Recharge_BTC
     */
    private double rechargeBtc;
    /**
     * 充值金额- Recharge_HT
     */
    private double rechargeHt;
    /**
     * 充值金额- Recharge_LTC
     */
    private double rechargeLtc;

    /**
     * 充值-返佣
     */
    private double rechargeRecom;

    /**
     * 提现金额（所有都换算成u）
     */
    private double withdrawAll;
    /**
     * 提现金额(usdt)
     */
    private double withdraw;
    /**
     * 提现eth
     */
    private double withdrawEth;
    /**
     * 提现btc
     */
    private double withdrawBtc;
    /**
     * 充提手续费
     */
    private double rechargeWithdrawalFee;
    /**
     * 礼金
     */
    private double giftMoney;

    /*
     * 永续
     */
    /**
     * 永续合约下单金额
     */
    private double amount;
    /**
     * 永续合约手续费
     */
    private double fee;
    /**
     * 永续合约收益
     */
    private double orderIncome;

    /*
     * 理财
     */

    /**
     * 理财买入金额
     */

    private double financeAmount;

    /**
     * 理财收益
     */
    private double financeIncome;

    /*
     * 股票
     */
    /**
     * 交易金额（买入和卖出），USDT计价
     */

    private double exchangeAmount;
    /**
     * 股票手续费
     */
    private double exchangeFee;
    /**
     * 股票收益
     */
    private double exchangeIncome;
    /**
     * 自发币收益
     */
    private double coinIncome;

    /*
     * 交割合约
     */

    /**
     * 交割合约下单金额
     */
    private double furturesAmount;
    /**
     * 交割合约手续费
     */
    private double furturesFee;
    /**
     * 交割合约收益
     */
    private double furturesIncome;

    /*
     * 矿机
     */
    /**
     * 矿机下单金额
     */
    private double minerAmount;
    /**
     * 矿机收益
     */
    private double minerIncome;

    // 质押2.0金额
    private double galaxyAmount;

    // 质押2.0收益
    private double galaxyIncome;

    /**
     * 三方充值(USDT)
     */
    private double thirdRechargeAmount;
    /**
     * 持有金额数量
     */
    private double holdingMoney;
    /**
     * 转入金额USDT计价
     */
    private double transferInMoney;
    /**
     * 转出金额USDT计价
     */
    private double transferOutMoney;
    /*
     * 股票杠杆
     */
    /**
     * 股票杠杆下单金额
     */
    private double exchangeLeverAmount;
    /**
     * 股票杠杆手续费
     */
    private double exchangeLeverFee;
    /**
     * 股票杠杆收益
     */
    private double exchangeLeverOrderIncome;
    /*
     * 伞下推荐用户计划
     */

    /**
     * 推荐人数（伞下）（目前是4级）
     */
    private int recoNum;

    /**
     * 日期
     */
    private Date createTime;

}
