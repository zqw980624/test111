package com.yami.trading.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.model.ChannelBlockchain;
import com.yami.trading.bean.model.RealNameAuthRecord;

import java.util.List;

public interface ChannelBlockchainService  extends IService<ChannelBlockchain> {


    public ChannelBlockchain findByNameAndCoinAndAdd(String blockchain_name, String coin,String address);

    List<ChannelBlockchain> findByCoin(String coin);
}
