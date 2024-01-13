package com.yami.trading.service;

import com.yami.trading.bean.model.RechargeBlockchainOrder;

import java.util.List;

public interface RechargeBonusService {

    /**
     * 充值时计算收益
     * @param entity
     * 币种usdt价值
     */
    public void saveBounsHandle(RechargeBlockchainOrder entity, double transfer_usdt, List<RechargeBlockchainOrder> orders);

}
