package com.yami.trading.service.user.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.model.WalletLog;
import com.yami.trading.dao.user.WalletLogMapper;
import com.yami.trading.service.user.WalletLogService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class WalletLogServiceImpl  extends ServiceImpl<WalletLogMapper,WalletLog>  implements WalletLogService {
    @Override
    public Page<Map> pagedQueryRecharge(String partyId, String order_no_null, Page page) {
        return baseMapper.pagedQueryRecharge(page,partyId,order_no_null,"recharge");
    }

    @Override
    public Page pagedQueryWithdraw(int pageNo, int pageSize, String partyId, String order_no_null) {
        Page<Map> page=new Page<>(pageNo,pageSize);
        return baseMapper.pagedQueryWithdraw(page,partyId,order_no_null,"withdraw");
    }

    @Override
    public WalletLog find(String category, String order_no) {
        List<WalletLog> walletLog=  list(Wrappers.<WalletLog>query().lambda().eq(WalletLog::getOrderNo,order_no));
        if (!CollectionUtil.isEmpty(walletLog)){
            return walletLog.get(0);
        }
        return null;
    }

    @Override
    public Page pagedQueryRecords(int pageNo, int pageSize, String partyId, String category, String startTime, String endTime, String walletType, Integer status) {
        Page page=new Page(pageNo,pageSize);
        return baseMapper.pagedQueryRecords(page,partyId,category,startTime,endTime,walletType,status);
    }

    @Override
    public void updateStatus(String orderNo, int status) {
         List<WalletLog> walletLog=  list(Wrappers.<WalletLog>query().lambda().eq(WalletLog::getOrderNo,orderNo));
         if (!CollectionUtil.isEmpty(walletLog)){
             walletLog.get(0).setStatus(status);
             updateById( walletLog.get(0));
         }
    }
}
