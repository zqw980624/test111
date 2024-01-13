package com.yami.trading.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.constans.WalletConstants;
import com.yami.trading.bean.contract.domain.ContractApplyOrder;
import com.yami.trading.bean.contract.domain.ContractOrder;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.exchange.ExchangeApplyOrder;
import com.yami.trading.bean.future.domain.FuturesOrder;
import com.yami.trading.bean.future.domain.FuturesRedisKeys;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.item.dto.RealtimeDTO;
import com.yami.trading.bean.model.MoneyLog;
import com.yami.trading.bean.model.Wallet;
import com.yami.trading.bean.model.WalletExtend;
import com.yami.trading.common.constants.ContractRedisKeys;
import com.yami.trading.common.constants.WalletRedisKeys;
import com.yami.trading.common.exception.BusinessException;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.ApplicationContextUtils;
import com.yami.trading.common.util.Arith;
import com.yami.trading.common.util.RedisUtil;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.dao.user.WalletMapper;
import com.yami.trading.service.MoneyLogService;
import com.yami.trading.service.WalletService;
import com.yami.trading.service.contract.ContractApplyOrderService;
import com.yami.trading.service.contract.ContractOrderService;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.exchange.ExchangeApplyOrderService;
import com.yami.trading.service.future.FuturesOrderService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.user.WalletExtendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WalletServiceImpl extends ServiceImpl<WalletMapper, Wallet> implements WalletService {

    @Qualifier("dataService")
    @Autowired
    @Lazy
    private DataService dataService;
    @Autowired
    private SysparaService sysparaService;
    @Autowired
    private ItemService itemService;
    @Autowired
    MoneyLogService moneyLogService;
    @Autowired
    WalletMapper walletMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    WalletExtendService walletExtendService;

    @Override
    public Wallet findByUserId(String userId) {
        Wallet wallet = getOne(Wrappers.<Wallet>query().lambda().eq(Wallet::getUserId, userId));
        return wallet;
    }

    @Override
    public List<WalletExtend> findExtend(String partyId) {
        List<WalletExtend> walletExtends = walletExtendService.findByUserId(partyId);
        return walletExtends;
    }

    @Override
    public List<WalletExtend> findExtend(String partyId, List<String> list_symbol) {
        return walletExtendService.findByUserIdAndWallettype(partyId,list_symbol);
    }

    @Override
    public WalletExtend saveExtendByPara(String userId, String wallettype) {
        if (StringUtils.isEmptyString(wallettype) || userId == null || StringUtils.isEmptyString(userId.toString())) {
            log.error("saveExtendByPara fail,partyId:{},wallettype:{}", new Object[]{userId, wallettype});
            throw new RuntimeException("saveExtendByPara fail");
        }
        List<WalletExtend> walletExtends = walletExtendService.findByUserIdAndWallettype(userId, wallettype);
        if (CollectionUtils.isNotEmpty(walletExtends)) return walletExtends.get(0);
        WalletExtend walletExtend = new WalletExtend();
        walletExtend.setPartyId(userId);
        walletExtend.setWallettype(wallettype);
        walletExtendService.save(walletExtend);
        return walletExtend;
    }

    @Override
    public void updateExtend(String partyId, String walletType, double amount) {
        List<WalletExtend>  walletExtends = walletExtendService.findByUserIdAndWallettype(partyId,walletType);
        WalletExtend walletExtend=null;
        if (CollectionUtils.isNotEmpty(walletExtends)){
             walletExtend=walletExtends.get(0);
        }
        if (walletExtend == null) {
            walletExtend = this.saveExtendByPara(partyId, walletType);
        }
        log.info(JSONUtil.toJsonStr(walletExtend));
        log.info("=============111111===>"+walletType);
        walletExtend.setAmount(Arith.add(walletExtend.getAmount(), (int)amount));
        //walletExtend.setAmount(walletExtend.getAmount()+(int)amount);
        if (!walletExtendService.updateById(walletExtend)) {
            throw new YamiShopBindException("操作钱包失败!");
        }
        redisTemplate.opsForValue().set(WalletRedisKeys.WALLET_EXTEND_PARTY_ID + partyId.toString() + walletType, walletExtend);
    }

    @Override
    public Wallet saveWalletByPartyId(String partyId) {
        Wallet wallet = findByUserId(partyId);
        if (wallet != null) {
            return wallet;
        } else {
            wallet = new Wallet();
            wallet.setUserId(partyId);
            save(wallet);
            return wallet;
        }
    }

    @Override
    public BigDecimal sumMoney() {
        return baseMapper.sumMoney();
    }

    @Override
    public BigDecimal sumMoneyAgent(String userCode) {
        return baseMapper.sumMoneyAgent(userCode);
    }

    @Override
    @Transactional
    public void updateMoney(String symbol, String userId, BigDecimal money, BigDecimal amountFee,
                            String category, String walletType, String contentType, String log) {

        Date now = new Date();
        Wallet wallet = findByUserId(userId);
        BigDecimal amountBefore = wallet.getMoney();
        wallet.setMoney(wallet.getMoney().add(money));
        wallet.setUpdateTime(now);
        updateById(wallet);
        // 账变日志
        MoneyLog moneyLog = new MoneyLog();
        moneyLog.setCreateTime(now);
        moneyLog.setSymbol(symbol);
        moneyLog.setCategory(category);
        moneyLog.setAmountBefore(amountBefore);
        moneyLog.setAmount(money);
        moneyLog.setTitle("Modify account balance");
        moneyLog.setConf("Modify balance");
        moneyLog.setAmountAfter(wallet.getMoney());
        moneyLog.setUserId(userId);
        moneyLog.setWalletType(walletType);
        moneyLog.setContentType(contentType);
        moneyLog.setLog(log);
        moneyLogService.save(moneyLog);
    }

    /*
     * 获取 所有订单 永续合约总资产、总保证金、总未实现盈利,当日盈利
     */
    public Map<String, Double> getMoneyContract(Serializable partyId, String symbolType) {
        double money_contract = 0;
        double money_contract_deposit = 0;
        double money_contract_profit = 0;
        double money_contract_profit_today = 0;

        ContractOrderService contractOrderService = ApplicationContextUtils.getBean(ContractOrderService.class);
        List<ContractOrder> contractOrders = contractOrderService.findSubmitted(partyId.toString(), "", "");

        if (contractOrders != null) {
            for (ContractOrder order : contractOrders) {
                String symbol = order.getSymbol();
                Item bySymbol = itemService.findBySymbol(symbol);
                if (bySymbol == null) {
                    continue;
                }
                // 类型不对直接continue
                if (StringUtils.isNotEmpty(symbolType)) {
                    if (!bySymbol.getType().equalsIgnoreCase(symbolType)) {
                        continue;
                    }
                }
                // 真正下单里
                double order_volume = 1;

                if (order.getLeverRate() != null && order.getLeverRate().compareTo(BigDecimal.ZERO) != 0) {
                    order_volume = order.getVolumeOpen().divide(order.getLeverRate()).doubleValue();
                } else {
                    order_volume = order.getVolumeOpen().doubleValue();
                }
                double amount = Arith.add(Arith.mul(order_volume, order.getUnitAmount().doubleValue()), order.getProfit().doubleValue());
                money_contract = Arith.add(amount, money_contract);
                money_contract_deposit = Arith.add(order.getDeposit().doubleValue(), money_contract_deposit);
                money_contract_profit = Arith.add(order.getProfit().doubleValue(), money_contract_profit);
                // 只需要计算当日盈亏比例*金额就是当日盈亏
               Item item =itemService.findBySymbol(symbol);
                Object results = redisTemplate.opsForValue().get("ydTask" + item.getPid());
                cn.hutool.json.JSONObject msgObject = JSONUtil.parseObj(results);
                String ydList = (String) redisTemplate.opsForValue().get("yd" + symbol + item.getPid());//取list
                if (StringUtils.isNotEmpty(ydList)) {
                    com.alibaba.fastjson2.JSONObject jsonObject = com.alibaba.fastjson2.JSONObject.parseObject(ydList);
                    BigDecimal open = jsonObject.getBigDecimal("Open");
                    if (msgObject!=null) {
                        // 当前每张金额*加杠杆后多少金额
                        // 今天总体涨跌幅
                        BigDecimal changeRatio = new BigDecimal(msgObject.getStr("last")).subtract(open).divide(open, 10, RoundingMode.HALF_UP);
                        Double todayProfit = order.getUnitAmount().multiply(order.getVolumeOpen()).multiply(changeRatio).setScale(10, RoundingMode.HALF_UP).doubleValue();
                        money_contract_profit_today += todayProfit;
                    }
                }
            }
        }

        Map<String, Double> moneys_contract = new HashMap<String, Double>();
        moneys_contract.put("money_contract", money_contract);
        moneys_contract.put("money_contract_deposit", money_contract_deposit);
        moneys_contract.put("money_contract_profit", money_contract_profit);
        moneys_contract.put("money_contract_profit_today", money_contract_profit_today);
        // usdt余额
        Wallet wallet = new Wallet();
        if (!"".equals(partyId) && partyId != null) {
            wallet = findByUserId(partyId.toString());
        }
        moneys_contract.put("money_wallet", wallet.getMoney().doubleValue());

        return moneys_contract;
    }

    @Override
    public Map<String, Object> getMoneyAll(Serializable partyId) {

        Map<String, Object> data = new HashMap<String, Object>();
        DecimalFormat df2 = new DecimalFormat("#.##");

        double money = 0;
        double money_wallet = 0;
        double money_coin = 0;
        double money_all_coin = 0;
        double money_finance = 0;
        double money_miner = 0;
        double money_contractApply = 0;
        double money_contract = 0;
        double money_contract_deposit = 0;
        double money_contract_profit = 0;
        double money_futures = 0;
        double money_futures_profit = 0;

        // 先获取一次所有币种的数据来计算
        String data_symbol = "";
        List<String> list_symbol = new ArrayList<String>();

        List<Item> list_items = this.itemService.cacheGetByMarket("");
        for (int i = 0; i < list_items.size(); i++) {
            Item items = list_items.get(i);
            list_symbol.add(items.getSymbol());
            if (i != 0) {
                data_symbol = data_symbol + "," + items.getSymbol();
            } else {
                data_symbol = items.getSymbol();
            }
        }

        List<Realtime> realtime_all = this.dataService.realtime(data_symbol);
        if (realtime_all.size() <= 0) {
            throw new BusinessException("系统错误，请稍后重试");
        }

        // usdt余额
        Wallet wallet = new Wallet();
        if (!"".equals(partyId) && partyId != null) {
            wallet = saveWalletByPartyId(partyId.toString());
        }

        money = wallet.getMoney().doubleValue();
        // 钱包USDT
        money_wallet = wallet.getMoney().doubleValue();
        // 币余额
        money_coin = this.getMoneyCoin(partyId, realtime_all, list_symbol);
        money = money + money_coin;
        // 钱包USDT+币余额
        money_all_coin = money;
        // 永续委托
        money_contractApply = getMoneyContractApply(partyId);
        money = money + money_contractApply;

        Map<String, Object> moneys_contract = this.getMoneyContractRedis(partyId);
        if (null != moneys_contract && 0 != moneys_contract.size()) {
            // 永续
            Object money_contract1 = moneys_contract.get("money_contract");
            if(money_contract1 instanceof BigDecimal){
                money_contract = ((BigDecimal) money_contract1).doubleValue();
            }else{
                money_contract = (Double) money_contract1;
            }
            // 永续总保证金
            Object money_contract_deposit1 = moneys_contract.get("money_contract_deposit");
            if(money_contract_deposit1 instanceof BigDecimal){
                money_contract_deposit = ((BigDecimal) money_contract_deposit1).doubleValue();
            }else{
                money_contract_deposit = (Double) money_contract_deposit1;
            }
            // 永续总未实现盈亏
            Object money_contract_profit1 = moneys_contract.get("money_contract_profit");
            if(money_contract_deposit1 instanceof BigDecimal){
                money_contract_profit = ((BigDecimal) money_contract_profit1).doubleValue();
            }else{
                money_contract_deposit = (Double) money_contract_deposit1;
            }
        }
        money = money + money_contract;
        Map<String, Object> moneys_futures = this.getMoneyFuturesRedis(partyId);
        if (null != moneys_futures && 0 != moneys_futures.size()) {
            // 交割
            money_futures = (Double) moneys_futures.get("money_futures");
            // 交割未实现盈亏
            money_futures_profit = (Double) moneys_futures.get("money_futures_profit");
        }

        money = money + money_futures;

        // 币币交易
        money = money + this.getMoneyexchangeApplyOrders(partyId, realtime_all);

        data.put("total", df2.format(money));
        //锁定金额
        data.put("lock_money", df2.format(wallet.getLockMoney()));
        //冻结金额
        data.put("freeze_money", df2.format(wallet.getFreezeMoney()));
        data.put("money_wallet", df2.format(money_wallet));
        data.put("money_coin", df2.format(money_coin));
        data.put("money_all_coin", df2.format(money_all_coin));
        data.put("money_miner", df2.format(money_miner));
        data.put("money_finance", df2.format(money_finance));
        data.put("money_contract", df2.format(Arith.add(money_contract, money_contractApply)));
        data.put("money_contract_deposit", df2.format(money_contract_deposit));
        data.put("money_contract_profit", df2.format(money_contract_profit));
        data.put("money_futures", df2.format(money_futures));
        data.put("money_futures_profit", df2.format(money_futures_profit));

        return data;
    }
    @Override
    public Map<String, Object> getMoneyAll(Serializable partyId, String symbolType) {

        Map<String, Object> data = new HashMap<String, Object>();
        DecimalFormat df2 = new DecimalFormat("#.##");

        double money = 0;
        double money_wallet = 0;
        double money_coin = 0;
        double money_all_coin = 0;
        double money_finance = 0;
        double money_miner = 0;
        double money_contractApply = 0;
        double money_contract = 0;
        double money_contract_deposit = 0;
        double money_contract_profit = 0;
        double money_futures = 0;
        double money_futures_profit = 0;
        // 当前类型，持有的资产
        double symbol_type_asserts = 0;

        // 先获取一次所有币种的数据来计算
        String data_symbol = "";
        List<String> list_symbol = new ArrayList<String>();

        List<Item> list_items = this.itemService.findByType(symbolType);
        for (int i = 0; i < list_items.size(); i++) {
            Item items = list_items.get(i);
            list_symbol.add(items.getSymbol());
            if (i != 0) {
                data_symbol = data_symbol + "," + items.getSymbol();
            } else {
                data_symbol = items.getSymbol();
            }
        }

        List<Realtime> realtime_all = this.dataService.realtime(data_symbol);
        if (realtime_all.size() <= 0) {
            throw new BusinessException("系统错误，请稍后重试");
        }

        // usdt余额
        Wallet wallet = new Wallet();
        if (!"".equals(partyId) && partyId != null) {
            wallet = saveWalletByPartyId(partyId.toString());
        }

        money = wallet.getMoney().doubleValue();
        // 钱包USDT
        money_wallet = wallet.getMoney().doubleValue();
        // 币余额
        if(CollectionUtils.isEmpty(list_symbol)){
            money_coin = 0;
        }else{
            money_coin = this.getMoneyCoin(partyId, realtime_all, list_symbol);
        }
        money = money + money_coin;
        symbol_type_asserts = symbol_type_asserts+ money_coin;
        // 钱包USDT+币余额
        money_all_coin = money;
        // 永续委托
        money_contractApply = getMoneyContractApply(partyId, list_symbol);
        money = money + money_contractApply;
        symbol_type_asserts = symbol_type_asserts+ money_contractApply;
        Map<String, BigDecimal> moneys_contract = this.getMoneyContractDB(partyId, list_symbol);
        if (null != moneys_contract && 0 != moneys_contract.size()) {
            // 永续
            Object money_contract1 = moneys_contract.get("money_contract");
            if(money_contract1 instanceof BigDecimal){
                money_contract = ((BigDecimal) money_contract1).doubleValue();
            }else{
                money_contract = (Double) money_contract1;
            }
            // 永续总保证金
            Object money_contract_deposit1 = moneys_contract.get("money_contract_deposit");
            if(money_contract_deposit1 instanceof BigDecimal){
                money_contract_deposit = ((BigDecimal) money_contract_deposit1).doubleValue();
            }else{
                money_contract_deposit = (Double) money_contract_deposit1;
            }
            // 永续总未实现盈亏
            Object money_contract_profit1 = moneys_contract.get("money_contract_profit");
            if(money_contract_deposit1 instanceof BigDecimal){
                money_contract_profit = ((BigDecimal) money_contract_profit1).doubleValue();
            }else{
                money_contract_deposit = (Double) money_contract_deposit1;
            }
        }
        money = money + money_contract;
        symbol_type_asserts = symbol_type_asserts+ money_contract;
        Map<String, Double> moneys_futures = this.getMoneyFuturesDB(partyId, list_symbol);
        if (null != moneys_futures && 0 != moneys_futures.size()) {
            // 交割
            money_futures = (Double) moneys_futures.get("money_futures");
            // 交割未实现盈亏
            money_futures_profit = (Double) moneys_futures.get("money_futures_profit");
        }

        money = money + money_futures;
        symbol_type_asserts = symbol_type_asserts+ money_futures;

        // 币币交易
        double moneyexchangeApplyOrders = 0;
        if(CollectionUtils.isEmpty(list_symbol)){
            moneyexchangeApplyOrders = this.getMoneyexchangeApplyOrders(partyId, realtime_all);
        }
        money = money + moneyexchangeApplyOrders;
        symbol_type_asserts = symbol_type_asserts+ moneyexchangeApplyOrders;

        data.put("total", df2.format(money));
        data.put("symbol_type_asserts", df2.format(symbol_type_asserts));
        //锁定金额
        data.put("lock_money", df2.format(wallet.getLockMoney()));
        //冻结金额
        data.put("freeze_money", df2.format(wallet.getFreezeMoney()));
        data.put("money_wallet", df2.format(money_wallet));
        data.put("money_coin", df2.format(money_coin));
        data.put("money_all_coin", df2.format(money_all_coin));
        data.put("money_miner", df2.format(money_miner));
        data.put("money_finance", df2.format(money_finance));
        data.put("money_contract", df2.format(Arith.add(money_contract, money_contractApply)));
        data.put("money_contract_deposit", df2.format(money_contract_deposit));
        data.put("money_contract_profit", df2.format(money_contract_profit));
        data.put("money_futures", df2.format(money_futures));
        data.put("money_futures_profit", df2.format(money_futures_profit));

        return data;
    }
    /*
     * 获取 所有订单 永续合约总资产、总保证金、总未实现盈利 redis
     */
    public Map<String, Object> getMoneyContractRedis(Serializable partyId) {

        BigDecimal contractAssets = (BigDecimal) RedisUtil.get(ContractRedisKeys.CONTRACT_ASSETS_PARTY_ID + partyId.toString());
        BigDecimal contractAssetsDeposit = (BigDecimal)RedisUtil.get(ContractRedisKeys.CONTRACT_ASSETS_DEPOSIT_PARTY_ID + partyId.toString());
        BigDecimal contractAssetsProfit = (BigDecimal) RedisUtil.get(ContractRedisKeys.CONTRACT_ASSETS_PROFIT_PARTY_ID + partyId.toString());

        Map<String, Object> moneys_contract = new HashMap<String, Object>();
        moneys_contract.put("money_contract", null == contractAssets ? 0.000D : contractAssets);
        moneys_contract.put("money_contract_deposit", null == contractAssetsDeposit ? 0.000D : contractAssetsDeposit);
        moneys_contract.put("money_contract_profit", null == contractAssetsProfit ? 0.000D : contractAssetsProfit);

        return moneys_contract;
    }
    public Map<String, BigDecimal> getMoneyContractDB(Serializable partyId, List<String> symbols) {
        List<ContractOrder> list =  ApplicationContextUtils.getBean(ContractOrderService.class).findSubmitted(partyId.toString(), symbols);

        // 永续合约：总资产、总保证金、总未实现盈利
        Map<String, Map<String, BigDecimal>> contractAssetsMap = new ConcurrentHashMap<>();

        for (ContractOrder order : list) {
            if (ContractOrder.STATE_SUBMITTED.equals(order.getState())) {
                // 获取 单个订单 永续合约总资产、总保证金、总未实现盈利
                Map<String, BigDecimal> contractAssetsOrder = getMoneyContractByOrder(order);

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
        }
        return contractAssetsMap.get(partyId);

    }

    public double getMoneyContractApply(Serializable partyId) {
        double money_contractApply = 0;
        ContractApplyOrderService contractApplyOrderService = ApplicationContextUtils.getBean(ContractApplyOrderService.class);

        List<ContractApplyOrder> contractApplyOrders = contractApplyOrderService.findSubmitted(partyId.toString(), "", "", "");
        if (contractApplyOrders != null) {

            for (ContractApplyOrder order : contractApplyOrders) {
                double amount = Arith.mul(order.getVolumeOpen().doubleValue(), order.getUnitAmount().doubleValue());
                money_contractApply = Arith.add(amount, money_contractApply);
            }
        }

        return money_contractApply;
    }

    public double getMoneyContractApply(Serializable partyId, List<String> symbols) {
        double money_contractApply = 0;
        ContractApplyOrderService contractApplyOrderService = ApplicationContextUtils.getBean(ContractApplyOrderService.class);

        List<ContractApplyOrder> contractApplyOrders = contractApplyOrderService.findSubmitted(partyId.toString(), "", "", "");
        if (contractApplyOrders != null) {

            for (ContractApplyOrder order : contractApplyOrders) {
                if(symbols.contains(order.getSymbol())){
                    double amount = Arith.mul(order.getVolumeOpen().doubleValue(), order.getUnitAmount().doubleValue());
                    money_contractApply = Arith.add(amount, money_contractApply);
                }

            }
        }

        return money_contractApply;
    }
    @Override
    public Map<String, BigDecimal> getMoneyContractByOrder(ContractOrder order) {
        Map<String, BigDecimal> moneysContract = new HashMap<String, BigDecimal>();

        if (null == order) {
            moneysContract.put("money_contract", BigDecimal.ZERO);
            moneysContract.put("money_contract_deposit", BigDecimal.ZERO);
            moneysContract.put("money_contract_profit", BigDecimal.ZERO);
            return moneysContract;
        }

        BigDecimal orderVolume = BigDecimal.ONE;

        if (order.getLeverRate() != null && order.getLeverRate().compareTo(BigDecimal.ZERO) != 0) {
            orderVolume = order.getVolumeOpen().divide(order.getLeverRate(), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            orderVolume = order.getVolumeOpen();
        }
        BigDecimal moneyContract = orderVolume.multiply(order.getUnitAmount()).add(order.getProfit());
        BigDecimal moneyContractDeposit = order.getDeposit();
        BigDecimal moneyContractProfit = order.getProfit();


        moneysContract.put("money_contract", moneyContract);
        moneysContract.put("money_contract_deposit", moneyContractDeposit);
        moneysContract.put("money_contract_profit", moneyContractProfit);
        return moneysContract;
    }

    @Override
    public Map<String, Double> getMoneyFuturesByOrder(FuturesOrder order) {
        Map<String, Double> moneysFutures = new HashMap<String, Double>();

        if (null == order) {
            moneysFutures.put("money_futures", 0.0);
            moneysFutures.put("money_futures_profit", 0.0);
            return moneysFutures;
        }

        Double moneyFutures = order.getVolume();
        Double moneyFuturesProfit = order.getProfit();

        moneysFutures.put("money_futures", moneyFutures);
        moneysFutures.put("money_futures_profit", moneyFuturesProfit);
        return moneysFutures;
    }

    @Override
    public void update(String userId, double amount) {
        Wallet wallet = findByUserId(userId);
        wallet.setMoney(new BigDecimal(Arith.add(wallet.getMoney().doubleValue(), amount)));
        int i = walletMapper.updateWall(wallet);
        if (i==0) {
            throw new YamiShopBindException("操作钱包失败!");
        }

    }

    @Override
    public void updateTo(String userId, double amount) {
        Wallet wallet = findByUserId(userId);
        wallet.setMoney(new BigDecimal(Arith.sub(wallet.getMoney().doubleValue(), amount)));
        int i = walletMapper.updateWall(wallet);
        if (i==0) {
            throw new YamiShopBindException("操作钱包失败!");
        }

    }
    @Override
    public void updateExtendWithLockAndFreeze(String partyId, String walletType, double amount, double lockAmount, double freezeAmount) {
        List<WalletExtend> walletExtends=walletExtendService.findByUserIdAndWallettype(partyId, walletType);
        WalletExtend walletExtend = walletExtends.get(0);
        walletExtend.setAmount(Arith.add(walletExtend.getAmount(), amount));
        walletExtend.setLockAmount(Arith.add(walletExtend.getLockAmount(), lockAmount));
        walletExtend.setFreezeAmount(Arith.add(walletExtend.getFreezeAmount(), freezeAmount));
        walletExtendService.updateById(walletExtend);
    }


    @Override
    public void updateWithLockAndFreeze(String partyId, double amount, double lockAmount, double freezeAmount) {

        Wallet wallet = (Wallet) findByUserId(partyId);
        wallet.setMoney(new BigDecimal(Arith.add(wallet.getMoney().doubleValue(), amount)));
        wallet.setLockMoney(new BigDecimal(Arith.add(wallet.getLockMoney().doubleValue(), lockAmount)));
        wallet.setFreezeMoney(new BigDecimal(Arith.add(wallet.getFreezeMoney().doubleValue(), freezeAmount)));
        updateById(wallet);
    }


    @Override
    public void updateMoney(String symbol, String userId, BigDecimal moneyRevise) {
        Wallet wallet = findByUserId(userId);
        BigDecimal amountBefore = wallet.getMoney();
        wallet.setMoney(wallet.getMoney().add(moneyRevise));
        wallet.setUpdateTime(new Date());
        updateById(wallet);
        // 账变日志
        MoneyLog moneyLog = new MoneyLog();
        moneyLog.setCategory(WalletConstants.MONEYLOG_CATEGORY_COIN);
        moneyLog.setAmountBefore(amountBefore);
        moneyLog.setAmount(moneyRevise);
        moneyLog.setAmountAfter(wallet.getMoney().add(moneyRevise));
        moneyLog.setUserId(userId);
        moneyLog.setWalletType(WalletConstants.WALLET_USDT);
        moneyLog.setContentType(WalletConstants.MONEYLOG_CONTENT_RECHARGE);
        moneyLogService.save(moneyLog);
    }

    public double getMoneyCoin(Serializable partyId, List<Realtime> realtime_all, List<String> list_symbol) {
        double money_coin = 0;

        List<WalletExtend> walletExtends = findExtend(partyId.toString(), list_symbol);
        WalletExtend walletExtend = new WalletExtend();
        if (realtime_all.size() <= 0) {

            String data_symbol = "";

            for (int i = 0; i < walletExtends.size(); i++) {
                walletExtend = walletExtends.get(i);
                if (walletExtend.getAmount() > 0) {
                    if (i != 0) {
                        data_symbol = data_symbol + "," + walletExtend.getWallettype();
                    } else {
                        data_symbol = walletExtend.getWallettype();
                    }
                }
            }

            walletExtend = new WalletExtend();

            realtime_all = this.dataService.realtime(data_symbol);
            if (realtime_all.size() <= 0) {
                throw new BusinessException("系统错误，请稍后重试");
            }
        }

        Realtime realtime = null;

        // 如果2个相同，则说明用户所有币账户已经生成 .toUpperCase()/
        if (walletExtends != null && walletExtends.size() != 0) {

            for (int i = 0; i < walletExtends.size(); i++) {
                if (null == walletExtends.get(i)) {
                    continue;
                }

                walletExtend = walletExtends.get(i);
                if (walletExtend.getAmount() > 0) {
                    realtime = null;

                    for (Realtime real : realtime_all) {
                        if (real.getSymbol().equals(walletExtend.getWallettype().toLowerCase())) {
                            realtime = real;
                            break;
                        }
                    }

                    if (realtime != null) {
                        money_coin = Arith.add(money_coin, Arith.mul(realtime.getClose().doubleValue(), walletExtend.getAmount()));
                    }
                }
            }
        }

        return money_coin;
    }

    /*
     * 获取 所有订单 交割合约总资产、总未实现盈利 redis
     */
    public Map<String, Object> getMoneyFuturesRedis(Serializable partyId) {
        Double futuresAssets = (Double) RedisUtil.get(FuturesRedisKeys.FUTURES_ASSETS_PARTY_ID + partyId.toString());
        Double futuresAssetsProfit = (Double) RedisUtil.get(FuturesRedisKeys.FUTURES_ASSETS_PROFIT_PARTY_ID + partyId.toString());
        Map<String, Object> moneys_futures = new HashMap<String, Object>();
        moneys_futures.put("money_futures", null == futuresAssets ? 0.000D : futuresAssets);
        moneys_futures.put("money_futures_profit", null == futuresAssetsProfit ? 0.000D : futuresAssetsProfit);
        return moneys_futures;
    }

    public Map<String, Double> getMoneyFuturesDB(Serializable partyId, List<String> symbolos) {
        List<FuturesOrder> list =  ApplicationContextUtils.getBean(FuturesOrderService.class).findSubmitted(partyId.toString(), symbolos);
        // 交割合约：总资产、总未实现盈利
        Map<String, Map<String, Double>> futuresAssetsMap = new ConcurrentHashMap<String, Map<String, Double>>();

        for (FuturesOrder order : list) {
            // 获取 单个订单 交割合约总资产、总未实现盈利
            Map<String, Double> futuresAssetsOrder = getMoneyFuturesByOrder(order);

            if (futuresAssetsMap.containsKey(order.getPartyId())) {
                Map<String, Double> futuresAssetsOld = futuresAssetsMap.get(order.getPartyId().toString());
                if (null == futuresAssetsOld) {
                    futuresAssetsOld = new HashMap<String, Double>();
                    futuresAssetsOld.put("money_futures", 0.000D);
                    futuresAssetsOld.put("money_futures_profit", 0.000D);
                }
                futuresAssetsOld.put("money_futures", Arith.add(futuresAssetsOld.get("money_futures"), futuresAssetsOrder.get("money_futures")));
                futuresAssetsOld.put("money_futures_profit", Arith.add(futuresAssetsOld.get("money_futures_profit"), futuresAssetsOrder.get("money_futures_profit")));
                futuresAssetsMap.put(order.getPartyId().toString(), futuresAssetsOld);
            } else {
                futuresAssetsMap.put(order.getPartyId().toString(), futuresAssetsOrder);
            }
        }
        return futuresAssetsMap.get(partyId);
    }

    /*
     * 获取 所有订单 交割合约总资产、总未实现盈利
     */
    public Map<String, Object> getMoneyFutures(Serializable partyId) {
        double money_futures = 0;
        double money_futures_profit = 0;
        FuturesOrderService futuresOrderService = ApplicationContextUtils.getBean(FuturesOrderService.class);
        List<FuturesOrder> futuresOrders = futuresOrderService.cacheSubmitted();
        if (futuresOrders != null) {
            for (FuturesOrder order : futuresOrders) {
                if (partyId.equals(order.getPartyId().toString())) {
                    money_futures = Arith.add(order.getVolume(), money_futures);
                    money_futures_profit = Arith.add(order.getProfit(), money_futures_profit);
                }
            }
        }
        Map<String, Object> moneys_futures = new HashMap<String, Object>();
        moneys_futures.put("money_futures", money_futures);
        moneys_futures.put("money_futures_profit", money_futures_profit);
        return moneys_futures;
    }

    public double getMoneyexchangeApplyOrders(Serializable partyId, List<Realtime> realtimeall) {
        double moneyExchange = 0;
        ExchangeApplyOrderService exchangeApplyOrderService = ApplicationContextUtils.getBean(ExchangeApplyOrderService.class);
        List<ExchangeApplyOrder> exchangeApplyOrders = exchangeApplyOrderService.findSubmitted();
        if (exchangeApplyOrders != null) {
            for (ExchangeApplyOrder order : exchangeApplyOrders) {
                if (partyId.equals(order.getPartyId().toString())) {
                    if ("open".equals(order.getOffset())) {
                        moneyExchange = Arith.add(moneyExchange, order.getVolume());
                    }
                    if ("close".equals(order.getOffset())) {
                        Realtime realtime = new Realtime();
                        if (realtimeall.size() <= 0) {
                            List<Realtime> realtime_list = this.dataService.realtime(order.getSymbol());
                            if (realtime_list.size() > 0) {
                                realtime = realtime_list.get(0);
                            } else {
                                throw new BusinessException("系统错误，请稍后重试");
                            }
                        } else {
                            for (Realtime real : realtimeall) {
                                if (real.getSymbol().equals(order.getSymbol())) {
                                    realtime = real;
                                    break;
                                }
                            }
                        }
                        moneyExchange = Arith.add(moneyExchange, Arith.mul(order.getVolume(), realtime.getClose().doubleValue()));
                    }
                }
            }
        }
        return moneyExchange;
    }
}
