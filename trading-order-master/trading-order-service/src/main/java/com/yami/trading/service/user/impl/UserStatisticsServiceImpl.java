package com.yami.trading.service.user.impl;

import cn.hutool.json.JSONUtil;
import com.yami.trading.bean.model.Wallet;
import com.yami.trading.bean.model.WalletExtend;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.service.WalletService;
import com.yami.trading.service.user.UserRecomService;
import com.yami.trading.service.user.UserStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Slf4j
public class UserStatisticsServiceImpl  implements UserStatisticsService {


    @Autowired
    UserRecomService userRecomService;
    @Autowired
    WalletService walletService;

    @Override
     public List<Map<String,Object>> getAssetsAll(String loginPartyId,String targetPartyId) {
        if (!StringUtils.isNullOrEmpty(loginPartyId)) {
//            List<String> children = this.userRecomService.findChildren(loginPartyId);
//            if (children.size() == 0) {
//                return new ArrayList<>();
//            }
//            if(!children.contains(targetPartyId)) throw new BusinessException("目标用户不属于登录人下级");
        }
        Map<String, Object> moneyAll = walletService.getMoneyAll(targetPartyId);
        Map<String, Object> nameMap = getNameMap();
        List<Map<String,Object>> result = new LinkedList<Map<String,Object>>();
        log.info(JSONUtil.toJsonPrettyStr(moneyAll)+"=============");

        log.info(JSONUtil.toJsonPrettyStr(nameMap)+"=============");

        for(Map.Entry<String, Object> entry :nameMap.entrySet()) {
            if("money_trader".equals(entry.getKey())) {
                continue;
            }
            Map<String,Object> data = new HashMap<String,Object>();
            data.put("name", entry.getValue());
            data.put("value", moneyAll.get(entry.getKey()));
            result.add(data);
        }
        return result;
    }


    public Map<String,Object> getNameMap(){
        Map<String,Object> data = new LinkedHashMap<String, Object>();

        data.put("money_all_coin", "钱包资产折合[USDT]");

        data.put("money_miner", "矿机");
        data.put("money_finance", "理财");
        data.put("money_contract", "永续合约");
        data.put("money_futures", "交割合约");
        data.put("money_fund", "基金");
        data.put("money_coin", "币余额");
        data.put("money_wallet", "钱包USDT");
//		data.put("money_trader", "理财资产");
        data.put("money_ico", "ico");
        data.put("total", "总资产");
        return data;
    }

    @Override
    public List<Map<String,Object>> getWalletExtends(String loginPartyId, String targetPartyId) {
        List<WalletExtend> findExtend = walletService.findExtend(targetPartyId);
        List<Map<String,Object>> result = new LinkedList<Map<String,Object>>();
        if(ObjectUtils.isNotEmpty(findExtend)) {
            for(WalletExtend we : findExtend) {
                if(ObjectUtils.isNotEmpty(we)) {
                    Map<String,Object> data = new HashMap<String,Object>();
                    if ("USDT_USER".equals(we.getWallettype())
                            || "ETH_DAPP".equals(we.getWallettype())
                            || "USDT_DAPP".equals(we.getWallettype())
                            || "ETH_USER".equals(we.getWallettype())) {
                        continue;
                    }
                    data.put("wallettype", we.getWallettype());
                    data.put("amount", new BigDecimal(we.getAmount()).setScale(8, RoundingMode.FLOOR).toPlainString());
                    data.put("lock_amount",null==we?0:new BigDecimal(we.getLockAmount()).setScale(8, RoundingMode.FLOOR).toPlainString() );
                    data.put("freeze_amount",null==we?0:new BigDecimal(we.getFreezeAmount()).setScale(8, RoundingMode.FLOOR).toPlainString() );
                    result.add(data);
                }
            }
        }
        Map<String,Object> data = new HashMap<String,Object>();
        Wallet wallet = walletService.saveWalletByPartyId(targetPartyId);
        data.put("wallettype", "usdt");
        data.put("amount",null==wallet?0:new BigDecimal(wallet.getMoney().doubleValue()).setScale(8, RoundingMode.FLOOR).toPlainString() );
        data.put("lock_amount",null==wallet?0:wallet.getLockMoney().setScale(8, RoundingMode.FLOOR).toPlainString() );
        data.put("freeze_amount",null==wallet?0:wallet.getFreezeMoney().setScale(8, RoundingMode.FLOOR).toPlainString() );
        result.add(0,data);

        return result;
    }
}
