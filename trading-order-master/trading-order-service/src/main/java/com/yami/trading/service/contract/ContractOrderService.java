package com.yami.trading.service.contract;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.contract.domain.ContractApplyOrder;
import com.yami.trading.bean.contract.dto.ContractApplyOrderDTO;
import com.yami.trading.bean.contract.dto.ContractOrderDTO;
import com.yami.trading.bean.contract.query.ContractApplyOrderQuery;
import com.yami.trading.bean.contract.query.ContractOrderQuery;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.item.dto.RealtimeDTO;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.model.UserData;
import com.yami.trading.bean.model.Wallet;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.constants.ContractRedisKeys;
import com.yami.trading.common.constants.TipConstants;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.common.util.RandomUtil;
import com.yami.trading.common.util.RedisUtil;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.system.TipService;
import com.yami.trading.service.user.UserDataService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.service.WalletService;
import com.yami.trading.service.item.ItemService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.contract.domain.ContractOrder;
import com.yami.trading.dao.contract.ContractOrderMapper;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 非按金额订单Service
 *
 * @author lucas
 * @version 2023-03-29
 */
@Service
@Transactional
public class ContractOrderService extends ServiceImpl<ContractOrderMapper, ContractOrder> {
    private final ConcurrentMap<String, ContractOrder> map = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public ContractOrderService(){
        executorService.scheduleAtFixedRate(this::flush, 1, 1, TimeUnit.SECONDS);
    }

    public void flush() {
        List<ContractOrder> items = new ArrayList<>(map.values());
        if (!items.isEmpty()) {
            getBaseMapper().batchUpdateBuffer(items);
        }
    }
    @Qualifier("dataService")
    @Autowired
    @Lazy
    private DataService dataService;
    @Autowired
    private ItemService itemService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserService userService;
    @Autowired
    private TipService tipService;
    @Autowired
    private UserDataService userDataService;

    @Autowired
    @Lazy
    private ContractApplyOrderService contractApplyOrderService;

    public IPage<ContractOrderDTO> listRecord(Page page, ContractOrderQuery query) {
        return baseMapper.listRecord(page, query);
    }

