package com.yami.trading.service.user.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.model.UserDataSum;
import com.yami.trading.bean.model.WalletExtend;
import com.yami.trading.dao.WalletExtendMapper;
import com.yami.trading.dao.user.UserDataSumMapper;
import com.yami.trading.service.user.UserDataSumService;
import com.yami.trading.service.user.WalletExtendService;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Service
public class WalletExtendServiceImpl  extends ServiceImpl<WalletExtendMapper, WalletExtend> implements WalletExtendService {
    @Override
    public List<WalletExtend> findByUserIdAndWallettype(String userId, String wallettype) {

        return list(Wrappers.<WalletExtend>query().lambda().eq(WalletExtend::getPartyId,userId).eq(WalletExtend::getWallettype,wallettype));
    }

    @Override
    public List<WalletExtend> findByUserIdAndWallettype(String partyId, List<String> list_symbol) {
        return list(Wrappers.<WalletExtend>query().lambda().eq(WalletExtend::getPartyId,partyId).in(WalletExtend::getWallettype,list_symbol));
    }

    @Override
    public List<WalletExtend> findByUserId(Serializable partyId) {
      List<WalletExtend> list=list(Wrappers.<WalletExtend>query().lambda().eq(WalletExtend::getPartyId,partyId));
        return list;
    }
}
