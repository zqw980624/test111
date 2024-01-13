package com.yami.trading.service.contract;

import com.yami.trading.bean.contract.domain.ContractOrder;
import com.yami.trading.common.constants.ContractRedisKeys;
import com.yami.trading.common.util.RedisUtil;
import com.yami.trading.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ContractLoadCacheService implements ApplicationRunner {

    @Autowired
    private WalletService walletService;

    @Autowired
    private ContractOrderService contractOrderService;

    public void load() {
        List<ContractOrder> list = contractOrderService.list();
        Map<String, Map<String, ContractOrder>> cacheMap = new ConcurrentHashMap<>();

        // 永续合约：总资产、总保证金、总未实现盈利
        Map<String, Map<String, BigDecimal>> contractAssetsMap = new ConcurrentHashMap<>();

        for (ContractOrder order : list) {
            if (ContractOrder.STATE_SUBMITTED.equals(order.getState())) {
                if (cacheMap.containsKey(order.getPartyId())) {
                    Map<String, ContractOrder> map = cacheMap.get(order.getPartyId());
                    map.put(order.getOrderNo(), order);
                    cacheMap.put(order.getPartyId(), map);
                } else {
                    Map<String, ContractOrder> map = new ConcurrentHashMap<>();
                    map.put(order.getOrderNo(), order);
                    cacheMap.put(order.getPartyId(), map);
                }

                // 获取 单个订单 永续合约总资产、总保证金、总未实现盈利
                Map<String, BigDecimal> contractAssetsOrder = this.walletService.getMoneyContractByOrder(order);

                if (contractAssetsMap.containsKey(order.getPartyId())) {
                    Map<String, BigDecimal> contractAssetsOld = contractAssetsMap.get(order.getPartyId());
                    if (null == contractAssetsOld) {
                        contractAssetsOld = new HashMap<>();
                        contractAssetsOld.put("money_contract", BigDecimal.ZERO);
                        contractAssetsOld.put("money_contract_deposit", BigDecimal.ZERO);
                        contractAssetsOld.put("money_contract_profit", BigDecimal.ZERO);
                    }
                    contractAssetsOld.put("money_contract", contractAssetsOld.get("money_contract").add(contractAssetsOrder.get("money_contract")));
                    contractAssetsOld.put("money_contract_deposit", contractAssetsOld.get("money_contract_deposit").add(contractAssetsOrder.get("money_contract_deposit")));
                    contractAssetsOld.put("money_contract_profit", contractAssetsOld.get("money_contract_profit").add(contractAssetsOrder.get("money_contract_profit")));
                    contractAssetsMap.put(order.getPartyId(), contractAssetsOld);
                } else {
                    contractAssetsMap.put(order.getPartyId(), contractAssetsOrder);
                }
            }

            RedisUtil.set(ContractRedisKeys.CONTRACT_ORDERNO + order.getOrderNo(), order);
        }

        for (Map.Entry<String, Map<String, ContractOrder>> entry : cacheMap.entrySet()) {
			RedisUtil.set(ContractRedisKeys.CONTRACT_SUBMITTED_ORDER_PARTY_ID + entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Map<String, BigDecimal>> entry : contractAssetsMap.entrySet()) {
			RedisUtil.set(ContractRedisKeys.CONTRACT_ASSETS_PARTY_ID + entry.getKey(), entry.getValue().get("money_contract"));
			RedisUtil.set(ContractRedisKeys.CONTRACT_ASSETS_DEPOSIT_PARTY_ID + entry.getKey(), entry.getValue().get("money_contract_deposit"));
			RedisUtil.set(ContractRedisKeys.CONTRACT_ASSETS_PROFIT_PARTY_ID + entry.getKey(), entry.getValue().get("money_contract_profit"));
        }
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始ContractOrder数据加载redis");
        load();
        log.info("完成ContractOrder数据加载redis");
    }
}