    /**
     * 持仓单
     *
     * @param partyId
     * @param symbol
     * @param direction
     * @return
     */
    public List<ContractOrder> findSubmitted(String partyId, String symbol, String direction) {
        QueryWrapper<ContractOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotBlank(partyId), "party_id", partyId);
        queryWrapper.eq(StrUtil.isNotBlank(symbol), "symbol", symbol);
        queryWrapper.eq(StrUtil.isNotBlank(direction), "direction", direction);
        queryWrapper.eq("state", "submitted");
        queryWrapper.orderByDesc("create_time");
        return list(queryWrapper);
    }

    public List<ContractOrder> findSubmitted(String partyId, List<String> symbols) {
        symbols.add("-1");
        QueryWrapper<ContractOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("party_id", partyId);
        queryWrapper.in("symbol", symbols);
        queryWrapper.eq("state", "submitted");
        queryWrapper.orderByDesc("create_time");
        return list(queryWrapper);
    }

    public List<ContractOrder> findSubmitted(String partyId, String symbol, String direction, String startTime, String endTime, String symbolType) {
        QueryWrapper<ContractOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotBlank(partyId), "party_id", partyId);
        queryWrapper.eq(StrUtil.isNotBlank(symbol), "symbol", symbol);
        queryWrapper.eq(StrUtil.isNotBlank(direction), "direction", direction);
        queryWrapper.eq("state", "submitted");
        List<String> symbols = itemService.findByType(symbolType).stream().map(Item::getSymbol).collect(Collectors.toList());
        symbols.add("-1");
        if (StringUtils.isNotEmpty(symbolType) && StringUtils.isEmptyString(symbol)) {
            queryWrapper.in(StringUtils.isNotEmpty(symbolType), "symbol", symbols);
        }
        queryWrapper.ge(StringUtils.isNotEmpty(startTime), "date_format(create_time,'%Y-%m-%d')", startTime);
        queryWrapper.le(StringUtils.isNotEmpty(endTime), "date_format(create_time,'%Y-%m-%d')", endTime);

        queryWrapper.orderByDesc("create_time");
        return list(queryWrapper);
    }

    public List<Map<String, Object>> findSubmittedRedis(String partyId, String symbol, String startTime, String endTime, String symbolType) {
        List<ContractOrder> list = findSubmitted(partyId, symbol, null, startTime, endTime, symbolType);
        return this.bulidData(list);
    }

    public List<Map<String, Object>> findSubmittedRedis(String partyId, String symbol) {
        List<ContractOrder> list = findSubmitted(partyId, symbol, null);
        return this.bulidData(list);
    }

    public List<Map<String, Object>> getPaged(int pageNo, int pageSize, String partyId, String symbol, String type, String queryDate) {
        QueryWrapper<ContractOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotBlank(partyId), "party_id", partyId);
        queryWrapper.eq(StrUtil.isNotBlank(symbol), "symbol", symbol);
        if ("orders".equals(type)) {
            queryWrapper.eq("state", "submitted");
            queryWrapper.eq(StringUtils.isNotEmpty(queryDate), "date_format(create_time,'%Y-%m-%d')", queryDate);

        } else if ("hisorders".equals(type)) {
            queryWrapper.eq("state", "created");
            queryWrapper.eq(StringUtils.isNotEmpty(queryDate), "date_format(create_time,'%Y-%m-%d')", queryDate);

        }
        queryWrapper.orderByDesc("create_time");

        Date date = DateUtils.addDay(new Date(), -1);

        Page page = new Page(pageNo, pageSize);
        List<ContractOrder> list = baseMapper.selectPage(page, queryWrapper).getRecords();
        List<Map<String, Object>> data = this.bulidData(list);
        return data;
    }


    public Long getOrdersCount(String type, String partyId, String symbol, String symbolType) {
        QueryWrapper<ContractOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotBlank(partyId), "party_id", partyId);
        queryWrapper.eq(StrUtil.isNotBlank(symbol), "symbol", symbol);
        if ("orders".equals(type)) {
            queryWrapper.eq("state", "submitted");
        } else if ("hisorders".equals(type)) {
            queryWrapper.eq("state", "created");
        }
        List<String> symbols = itemService.findByType(symbolType).stream().map(Item::getSymbol).collect(Collectors.toList());
        symbols.add("-1");
        if (StringUtils.isNotEmpty(symbolType) && StringUtils.isEmptyString(symbol)) {
            queryWrapper.in(StringUtils.isNotEmpty(symbolType), "symbol", symbols);
        }
        return count(queryWrapper);
    }


    public List<ContractOrder> findSubmitted() {
        QueryWrapper<ContractOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("state", ContractOrder.STATE_SUBMITTED);
        return list(queryWrapper);
    }

    public ContractOrder findByOrderNoRedis(String orderNo) {
       return  RedisUtil.get(ContractRedisKeys.CONTRACT_ORDERNO + orderNo);

    }
    public ContractOrder findByOrderNo(String orderNo) {
        QueryWrapper<ContractOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        queryWrapper.last("limit 1");
        List<ContractOrder> list = list(queryWrapper);
        if (CollectionUtil.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    public List<Map<String, Object>> getPaged(int pageNo, int pageSize, String partyId, String symbol, String type, String startTime, String endTime, String symbolType) {
        QueryWrapper<ContractOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotBlank(partyId), "party_id", partyId);
        queryWrapper.eq(StrUtil.isNotBlank(symbol), "symbol", symbol);
        if ("orders".equals(type)) {
            queryWrapper.eq("state", "submitted");
        } else if ("hisorders".equals(type)) {
            queryWrapper.eq("state", "created");
        }
        List<String> symbols = itemService.findByType(symbolType).stream().map(Item::getSymbol).collect(Collectors.toList());
        symbols.add("-1");
        if (StringUtils.isNotEmpty(symbolType) && StringUtils.isEmptyString(symbol)) {
            queryWrapper.in(StringUtils.isNotEmpty(symbolType), "symbol", symbols);
        }
        queryWrapper.ge(StrUtil.isNotBlank(startTime), "create_time", startTime + " 00:00:00");
        queryWrapper.le(StrUtil.isNotBlank(endTime), "create_time", endTime + " 23:59:59");
        queryWrapper.orderByDesc("create_time");

        Page page = new Page(pageNo, pageSize);
        List<ContractOrder> list = baseMapper.selectPage(page, queryWrapper).getRecords();
        List<Map<String, Object>> data = this.bulidData(list);
        return data;
    }


    /**
     * 平仓，按订单进行平仓
     */
    @Transactional
    public ContractOrder saveClose(String partyId, String orderNo) {
        /*
         * 平仓
         */
        ContractOrder order = this.findByOrderNo(orderNo);
        if (order == null || !ContractOrder.STATE_SUBMITTED.equals(order.getState())
                || !partyId.equals(order.getPartyId()) || order.getVolume().compareTo(BigDecimal.ZERO) <= 0) {
            /**
             * 状态已改变，退出处理
             */
            return null;
        }

        /**
         * 收益
         */
        BigDecimal volume = order.getVolume();
        BigDecimal profit = settle(order, order.getVolume());
        Wallet wallet = walletService.findByUserId(order.getPartyId());
        if (wallet.getMoney().add(profit).compareTo(BigDecimal.ZERO) < 0) {
            profit = wallet.getMoney().negate();
        }

        walletService.updateMoney(order.getSymbol(), partyId, profit, BigDecimal.ZERO,
                Constants.MONEYLOG_CATEGORY_CONTRACT, Constants.WALLET_USDT, Constants.MONEYLOG_CONTENT_CONTRACT_CLOSE, "平仓，平仓合约数[" + volume + "],订单号[" + order.getOrderNo() + "]");
        order.setState(ContractOrder.STATE_CREATED);
        order.setVolume(BigDecimal.ZERO);
        order.setDeposit(BigDecimal.ZERO);
        order.setCloseTime(DateUtil.currentSeconds());
        order.setCloseTimeTs(DateUtil.currentSeconds());
//        List<Realtime> list = this.dataService.realtime(order.getSymbol());
//        // 平仓时候把当前价格先更新回去
//        if (list.size() != 0) {
//            Realtime realtime = list.get(0);
//            BigDecimal close = realtime.getClose();
//            order.setCloseAvgPrice(close);
//        }
        update(order);

        /**
         * 合约产品平仓后添加当前流水setWithdraw_limit_now_amount
         */
        User party = userService.getById(order.getPartyId());
        party.setWithdrawLimitNowAmount(party.getWithdrawLimitNowAmount().add(order.getDepositOpen()));
        userService.updateById(party);
        if (ObjectUtils.isEmpty(order.getCloseAvgPrice())) {
            order.setCloseAvgPrice(BigDecimal.ZERO);
        }
        return order;

    }

    /**
     * 前台发起的，直接不缓存
     * updateByIdBuffer 只是更新利润强平价格的，不要处理state状态更新
     *
     * @param entity
     * @return
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void updateByIdBuffer(ContractOrder entity) {
        map.put(entity.getUuid(), entity);
    }

    public void update(ContractOrder order) {
        // 强制刷新
        updateById(order);
        // this.getHibernateTemplate().merge(order);
        RedisUtil.set(ContractRedisKeys.CONTRACT_ORDERNO + order.getOrderNo(), order);
        if (ContractOrder.STATE_SUBMITTED.equals(order.getState())) {

            Map<String, ContractOrder> map =
                    RedisUtil.get(ContractRedisKeys.CONTRACT_SUBMITTED_ORDER_PARTY_ID + order.getPartyId());
            if (null == map) {
                map = new ConcurrentHashMap<>();
            }

            ContractOrder orderOld = map.get(order.getOrderNo());
            map.put(order.getOrderNo(), order);
            RedisUtil.set(ContractRedisKeys.CONTRACT_SUBMITTED_ORDER_PARTY_ID + order.getPartyId(), map);

            // 获取单个订单的合约总资产、总保证金、总未实现盈利
            Map<String, BigDecimal> contractAssetsOrder = this.walletService.getMoneyContractByOrder(order);
            Map<String, BigDecimal> contractAssetsOrderOld = this.walletService.getMoneyContractByOrder(orderOld);

            BigDecimal contractAssets = RedisUtil.get(ContractRedisKeys.CONTRACT_ASSETS_PARTY_ID + order.getPartyId().toString());
            if (contractAssets == null) {
                contractAssets = BigDecimal.ZERO;
            }
            BigDecimal contractAssetsDeposit = RedisUtil.get(ContractRedisKeys.CONTRACT_ASSETS_DEPOSIT_PARTY_ID + order.getPartyId().toString());
            if (contractAssetsDeposit == null) {
                contractAssetsDeposit = BigDecimal.ZERO;
            }
            BigDecimal contractAssetsProfit = RedisUtil.get(ContractRedisKeys.CONTRACT_ASSETS_PROFIT_PARTY_ID + order.getPartyId().toString());
            if (contractAssetsProfit == null) {
                contractAssetsProfit = BigDecimal.ZERO;
            }

            RedisUtil.set(ContractRedisKeys.CONTRACT_ASSETS_PARTY_ID + order.getPartyId().toString(),
                    contractAssets.add(contractAssetsOrder.get("money_contract")).subtract(contractAssetsOrderOld.get("money_contract")));
            RedisUtil.set(ContractRedisKeys.CONTRACT_ASSETS_DEPOSIT_PARTY_ID + order.getPartyId().toString(),
                    contractAssetsDeposit.add(contractAssetsOrder.get("money_contract_deposit")).subtract(contractAssetsOrderOld.get("money_contract_deposit")));
            RedisUtil.set(ContractRedisKeys.CONTRACT_ASSETS_PROFIT_PARTY_ID + order.getPartyId().toString(),
                    contractAssetsProfit.add(contractAssetsOrder.get("money_contract_profit")).subtract(contractAssetsOrderOld.get("money_contract_profit")));

        } else if (ContractOrder.STATE_CREATED.equals(order.getState())) {
            // 平仓后，移除持仓列表

            Map<String, ContractOrder> map = RedisUtil.get(ContractRedisKeys.CONTRACT_SUBMITTED_ORDER_PARTY_ID + order.getPartyId().toString());
            ContractOrder orderOld = null;
            if (map != null && !map.isEmpty()) {
                orderOld = map.get(order.getOrderNo());
                map.remove(order.getOrderNo());
            }
            RedisUtil.set(ContractRedisKeys.CONTRACT_SUBMITTED_ORDER_PARTY_ID + order.getPartyId().toString(), map);

            // 获取单个订单的合约总资产、总保证金、总未实现盈利
            Map<String, BigDecimal> contractAssetsOrderOld = walletService.getMoneyContractByOrder(orderOld);

            BigDecimal contractAssets = RedisUtil.get(ContractRedisKeys.CONTRACT_ASSETS_PARTY_ID + order.getPartyId().toString());
            if (contractAssets == null) {
                contractAssets = BigDecimal.ZERO;
            }
            BigDecimal contractAssetsDeposit = RedisUtil.get(ContractRedisKeys.CONTRACT_ASSETS_DEPOSIT_PARTY_ID + order.getPartyId().toString());
            if (contractAssetsDeposit == null) {
                contractAssetsDeposit = BigDecimal.ZERO;
            }
            BigDecimal contractAssetsProfit = RedisUtil.get(ContractRedisKeys.CONTRACT_ASSETS_PROFIT_PARTY_ID + order.getPartyId().toString());
            if (contractAssetsProfit == null) {
                contractAssetsProfit = BigDecimal.ZERO;
            }

            RedisUtil.set(ContractRedisKeys.CONTRACT_ASSETS_PARTY_ID + order.getPartyId().toString(),
                    contractAssets.subtract(contractAssetsOrderOld.get("money_contract")));
            RedisUtil.set(ContractRedisKeys.CONTRACT_ASSETS_DEPOSIT_PARTY_ID + order.getPartyId().toString(),
                    contractAssetsDeposit.subtract(contractAssetsOrderOld.get("money_contract_deposit")));
            RedisUtil.set(ContractRedisKeys.CONTRACT_ASSETS_PROFIT_PARTY_ID + order.getPartyId().toString(),
                    contractAssetsProfit.subtract(contractAssetsOrderOld.get("money_contract_profit")));

            // 平仓则纪录数据（委托平仓，订单直接平仓）
            this.userDataService.saveClose(order);
            User party = userService.getById(order.getPartyId());

            if (Constants.SECURITY_ROLE_MEMBER.equals(party.getRoleName())) {
                tipService.deleteTip(order.getUuid().toString());
            }
        }
    }


    /**
     * 收益结算，平仓时计算
     *closevolume 平仓的张数
     * @param
     */
    public BigDecimal settle(ContractOrder order, BigDecimal volume) {
        /**
         * 平仓比率
         */
        BigDecimal rate = volume.divide(order.getVolumeOpen(), 2, RoundingMode.HALF_UP);
        BigDecimal profit = order.getDeposit().add(order.getProfit()).multiply(rate);
        order.setAmountClose(order.getAmountClose().add(profit));
        order.setVolume(order.getVolume().subtract(volume));
        order.setDeposit(order.getDeposit().subtract(order.getDepositOpen().multiply(rate)));
        if (order.getVolume().compareTo(BigDecimal.ZERO) <= 0) {
            order.setState(ContractOrder.STATE_CREATED);
            order.setCloseTime(DateUtil.currentSeconds());
            order.setCloseTimeTs(DateUtil.currentSeconds());

        }
        return profit;
    }



    public void saveOpen(ContractApplyOrder applyOrder, Realtime realtime) {
        Item item = this.itemService.findBySymbol(applyOrder.getSymbol());

        ContractOrder order = new ContractOrder();
        order.setPartyId(applyOrder.getPartyId());
        order.setSymbol(applyOrder.getSymbol());
        String orderNo = com.yami.trading.common.util.DateUtil.formatDate(new Date(), "yyMMddHHmmss") + RandomUtil.getRandomNum(8);
        order.setOrderNo(orderNo);
        order.setDirection(applyOrder.getDirection());
        order.setLeverRate(applyOrder.getLeverRate());
        order.setVolume(applyOrder.getVolume());
        order.setVolumeOpen(applyOrder.getVolumeOpen());
        order.setOrderPriceType(applyOrder.getOrderPriceType());
        order.setUnitAmount(applyOrder.getUnitAmount());
        order.setFee(applyOrder.getFee());
        order.setDeposit(applyOrder.getDeposit());
        order.setDepositOpen(applyOrder.getDeposit());

        order.setTradeAvgPrice(realtime.getClose());
        order.setStopPriceProfit(applyOrder.getStopPriceProfit());
        order.setStopPriceLoss(applyOrder.getStopPriceLoss());

        order.setPips(item.getPips());
        order.setPipsAmount(item.getPipsAmount());
        // 爆仓是爆整个钱包
//        BigDecimal forceClose = BigDecimal.ZERO;
//        BigDecimal base = order.getDepositOpen().multiply(order.getPips()).divide(order.getPipsAmount(), 10, RoundingMode.HALF_UP).divide(order.getVolume(),10, RoundingMode.HALF_UP);
//        if(order.getDirection().equalsIgnoreCase(ContractOrder.DIRECTION_BUY)){
//            forceClose = order.getTradeAvgPrice().subtract(base).setScale(item.getDecimals(), RoundingMode.HALF_UP);
//        }else if(order.getDirection().equalsIgnoreCase(ContractOrder.DIRECTION_SELL)) {
//            forceClose = order.getTradeAvgPrice().add(base).setScale(item.getDecimals(), RoundingMode.HALF_UP);
//        }
//        if(forceClose.compareTo(BigDecimal.ZERO) <0 ){
//            forceClose  =  BigDecimal.ZERO;
//        }
//        order.setForceClosePrice(forceClose.toPlainString());
        save(order);

        RedisUtil.set(ContractRedisKeys.CONTRACT_ORDERNO + order.getOrderNo(), order);

        Map<String, ContractOrder> map = RedisUtil
                .get(ContractRedisKeys.CONTRACT_SUBMITTED_ORDER_PARTY_ID + order.getPartyId().toString());
        if (map == null) {
            map = new ConcurrentHashMap<String, ContractOrder>();
        }
        map.put(order.getOrderNo(), order);
        RedisUtil.set(ContractRedisKeys.CONTRACT_SUBMITTED_ORDER_PARTY_ID + order.getPartyId().toString(), map);

        // 获取单个订单的合约总资产、总保证金、总未实现盈利
        Map<String, BigDecimal> contractAssetsOrder = this.walletService.getMoneyContractByOrder(order);

        BigDecimal contractAssets = RedisUtil.get(ContractRedisKeys.CONTRACT_ASSETS_PARTY_ID + order.getPartyId().toString());
        if (contractAssets == null) {
            contractAssets = BigDecimal.ZERO;
        }
        BigDecimal contractAssetsDeposit = RedisUtil.get(ContractRedisKeys.CONTRACT_ASSETS_DEPOSIT_PARTY_ID + order.getPartyId().toString());
        if (contractAssetsDeposit == null) {
            contractAssetsDeposit = BigDecimal.ZERO;
        }
        BigDecimal contractAssetsProfit = RedisUtil.get(ContractRedisKeys.CONTRACT_ASSETS_PROFIT_PARTY_ID + order.getPartyId().toString());
        if (contractAssetsProfit == null) {
            contractAssetsProfit = BigDecimal.ZERO;
        }
        RedisUtil.set(ContractRedisKeys.CONTRACT_ASSETS_PARTY_ID + order.getPartyId().toString(),
                contractAssets.add(contractAssetsOrder.get("money_contract")));
        RedisUtil.set(ContractRedisKeys.CONTRACT_ASSETS_DEPOSIT_PARTY_ID + order.getPartyId().toString(),
                contractAssetsDeposit.add(contractAssetsOrder.get("money_contract_deposit")));
        RedisUtil.set(ContractRedisKeys.CONTRACT_ASSETS_PROFIT_PARTY_ID + order.getPartyId().toString(),
                contractAssetsProfit.add(contractAssetsOrder.get("money_contract_profit")));

        /**
         * 进入市场
         */
        applyOrder.setVolume(BigDecimal.ZERO);
        applyOrder.setState(ContractApplyOrder.STATE_CREATED);

        contractApplyOrderService.updateById(applyOrder);


        User party = this.userService.getById(order.getPartyId());
        if (Constants.SECURITY_ROLE_MEMBER.equals(party.getRoleName())) {
            tipService.saveTip(order.getUuid().toString(), TipConstants.CONTRACT_ORDER);
        }
    }

    public ContractApplyOrder saveClose(ContractApplyOrder applyOrder, Realtime realtime, String order_no) {
        ContractOrder order = this.findByOrderNo(order_no);
        if (order == null || !ContractOrder.STATE_SUBMITTED.equals(order.getState()) || order.getVolume().compareTo(BigDecimal.ZERO) <= 0) {
            /**
             * 状态已改变，退出处理
             */
            return applyOrder;
        }
        BigDecimal volume;
        if (applyOrder.getVolume().compareTo(order.getVolume()) > 0) {
            volume = order.getVolume();
        } else {
            volume = applyOrder.getVolume();
        }
        /**
         * 平仓退回的金额
         */
        BigDecimal profit = this.settle(order, volume);
        update(order);
        Wallet wallet = this.walletService.findByUserId(order.getPartyId());
        BigDecimal amount_before = wallet.getMoney();

        if (wallet.getMoney().add(profit).compareTo(BigDecimal.ZERO) < 0) {
            profit = wallet.getMoney().negate();
        }

        walletService.updateMoney(order.getSymbol(), order.getPartyId(), profit, BigDecimal.ZERO,
                Constants.MONEYLOG_CATEGORY_CONTRACT, Constants.WALLET_USDT, Constants.MONEYLOG_CONTENT_CONTRACT_CLOSE, "平仓，平仓合约数[" + volume + "],订单号[" + order.getOrderNo() + "]");


        applyOrder.setVolume(applyOrder.getVolume().subtract(volume));
        if (applyOrder.getVolume().compareTo(BigDecimal.ZERO) <= 0) {
            applyOrder.setState(ContractApplyOrder.STATE_CREATED);
        }
        contractApplyOrderService.updateById(applyOrder);

        return applyOrder;
    }

    public boolean lock(String order_no) {
        return ContractLock.add(order_no);

    }

    public void unlock(String order_no) {
        ContractLock.remove(order_no);

    }

    private List<Map<String, Object>> bulidData(List<ContractOrder> list) {
        List<Map<String, Object>> data = new ArrayList();

        for (int i = 0; i < list.size(); i++) {
            ContractOrder order = list.get(i);
            Map<String, Object> map = bulidOne(order);
            data.add(map);
        }
        return data;
    }

    public Map<String, Object> bulidOne(ContractOrder order) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("order_no", order.getOrderNo());
        Item bySymbol = itemService.findBySymbol(order.getSymbol());
        String name = "---";
        if(bySymbol != null){
            name = bySymbol.getName();
        }
        map.put("name", name);
        map.put("symbol", order.getSymbol());
        map.put("create_time", DateUtils.format(order.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
        map.put("create_time_ts", order.getCreateTimeTs());

        if (order.getCloseTime() != null) {
            map.put("close_time", order.getCloseTime());
        } else {
            map.put("close_time", "");
        }
        String orderPriceType = order.getOrderPriceType();
        if (StringUtils.isEmptyString(orderPriceType)) {
            orderPriceType = "opponent";
        }
        map.put("order_price_type", orderPriceType);

        map.put("direction", order.getDirection());
        map.put("lever_rate", order.getLeverRate());
        map.put("trade_avg_price", order.getTradeAvgPrice());
        map.put("close_avg_price", order.getCloseAvgPrice());
        if (order.getStopPriceProfit() != null) {
            map.put("stop_price_profit", order.getStopPriceProfit().setScale(4, RoundingMode.HALF_UP));
        }else{
            map.put("stop_price_profit", order.getStopPriceProfit());

        }
        if (order.getStopPriceLoss() != null) {
            map.put("stop_price_loss", order.getStopPriceLoss().setScale(4, RoundingMode.HALF_UP));
        }else{
            map.put("stop_price_loss", order.getStopPriceLoss());
        }
        map.put("state", order.getState());
        map.put("amount", order.getVolume().multiply(order.getUnitAmount()));
        map.put("amount_open", order.getVolumeOpen().multiply(order.getUnitAmount()));
        map.put("fee", order.getFee());
        map.put("deposit", order.getDeposit());
        map.put("deposit_open", order.getDepositOpen());
        map.put("change_ratio", order.getChangeRatio());
        /**
         * 收益
         */
//		if (ContractOrder.STATE_SUBMITTED.equals(order.getState())) {
//			map.put("profit",
//					df.format(Arith.sub(
//							Arith.add(Arith.add(order.getAmount_close(), order.getProfit()), order.getDeposit()),
//							order.getDeposit_open())));
//		} else {
//			map.put("profit", df.format(
//					Arith.sub(Arith.add(order.getAmount_close(), order.getDeposit()), order.getDeposit_open())));
//		}
        if(order.getProfit()!=null){
            map.put("profit", order.getProfit().setScale(4, RoundingMode.HALF_UP));
        }else{
            map.put("profit", order.getProfit());
        }
        map.put("volume", order.getVolume());
        map.put("volume_open", order.getVolumeOpen());

        return map;
    }

    /**
     * 根据用户批量赎回订单
     *
     * @param partyId
     */
    public void saveCloseRemoveAllByPartyId(String partyId) {
        QueryWrapper<ContractOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("party_id", partyId);

        List<ContractOrder> orders = list(queryWrapper);
        List<ContractOrder> findSubmittedContractOrders = findSubmitted(partyId, null, null);
        if (!CollectionUtils.isEmpty(findSubmittedContractOrders)) {
            for (ContractOrder order : orders) {
                if (ContractOrder.STATE_SUBMITTED.equals(order.getState())) {
                    saveClose(order.getPartyId().toString(), order.getOrderNo());
                }
                RedisUtil.del(ContractRedisKeys.CONTRACT_ORDERNO + order.getOrderNo());
            }
            RedisUtil.del(ContractRedisKeys.CONTRACT_SUBMITTED_ORDER_PARTY_ID + partyId);

            RedisUtil.del(ContractRedisKeys.CONTRACT_ASSETS_PARTY_ID + partyId);
            RedisUtil.del(ContractRedisKeys.CONTRACT_ASSETS_DEPOSIT_PARTY_ID + partyId);
            RedisUtil.del(ContractRedisKeys.CONTRACT_ASSETS_PROFIT_PARTY_ID + partyId);
        }
    }
}
