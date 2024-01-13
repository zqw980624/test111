package com.yami.trading.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.model.WalletExtend;

import java.io.Serializable;
import java.util.List;

public interface WalletExtendService extends IService<WalletExtend> {
    List<WalletExtend> findByUserIdAndWallettype(String userId, String wallettype);

    List<WalletExtend> findByUserIdAndWallettype(String partyId, List<String> list_symbol);



    List<WalletExtend> findByUserId(Serializable partyId);
}
