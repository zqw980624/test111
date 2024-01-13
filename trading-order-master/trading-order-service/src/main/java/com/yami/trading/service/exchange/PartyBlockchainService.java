package com.yami.trading.service.exchange;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.exchange.ExchangeApplyOrder;
import com.yami.trading.bean.exchange.PartyBlockchain;

import java.util.List;

public interface  PartyBlockchainService extends IService<PartyBlockchain> {

    List<PartyBlockchain> findByUserName(String userName);

    public List<PartyBlockchain> findByUserNameAndCoinSymbol(String userName,String coinSymbol);
}
