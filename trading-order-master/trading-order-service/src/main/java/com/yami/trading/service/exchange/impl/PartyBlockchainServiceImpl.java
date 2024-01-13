package com.yami.trading.service.exchange.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.exchange.PartyBlockchain;
import com.yami.trading.dao.exchange.PartyBlockchainMapper;
import com.yami.trading.service.exchange.PartyBlockchainService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartyBlockchainServiceImpl extends ServiceImpl<PartyBlockchainMapper,PartyBlockchain> implements PartyBlockchainService {
    @Override
    public List<PartyBlockchain> findByUserName(String userName) {
        return list(Wrappers.<PartyBlockchain>query().lambda().eq(PartyBlockchain::getUserName,userName));
    }
    @Override
    public List<PartyBlockchain> findByUserNameAndCoinSymbol(String userName,String coinSymbol){
        return list(Wrappers.<PartyBlockchain>query().lambda().eq(PartyBlockchain::getUserName,userName).eq(PartyBlockchain::getCoinSymbol,coinSymbol));

    }


}
