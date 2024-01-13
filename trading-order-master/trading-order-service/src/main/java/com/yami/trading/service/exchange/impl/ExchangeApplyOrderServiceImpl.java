package com.yami.trading.service.exchange.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.exchange.ExchangeApplyOrder;
import com.yami.trading.bean.exchange.dto.ExchangeApplyOrderDto;
import com.yami.trading.bean.exchange.dto.ExchangeSymbolDto;
import com.yami.trading.bean.exchange.dto.SumEtfDto;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.item.dto.RealtimeDTO;
import com.yami.trading.bean.item.dto.SymbolDTO;
import com.yami.trading.bean.model.MoneyLog;
import com.yami.trading.bean.model.RealNameAuthRecord;
import com.yami.trading.bean.model.Wallet;
import com.yami.trading.bean.model.WalletExtend;
import com.yami.trading.bean.purchasing.dto.ExchangeRecord;
import com.yami.trading.bean.rate.domain.ExchangeRate;
import com.yami.trading.bean.syspara.domain.OpenClose;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.*;
import com.yami.trading.dao.WalletExtendMapper;
import com.yami.trading.dao.exchange.ExchangeApplyOrderMapper;
import com.yami.trading.service.MoneyLogService;
import com.yami.trading.service.WalletService;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.exchange.ExchangeApplyOrderService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.syspara.OpenCloseService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.user.UserDataService;
import com.yami.trading.service.user.WalletExtendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.crypto.Data;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.yami.trading.bean.item.domain.ItemConstants.*;

@Service
@Slf4j
public class ExchangeApplyOrderServiceImpl extends ServiceImpl<ExchangeApplyOrderMapper, ExchangeApplyOrder> implements ExchangeApplyOrderService {
    @Autowired
    SysparaService sysparaService;
    @Autowired
    ItemService itemService;
    @Autowired
    DataService dataService;
    @Autowired
    WalletService walletService;
    @Autowired
    MoneyLogService moneyLogService;
    @Autowired
    UserDataService userDataService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    WalletExtendMapper walletExtendMapper;
    @Autowired
    private OpenCloseService openCloseService;

    @Override
    public List<ExchangeApplyOrder> findSubmitted() {

        return list(Wrappers.<ExchangeApplyOrder>query().lambda().eq(ExchangeApplyOrder::getState, ExchangeApplyOrder.STATE_SUBMITTED));
    }


    @Override
    @Transactional
    public void saveOpens(ExchangeApplyOrder order, RealtimeDTO realtime) {
        double sub = Arith.sub(order.getVolume(), order.getFee());// 买入数量-手续费=到账
        double amount = Arith.div(sub, Double.parseDouble(realtime.getLast()), 8);// 可以买的数量
        order.setCloseTime(new Date());
        order.setClosePrice(Double.parseDouble(realtime.getLast()));
        WalletExtend walletExtend = walletService.saveExtendByPara(order.getPartyId(), order.getSymbol());
        this.walletService.updateExtend(walletExtend.getPartyId().toString(), walletExtend.getWallettype(), amount);
        order.setState(ExchangeApplyOrder.STATE_CREATED);
        order.setAmount(order.getVolume());
        order.setWalletFee(order.getFee());
        updateById(order);
    }

    @Override
    @Transactional
    public void saveOpen(ExchangeApplyOrder order, JSONObject msgObject) {
        double sub = Arith.sub(order.getVolume(), order.getFee());// 买入数量-手续费=到账
        double amount = Arith.div(sub, Double.parseDouble(msgObject.getStr("last")), 8);// 可以买的数量
        order.setCloseTime(new Date());
        order.setClosePrice(Double.parseDouble(msgObject.getStr("last")));
        WalletExtend walletExtend = walletService.saveExtendByPara(order.getPartyId(), order.getSymbol());
        this.walletService.updateExtend(walletExtend.getPartyId().toString(), walletExtend.getWallettype(), amount);
        order.setState(ExchangeApplyOrder.STATE_CREATED);
        order.setAmount(order.getVolume());
        order.setWalletFee(order.getFee());
        updateById(order);
    }

    @Override
    @Transactional
    public void saveCloses(ExchangeApplyOrder order, RealtimeDTO realtime) {
        order.setFee(Arith.mul(order.getVolume() * Double.parseDouble(realtime.getLast()), sysparaService.find("exchange_apply_order_sell_fee").getDouble()));
        double amount = Arith.mul(order.getVolume(), Double.parseDouble(realtime.getLast()));
        amount = Arith.sub(amount, order.getFee());
        order.setCloseTime(new Date());
        order.setClosePrice(Double.parseDouble(realtime.getLast()));
        Wallet wallet = this.walletService.saveWalletByPartyId(order.getPartyId());
        double amount_before = wallet.getMoney().doubleValue();
        this.walletService.update(wallet.getUserId().toString(), amount);
        /*
         * 保存资金日志
         */
        MoneyLog moneylog_deposit = new MoneyLog();
        moneylog_deposit.setCategory(Constants.MONEYLOG_CATEGORY_EXCHANGE);
        moneylog_deposit.setAmountBefore(new BigDecimal(amount_before));
        moneylog_deposit.setAmount(new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP));
        moneylog_deposit.setAmountAfter(wallet.getMoney());
        moneylog_deposit.setLog("委托单，订单号[" + order.getOrderNo() + "]");
        moneylog_deposit.setTitle("Closings position");//平仓委托单卖出金额
        moneylog_deposit.setConf("Selling");//卖出金额
        moneylog_deposit.setUserId(order.getPartyId());
        moneylog_deposit.setWalletType(Constants.WALLET);
        moneylog_deposit.setContentType(Constants.MONEYLOG_CONTENT_EXCHANGE_CLOSE);
        moneylog_deposit.setSymbol(order.getSymbol());
        moneyLogService.save(moneylog_deposit);
        order.setAmount(amount);
        order.setWalletFee(Arith.mul(order.getFee(), Double.parseDouble(realtime.getLast())));
        order.setState(ExchangeApplyOrder.STATE_CREATED);
        updateById(order);
    }

    @Override
    @Transactional
    public void saveClose(ExchangeApplyOrder order, JSONObject msgObject) {
        //double sub = Arith.sub(order.getVolume(), order.getFee());
        //double amount = Arith.mul(sub, Double.parseDouble(realtime.getLast()));
        order.setFee(Arith.mul(order.getVolume() * Double.parseDouble(msgObject.getStr("last")), sysparaService.find("exchange_apply_order_sell_fee").getDouble()));
        double amount = Arith.mul(order.getVolume(), Double.parseDouble(msgObject.getStr("last")));
        amount = Arith.sub(amount, order.getFee());

        order.setCloseTime(new Date());
        order.setClosePrice(Double.parseDouble(msgObject.getStr("last")));
        Wallet wallet = this.walletService.saveWalletByPartyId(order.getPartyId());
        double amount_before = wallet.getMoney().doubleValue();
        this.walletService.update(wallet.getUserId().toString(), amount);
        /*
         * 保存资金日志
         */
        MoneyLog moneylog_deposit = new MoneyLog();
        moneylog_deposit.setCategory(Constants.MONEYLOG_CATEGORY_EXCHANGE);
        moneylog_deposit.setAmountBefore(new BigDecimal(amount_before));
        moneylog_deposit.setAmount(new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP));
        moneylog_deposit.setAmountAfter(wallet.getMoney());
        moneylog_deposit.setLog("委托单，订单号[" + order.getOrderNo() + "]");
        moneylog_deposit.setTitle("Closing position");//平仓委托单卖出金额
        moneylog_deposit.setConf("Selling");//卖出金额
        moneylog_deposit.setUserId(order.getPartyId());
        moneylog_deposit.setWalletType(Constants.WALLET);
        moneylog_deposit.setContentType(Constants.MONEYLOG_CONTENT_EXCHANGE_CLOSE);
        moneylog_deposit.setSymbol(order.getSymbol());
        moneyLogService.save(moneylog_deposit);
        order.setAmount(amount);
        order.setWalletFee(Arith.mul(order.getFee(), Double.parseDouble(msgObject.getStr("last"))));
        order.setState(ExchangeApplyOrder.STATE_CREATED);
        updateById(order);
    }

    @Override
    public void saveCreate(ExchangeApplyOrder order) {
        boolean order_open = sysparaService.find("exchange_order_open").getBoolean();
        if (!order_open) {
            throw new YamiShopBindException("不在交易时段");
        }
        Item item = itemService.findBySymbol(order.getSymbol());
        if (item == null) {
            throw new YamiShopBindException("参数错误");
        }
        List<Realtime> realtimes = dataService.realtime(order.getSymbol());
        double close = 1;
        if (realtimes != null && realtimes.size() > 0) {
            close = realtimes.get(0).getClose().doubleValue();
        }
        order.setClosePrice(close);
        if (ExchangeApplyOrder.OFFSET_OPEN.equals(order.getOffset())) {
            open(order);
        } else if (ExchangeApplyOrder.OFFSET_CLOSE.equals(order.getOffset())) {
            close(order);
        }
    }

    @Override
    public void saveCreateyd(ExchangeApplyOrder order, String pid) {
        boolean order_open = sysparaService.find("exchange_order_open").getBoolean();
        if (!order_open) {
            throw new YamiShopBindException("不在交易时段");
        }
        Item item = itemService.findByPid(pid);
        if (item == null) {
            throw new YamiShopBindException("参数错误null");
        }
        Object results = redisTemplate.opsForValue().get("ydTask" + pid);
        cn.hutool.json.JSONObject msgObject = JSONUtil.parseObj(results);
        double close = 1;
        if (msgObject != null) {
            close = Double.parseDouble(msgObject.getStr("last"));
        } else {
            throw new YamiShopBindException("参数错误");
        }
        order.setClosePrice(close);
        if (ExchangeApplyOrder.OFFSET_OPEN.equals(order.getOffset())) {
            openYd(order, pid);
        } else if (ExchangeApplyOrder.OFFSET_CLOSE.equals(order.getOffset())) {
            closeYd(order, pid);
        }
    }

    @Override
    public ExchangeApplyOrder findByOrderNo(String orderNo) {

        return getOne(Wrappers.<ExchangeApplyOrder>query().lambda().eq(ExchangeApplyOrder::getOrderNo, orderNo));
    }

    @Override
    public List<ExchangeSymbolDto> getETFListByUserId(String userId, String type) {
        List<String> symbols = itemService.findByType(type).stream().map(Item::getSymbol).collect(Collectors.toList());
        symbols.add("-1");
        log.info(JSONUtil.toJsonStr(symbols));
        LambdaQueryWrapper<ExchangeApplyOrder> lambdaQueryWrapper = Wrappers.<ExchangeApplyOrder>query().lambda();
        lambdaQueryWrapper.eq(ExchangeApplyOrder::getPartyId, userId);
        lambdaQueryWrapper.in(ExchangeApplyOrder::getSymbol, symbols);
        lambdaQueryWrapper.notIn(ExchangeApplyOrder::getOffset, ExchangeApplyOrder.OFFSET_CLOSE);
        lambdaQueryWrapper.eq(ExchangeApplyOrder::getState, ExchangeApplyOrder.STATE_CREATED);
        lambdaQueryWrapper.orderByDesc(ExchangeApplyOrder::getCreateTime);
        List<ExchangeApplyOrder> list = list(lambdaQueryWrapper);
        return getDataList(list);
    }

    public List<ExchangeSymbolDto> getDataList(List<ExchangeApplyOrder> dbList) {
        List<ExchangeSymbolDto> result = new ArrayList<>();
        Map<String, List<ExchangeSymbolDto>> map = new HashMap<>();
        for (ExchangeApplyOrder order : dbList) {
            QueryWrapper<WalletExtend> walletExtendWrapper = new QueryWrapper<>();
            walletExtendWrapper.eq("wallettype", order.getSymbol());
            walletExtendWrapper.eq("party_id", order.getPartyId());
            WalletExtend walletExtend = walletExtendMapper.selectOne(walletExtendWrapper);
            if (walletExtend == null || walletExtend.getAmount() <= 0) {
                continue;
            }
            List<ExchangeSymbolDto> exchangeSymbolDtos = map.get(order.getSymbol());
            if (exchangeSymbolDtos == null) {
                exchangeSymbolDtos = new ArrayList<>();
            }
            Item item = itemService.findBySymbol(order.getSymbol());
            Object results = redisTemplate.opsForValue().get("ydTask" + item.getPid());//实时价格
            String ydList = (String) redisTemplate.opsForValue().get("yd" + order.getSymbol() + item.getPid());//取list
            if (StringUtils.isNotEmpty(ydList) && results != null) {
                JSONObject msgObject = JSONUtil.parseObj(results);
                com.alibaba.fastjson2.JSONObject jsonObject = com.alibaba.fastjson2.JSONObject.parseObject(ydList);
                BigDecimal open = jsonObject.getBigDecimal("Open");
                ExchangeSymbolDto exchangeSymbolDto = new ExchangeSymbolDto();
                exchangeSymbolDto.setVolume(order.getVolume());
                exchangeSymbolDto.setName(item.getName());
                exchangeSymbolDto.setPid(item.getPid());
                exchangeSymbolDto.setOpenPrice(open.doubleValue());
                exchangeSymbolDto.setCurrentPrice(Double.parseDouble(msgObject.getStr("last")));//现价
                exchangeSymbolDto.setPrice(order.getClosePrice());
                exchangeSymbolDto.setSymbol(order.getSymbol());
                exchangeSymbolDto.setPartyId(order.getPartyId());
                exchangeSymbolDto.setQuantity(new BigDecimal(walletExtend.getAmount()));
                exchangeSymbolDtos.add(exchangeSymbolDto);
                map.put(order.getSymbol(), exchangeSymbolDtos);
            }
        }
        for (String key : map.keySet()) {
            List<ExchangeSymbolDto> list = map.get(key);
            double volume = 0; //可用
            double cost = 0.0;
            double marketValue = 0; //市值
            double currentPrice = 0; //当前价格
            double profitLoss = 0; //总盈亏
            double toDayProfitLoss = 0; //今日总盈亏
            double openPrice = 0;
            double prices = 0;
            String pid = null;
            String name = "";
            double volumes = 0;
            BigDecimal quantity = new BigDecimal(0);

            for (ExchangeSymbolDto dto : list) {
                volume += dto.getVolume();
                name = dto.getName();
                cost += (dto.getVolume() * dto.getPrice());
                currentPrice = dto.getCurrentPrice();//现价
                openPrice = dto.getOpenPrice();//开盘价
                marketValue += (dto.getCurrentPrice() * dto.getVolume());
                prices = dto.getPrice();
                pid = dto.getPid();
                quantity = dto.getQuantity();
            }
            LambdaQueryWrapper<ExchangeApplyOrder> lambdaQueryWrappers = Wrappers.<ExchangeApplyOrder>query().lambda();
            lambdaQueryWrappers.eq(ExchangeApplyOrder::getPartyId, list.get(0).getPartyId());
            lambdaQueryWrappers.in(ExchangeApplyOrder::getSymbol, list.get(0).getSymbol());
            lambdaQueryWrappers.in(ExchangeApplyOrder::getOffset, "close");
            lambdaQueryWrappers.eq(ExchangeApplyOrder::getState, ExchangeApplyOrder.STATE_CREATED);
            lambdaQueryWrappers.orderByDesc(ExchangeApplyOrder::getCreateTime);
            List<ExchangeApplyOrder> lists = list(lambdaQueryWrappers);
            for (ExchangeApplyOrder orders : lists) {
                volumes += orders.getVolume() * orders.getPrice();
            }
            double price = 0;
            if (volume == 0) {
                price = 0;
            } else {
                price = cost / volume;
            }
            ExchangeSymbolDto exchangeSymbolDto = new ExchangeSymbolDto();
            double vPrice = currentPrice * quantity.doubleValue();
            double xPrice = price * quantity.doubleValue();

            exchangeSymbolDto.setVolume(new BigDecimal(vPrice).setScale(2, RoundingMode.HALF_UP).doubleValue());
            exchangeSymbolDto.setPositionVolume(new BigDecimal(xPrice).setScale(2, RoundingMode.HALF_UP).doubleValue());

            exchangeSymbolDto.setPrice(new BigDecimal(price).setScale(2, RoundingMode.HALF_UP).doubleValue());

            exchangeSymbolDto.setName(name);
            exchangeSymbolDto.setSymbol(key);
            exchangeSymbolDto.setToDayProfitLoss(new BigDecimal(currentPrice - openPrice).setScale(2, RoundingMode.HALF_UP).doubleValue());
            exchangeSymbolDto.setToDayProfitLossPercentage(calculateProfitPercentagesopen(openPrice, currentPrice));
            exchangeSymbolDto.setCurrentPrice(new BigDecimal(currentPrice).setScale(2, RoundingMode.HALF_UP).doubleValue());
            exchangeSymbolDto.setOpenPrice(new BigDecimal(openPrice).setScale(2, RoundingMode.HALF_UP).doubleValue());
            exchangeSymbolDto.setMarketValue(new BigDecimal(marketValue).setScale(2, RoundingMode.HALF_UP).doubleValue());
            BigDecimal profitLosss = new BigDecimal(currentPrice - exchangeSymbolDto.getPrice()).setScale(2, RoundingMode.HALF_UP);
            exchangeSymbolDto.setProfitLoss(quantity.multiply(profitLosss).doubleValue());
            exchangeSymbolDto.setProfitLossPercentage(calculateProfitPercentages(exchangeSymbolDto.getPrice(), currentPrice));
            exchangeSymbolDto.setPid(pid);
            exchangeSymbolDto.setQuantity(quantity);
            result.add(exchangeSymbolDto);
        }
        return result;
    }

    public static double calculateProfitPercentages(double prices, double currentPrice) {
        BigDecimal profit = new BigDecimal(currentPrice).subtract(new BigDecimal(prices));
        BigDecimal profits = profit.setScale(2, RoundingMode.HALF_UP);
        double profitPercentage = (profits.doubleValue() / prices) * 100;
        System.out.println(profitPercentage);
        return new BigDecimal(profitPercentage).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static double calculateProfitPercentagesopen(double openPrice, double currentPrice) {
        double profit = currentPrice - openPrice;
        double profitPercentage = (profit / currentPrice) * 100;
        System.out.println(profitPercentage);
        return new BigDecimal(profitPercentage).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static double calculateProfitPercentage(double buyPrice, double currentPrice) {

        double profit = currentPrice - buyPrice;
        double profitPercentage = (profit / buyPrice) * 100;
        System.out.println(profitPercentage);
        return new BigDecimal(profitPercentage).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public SumEtfDto getProfitLossByUserId(String userId, String type) {
        List<ExchangeSymbolDto> list = getETFListByUserId(userId, type);
        double profitLoss = 0;  //盈亏
        double toDayProfitLoss = 0; //今日盈亏
        // double sumPrice = 0;  //总资产
        //double sumVolume = 0; //可用
        for (ExchangeSymbolDto order : list) {
            profitLoss += order.getProfitLoss();
            toDayProfitLoss += order.getToDayProfitLoss();
            // sumPrice += order.getMarketValue();
        }
        SumEtfDto sumEtfDto = new SumEtfDto();
        sumEtfDto.setProfitLoss(new BigDecimal(profitLoss).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        sumEtfDto.setToDayProfitLoss(new BigDecimal(toDayProfitLoss).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        //余额
        Wallet wallet = new Wallet();
        if (!"".equals(userId) && userId != null) {
            wallet = walletService.findByUserId(userId);
        }
        sumEtfDto.setSumVolume(wallet.getMoney().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        sumEtfDto.setSumPrice(wallet.getMoney().setScale(2, BigDecimal.ROUND_HALF_UP).add(new BigDecimal(profitLoss).setScale(2, BigDecimal.ROUND_HALF_UP)).doubleValue());

        return sumEtfDto;
    }

    @Transactional
    @Override
    public void saveCancel(String partyId, String order_no) {//撤单支行方法
        ExchangeApplyOrder order = findByOrderNo(order_no);
        if (order == null || !"submitted".equals(order.getState()) || !partyId.equals(order.getPartyId().toString())) {
            return;
        }
        // 如果是计划委托则不返回余额
        if (order.isTriggerOrder()) {
            if (ExchangeApplyOrder.OFFSET_OPEN.equals(order.getOffset())) {
                Wallet wallet = this.walletService.saveWalletByPartyId(order.getPartyId());
                double amount_before = wallet.getMoney().doubleValue();
                MoneyLog moneylog = new MoneyLog();
                moneylog.setCategory(Constants.MONEYLOG_CATEGORY_EXCHANGE);
                moneylog.setAmountBefore(new BigDecimal(amount_before));
                moneylog.setAmount(new BigDecimal(0));
                moneylog.setAmountAfter(new BigDecimal(amount_before));
                moneylog.setLog("股票交易计划委托单撤单，订单号[" + order.getOrderNo() + "]");
                moneylog.setTitle("Stock cancellation trading");//股票撤单交易
                moneylog.setConf("cancellation.");//撤单
                moneylog.setUserId(order.getPartyId());
                moneylog.setWalletType(Constants.WALLET);
                moneylog.setSymbol(order.getSymbol());
                moneylog.setContentType(Constants.MONEYLOG_CONTENT_EXCHANGE_CANCEL);
                moneyLogService.save(moneylog);
            } else if (ExchangeApplyOrder.OFFSET_CLOSE.equals(order.getOffset())) {
                WalletExtend walletExtend = walletService.saveExtendByPara(order.getPartyId(), order.getSymbol());
                double amount_before = walletExtend.getAmount();
                /*
                 * 保存资金日志
                 */
                MoneyLog moneylog = new MoneyLog();
                moneylog.setCategory(Constants.MONEYLOG_CATEGORY_EXCHANGE);
                moneylog.setAmountBefore(new BigDecimal(amount_before));
                moneylog.setAmount(new BigDecimal(0));
                moneylog.setAmountAfter(new BigDecimal(amount_before));
                moneylog.setLog("股票交易计划委托单撤单，订单号[" + order.getOrderNo() + "]");
                moneylog.setTitle("Stock cancellation trading");//股票撤单交易Stock cancellation trading
                moneylog.setConf("cancellation..");//Stock cancellation trading
                moneylog.setUserId(order.getPartyId());
                moneylog.setWalletType(order.getSymbol());
                moneylog.setSymbol(order.getSymbol());
                moneylog.setContentType(Constants.MONEYLOG_CONTENT_EXCHANGE_CANCEL);
                moneyLogService.save(moneylog);
            }
            order.setState(ExchangeApplyOrder.STATE_CANCELED);
        }
        if (!order.isTriggerOrder()) {
            if (ExchangeApplyOrder.OFFSET_OPEN.equals(order.getOffset())) {
                Wallet wallet = this.walletService.saveWalletByPartyId(order.getPartyId());
                double amount_before = wallet.getMoney().doubleValue();
                walletService.update(wallet.getUserId().toString(), order.getVolume());
                MoneyLog moneylog = new MoneyLog();
                moneylog.setCategory(Constants.MONEYLOG_CATEGORY_EXCHANGE);
                moneylog.setAmountBefore(new BigDecimal(amount_before));
                moneylog.setAmount(new BigDecimal(order.getVolume()));
                moneylog.setAmount_after(wallet.getMoney());
                moneylog.setLog("股票交易撤单，订单号[" + order.getOrderNo() + "]");
                moneylog.setTitle("Stock cancellation trading");//股票撤单交易
                moneylog.setConf("cancellation...");//cancellation
                moneylog.setUserId(order.getPartyId());
                moneylog.setWalletType(Constants.WALLET);
                moneylog.setContentType(Constants.MONEYLOG_CONTENT_EXCHANGE_CANCEL);
                moneylog.setSymbol(order.getSymbol());
                moneyLogService.save(moneylog);
            } else if (ExchangeApplyOrder.OFFSET_CLOSE.equals(order.getOffset())) {
                WalletExtend walletExtend = walletService.saveExtendByPara(order.getPartyId(), order.getSymbol());
                double amount_before = walletExtend.getAmount();
                walletService.updateExtend(walletExtend.getPartyId().toString(), walletExtend.getWallettype(),
                        order.getVolume());
                /*
                 * 保存资金日志
                 */
                MoneyLog moneylog = new MoneyLog();
                moneylog.setCategory(Constants.MONEYLOG_CATEGORY_EXCHANGE);
                moneylog.setAmountBefore(new BigDecimal(amount_before));
                moneylog.setAmount(new BigDecimal(order.getVolume()));
                moneylog.setAmountAfter(new BigDecimal(Arith.add(walletExtend.getAmount(), order.getVolume())));
                moneylog.setLog("股票交易撤单，订单号[" + order.getOrderNo() + "]");
                moneylog.setTitle("Stock cancellation trading");//股票撤单交易
                moneylog.setConf("cancellation....");//cancellation
                moneylog.setUserId(order.getPartyId());
                moneylog.setWalletType(order.getSymbol());
                moneylog.setSymbol(order.getSymbol());
                moneylog.setContentType(Constants.MONEYLOG_CONTENT_EXCHANGE_CANCEL);
                moneyLogService.save(moneylog);
            }
            order.setState(ExchangeApplyOrder.STATE_CANCELED);
        }
        updateById(order);
    }

    @Override
    public Page<ExchangeApplyOrderDto> listPage(Page page, String rolename, String userName, String orderNo, String state, String offset, String symbolType, String userCode, String symbol) {

        return baseMapper.listPage(page, rolename, userName, orderNo, state, offset, symbolType, userCode, symbol);
    }

    @Override
    public Page<ExchangeApplyOrderDto> listPages(Page page, String rolename, String userName, String orderNo, String state, String offset, String symbolType, String userCode, String symbol, String recomCode) {

        return baseMapper.listPages(page, rolename, userName, orderNo, state, offset, symbolType, userCode, symbol, recomCode);
    }

    @Override
    public ExchangeApplyOrder findByOrderNoAndPartyId(String order_no, String userId) {

        return getOne(Wrappers.<ExchangeApplyOrder>query().lambda().eq(ExchangeApplyOrder::getOrderNo, order_no).eq(ExchangeApplyOrder::getPartyId, userId));
    }

    @Override
    public List<Map<String, Object>> getPageds(int pageNo, int size, String userId, String type) {
        QueryWrapper<ExchangeApplyOrder> lambdaQueryWrapper = new QueryWrapper<>();
        lambdaQueryWrapper.select("DISTINCT symbol")
                .lambda()
                .eq(ExchangeApplyOrder::getPartyId, userId)
                .in(ExchangeApplyOrder::getState, ExchangeApplyOrder.STATE_CREATED, ExchangeApplyOrder.STATE_CANCELED)
                .orderByDesc(ExchangeApplyOrder::getCreateTime);
        Page page = new Page(pageNo, size);
        page(page, lambdaQueryWrapper);
        List<Map<String, Object>> data = new ArrayList<>();
        if (page.getRecords().size() > 0) {
            data = this.bulidDatas(page.getRecords());
        } else {
            //Result.succeed("历史股票无数据");
            Map<String, Object> map = new HashMap<String, Object>();
            Object results = redisTemplate.opsForValue().get("ydTask" + "39540");
            JSONObject msgObject = JSONUtil.parseObj(results);
            map.put("symbol", "BSE_IDFC");
            map.put("name", "IDFC");
            map.put("closePrice", new BigDecimal(msgObject.getStr("last")));
            map.put("chgs", msgObject.getStr("pcp"));
            map.put("pid", "39540");
            data.add(map);
        }
        return data;
    }

    private List<Map<String, Object>> bulidDatas(List<ExchangeApplyOrder> list) {
        List<Map<String, Object>> data = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            ExchangeApplyOrder order = list.get(i);
            Map<String, Object> map = new HashMap<String, Object>();
            Item item = itemService.findBySymbol(order.getSymbol());
            if (item != null) {
                Object results = redisTemplate.opsForValue().get("ydTask" + item.getPid());
                if (results != null) {
                    JSONObject msgObject = JSONUtil.parseObj(results);
                    map.put("symbol", order.getSymbol());
                    map.put("name", item.getName());
                    map.put("closePrice", new BigDecimal(msgObject.getStr("last")));
                    map.put("chgs", msgObject.getStr("pcp"));
                    map.put("pid", item.getPid());
                    data.add(map);
                }
            }
        }
        return data;
    }

    @Override
    public List<Map<String, Object>> getPaged(int pageNo, int size, String userId, String symbol, String type, String isAll, String startTime, String endTime, String symbolType,
                                              String orderPriceType) {

        LambdaQueryWrapper<ExchangeApplyOrder> lambdaQueryWrapper = Wrappers.<ExchangeApplyOrder>query().lambda();
        lambdaQueryWrapper.eq(ExchangeApplyOrder::getPartyId, userId);
        if (!StringUtils.isNullOrEmpty(symbol)) {
            lambdaQueryWrapper.eq(ExchangeApplyOrder::getSymbol, symbol);
        }
        if (StringUtils.isNotEmpty(orderPriceType)) {
            lambdaQueryWrapper.eq(ExchangeApplyOrder::getOrderPriceType, orderPriceType);
        }
        if (null != isAll) {
            List<String> items = itemService.getAllSymbol();
            lambdaQueryWrapper.in(ExchangeApplyOrder::getSymbol, items);
        }
        if ("orders".equals(type)) {
//            lambdaQueryWrapper.notIn(ExchangeApplyOrder::getOrderPriceType, "opponent");
            lambdaQueryWrapper.eq(ExchangeApplyOrder::getState, ExchangeApplyOrder.STATE_SUBMITTED);
        } else if ("hisorders".equals(type)) {
//            lambdaQueryWrapper.notIn(ExchangeApplyOrder::getOrderPriceType, "opponent");
            lambdaQueryWrapper.in(ExchangeApplyOrder::getState, ExchangeApplyOrder.STATE_CREATED, ExchangeApplyOrder.STATE_CANCELED);
        } else if ("opponent".equals(type)) {
//            lambdaQueryWrapper.notIn(ExchangeApplyOrder::getOrderPriceType, "opponent");
            lambdaQueryWrapper.eq(ExchangeApplyOrder::getState, ExchangeApplyOrder.STATE_CREATED);
        } else if ("canceled".equals(type)) {
//            lambdaQueryWrapper.notIn(ExchangeApplyOrder::getOrderPriceType, "opponent");
            lambdaQueryWrapper.eq(ExchangeApplyOrder::getState, ExchangeApplyOrder.STATE_CANCELED);
        }
        if (!StringUtils.isNullOrEmpty(startTime)) {
            lambdaQueryWrapper.ge(ExchangeApplyOrder::getCreateTime, DateUtil.minDate(DateUtil.stringToDate(startTime, "yyyy-MM-dd")));
//			parameters.put("startTime",DateUtils.toDate(startTime));
        }
        if (!StringUtils.isNullOrEmpty(endTime)) {
            lambdaQueryWrapper.le(ExchangeApplyOrder::getCreateTime, DateUtil.maxDate(DateUtil.stringToDate(endTime, "yyyy-MM-dd")));
        }
        List<String> symbols = itemService.findByType(symbolType).stream().map(Item::getSymbol).collect(Collectors.toList());
        symbols.add("-1");
        if (StringUtils.isNotEmpty(symbolType)) {
            lambdaQueryWrapper.in(ExchangeApplyOrder::getSymbol, symbols);
        }
        lambdaQueryWrapper.orderByDesc(ExchangeApplyOrder::getCreateTime);
        Page page = new Page(pageNo, size);
        page(page, lambdaQueryWrapper);
        List<Map<String, Object>> data;
        if (!StringUtils.isNullOrEmpty(symbol) || null != isAll) {
//        if (StringUtils.isNullOrEmpty(type)) {
            // 股票交易的记录
            data = this.entrustBulidData(page.getRecords());
        } else {
            // 兑换记录
            data = this.bulidData(page.getRecords());
        }
        return data;
    }

    private List<Map<String, Object>> bulidData(List<ExchangeApplyOrder> list) {

        Map<String, ExchangeRecord> recordMap = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            ExchangeApplyOrder order = list.get(i);
            ExchangeRecord record = new ExchangeRecord();
            if (recordMap.containsKey(order.getOrderNo())) {
                record = recordMap.get(order.getOrderNo());
            }
            record.setClosePrice(order.getClosePrice());
            record.setState(order.getState());
            record.setCreate_time(DateUtils.format(order.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
            // 开仓 买入**币
            if (ExchangeApplyOrder.OFFSET_CLOSE.equals(order.getOffset())) {
                if (recordMap.containsKey(order.getOrderNo())) {
                    record.setSymbol(order.getSymbol());
                    record.setAmount(order.getVolume());
                } else {
                    record.setSymbol(order.getSymbol());
                    record.setAmount(order.getVolume());
                    // 针对 **币 --兑换-- usdt
                    record.setAmount_to(order.getSymbolValue());
                }
            }
            // 平仓 卖出**币
            else if (ExchangeApplyOrder.OFFSET_OPEN.equals(order.getOffset())) {
                // new BigDecimal(String.valueOf(value)).toPlainString()
                if (recordMap.containsKey(order.getOrderNo())) {
                    record.setSymbol_to(order.getSymbol());
                    record.setAmount_to(order.getSymbolValue());
                } else {
                    record.setSymbol_to(order.getSymbol());
                    record.setAmount(order.getVolume());
                    record.setAmount_to(order.getSymbolValue());
                }
            }
            record.setPid(order.getPid());
            recordMap.put(order.getOrderNo(), record);
        }
        for (ExchangeRecord entry : recordMap.values()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("symbol", entry.getSymbol());
            Item bySymbol = itemService.findBySymbol(entry.getSymbol());
            if (bySymbol != null) {
                map.put("symbol_data", bySymbol.getSymbolData());
            }
            map.put("symbol_to", entry.getSymbol_to());
            map.put("amount", entry.getAmount());
            map.put("amount_to", entry.getAmount_to());
            map.put("create_time", entry.getCreate_time());
            map.put("state", entry.getState());
            map.put("closePrice", entry.getClosePrice());
            map.put("pid", entry.getPid());
            data.add(map);
        }
        if (data.size() > 0) {
            Collections.sort(data, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    String time1 = String.valueOf(o1.get("create_time"));
                    String time2 = String.valueOf(o2.get("create_time"));
                    return time2.compareTo(time1);
                }
            });
            return data;
        }
        return data;
    }

    private List<Map<String, Object>> entrustBulidData(List<ExchangeApplyOrder> list) {
        List<Map<String, Object>> data = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            ExchangeApplyOrder order = list.get(i);
            Map<String, Object> map = new HashMap<String, Object>();
            String symbol = itemService.findBySymbol(order.getSymbol()).getName();
            map.put("profitLoss", 0);
            List<Realtime> symbolList = dataService.realtime(symbol);
            if (!CollectionUtil.isEmpty(symbolList)) {
                Realtime realtime = symbolList.get(0);
                double oldPrie = order.getClosePrice().doubleValue() * order.getVolume().doubleValue();
                double newPrie = realtime.getClose().doubleValue() * order.getVolume().doubleValue();
                map.put("profitLoss", oldPrie - newPrie); //盈亏
            }
            map.put("order_no", order.getOrderNo());
            map.put("name", symbol);
            map.put("symbol", order.getSymbol());
            Item bySymbol = itemService.findBySymbol(symbol);
            if (bySymbol != null) {
                map.put("symbol_data", bySymbol.getSymbolData());
            }
            map.put("create_time", DateUtil.formatDate(order.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
            map.put("volume", new BigDecimal(order.getVolume()).setScale(0, RoundingMode.HALF_UP));
            // 买的时候total 为volume，卖的时候total = volume*closeTime
            if ("open".equalsIgnoreCase(order.getOffset())) {
                map.put("total", new BigDecimal(order.getVolume()).setScale(2, RoundingMode.HALF_UP));
            } else {
                map.put("total", new BigDecimal(order.getVolume() * order.getClosePrice()).setScale(2, RoundingMode.HALF_UP));
            }
            map.put("offset", order.getOffset());
            map.put("price", order.getPrice());
            map.put("order_price_type", order.getOrderPriceType());
            map.put("state", order.getState());
            map.put("closePrice", order.getClosePrice());
            map.put("fee", order.getFee());
            /*map.put("create_time_ts", order.getCreateTimeTs());
            map.put("update_time_ts", order.getUpdateTimeTs());*/
            Date date = new Date(order.getCreateTimeTs() * 1000L);
            // 创建 SimpleDateFormat 对象并设置时区
            String format = DateUtils.format(date, DateUtils.DF_yyyyMMddHHmmss);
            Date dates = DateUtils.toDates(format, DateUtils.NORMAL_DATE_FORMAT);
            long timestamp = dates.getTime();
            String substring = String.valueOf(timestamp).substring(0, 10);
            map.put("create_time_ts", Long.valueOf(substring));

            Date dateUp = new Date(order.getUpdateTimeTs() * 1000L);
            // 创建 SimpleDateFormat 对象并设置时区
            String formatUp = DateUtils.format(dateUp, DateUtils.DF_yyyyMMddHHmmss);
            Date datesUp = DateUtils.toDates(formatUp, DateUtils.NORMAL_DATE_FORMAT);
            long timestampUp = datesUp.getTime();
            String substringUp = String.valueOf(timestampUp).substring(0, 10);
            map.put("update_time_ts", substringUp);

            map.put("trigger_price", order.getTriggerPrice());
            map.put("is_trigger_order", order.isTriggerOrder());
            map.put("pid", order.getPid());
            data.add(map);
        }
        return data;
    }

    /**
     * 开仓委托
     */
    public void openYd(ExchangeApplyOrder order, String pid) {
        order.setOrderNo(DateUtil.getToday("yyMMddHHmmss") + RandomUtil.getRandomNum(8));
        order.setFee(Arith.mul(order.getVolume(), sysparaService.find("exchange_apply_order_buy_fee").getDouble()));
        order.setCreateTime(new Date());
        // 买入数量 - 手续费 = 到账
        double sub = Arith.sub(order.getVolume(), order.getFee());
        // 可以买的数量
        double amount = Arith.div(sub, order.getClosePrice(), 8);
        order.setSymbolValue(amount);
        order.setPid(pid);
        Wallet wallet = this.walletService.saveWalletByPartyId(order.getPartyId());
        double amount_before = wallet.getMoney().doubleValue();
        // 如果是计划委托，则先不扣钱
        if (order.isTriggerOrder()) {
            /*
             * 保存资金日志
             */
            MoneyLog moneylog_deposit = new MoneyLog();
            moneylog_deposit.setCategory(Constants.MONEYLOG_CATEGORY_EXCHANGE);
            moneylog_deposit.setAmountBefore(new BigDecimal(amount_before));
            moneylog_deposit.setAmount(new BigDecimal(0));
            moneylog_deposit.setAmountAfter(new BigDecimal(amount_before));
            moneylog_deposit.setLog("股票交易计划委托买入订单，订单号[" + order.getOrderNo() + "]");
            moneylog_deposit.setUserId(order.getPartyId());
            moneylog_deposit.setWalletType(Constants.WALLET);
            moneylog_deposit.setSymbol(order.getSymbol());
            moneylog_deposit.setContentType(Constants.MONEYLOG_CONTENT_EXCHANGE_OPEN);
            moneyLogService.save(moneylog_deposit);
            save(order);
        }
        if (!order.isTriggerOrder()) {
            if (wallet.getMoney().doubleValue() < order.getVolume().doubleValue()) {
                throw new YamiShopBindException("余额不足");
            }
            this.walletService.update(wallet.getUserId().toString(), Arith.sub(0, order.getVolume()));
            /*
             * 保存资金日志
             */
            MoneyLog moneylog_deposit = new MoneyLog();
            moneylog_deposit.setCategory(Constants.MONEYLOG_CATEGORY_EXCHANGE);
            moneylog_deposit.setAmountBefore(new BigDecimal(amount_before));
            moneylog_deposit.setAmount(new BigDecimal(Arith.sub(0, order.getVolume().doubleValue())).setScale(2, RoundingMode.HALF_UP));
            // moneylog_deposit.setAmount(new BigDecimal(order.getVolume().doubleValue()));
            moneylog_deposit.setAmountAfter(new BigDecimal(Arith.sub(wallet.getMoney().doubleValue(), order.getVolume())));
            moneylog_deposit.setLog("股票买入交易，购买订单号[" + order.getOrderNo() + "]");
            moneylog_deposit.setTitle("stock buy transaction");//股票买入交易
            moneylog_deposit.setConf("buy");//买入
            moneylog_deposit.setUserId(order.getPartyId());
            moneylog_deposit.setWalletType(Constants.WALLET);
            moneylog_deposit.setSymbol(order.getSymbol());
            moneylog_deposit.setContentType(Constants.MONEYLOG_CONTENT_EXCHANGE_OPEN);
            moneyLogService.save(moneylog_deposit);
            save(order);
            userDataService.saveBuyYd(order);
        }
    }

    /**
     * 开仓委托
     */
    public void open(ExchangeApplyOrder order) {
        order.setOrderNo(DateUtil.getToday("yyMMddHHmmss") + RandomUtil.getRandomNum(8));
        order.setFee(Arith.mul(order.getVolume(), sysparaService.find("exchange_apply_order_buy_fee").getDouble()));
        order.setCreateTime(new Date());
        // 买入数量 - 手续费 = 到账
        double sub = Arith.sub(order.getVolume(), order.getFee());
        // 可以买的数量
        double amount = Arith.div(sub, order.getClosePrice(), 8);
        order.setSymbolValue(amount);
        Wallet wallet = this.walletService.saveWalletByPartyId(order.getPartyId());
        double amount_before = wallet.getMoney().doubleValue();
        // 如果是计划委托，则先不扣钱
        if (order.isTriggerOrder()) {
            /*
             * 保存资金日志
             */
            MoneyLog moneylog_deposit = new MoneyLog();
            moneylog_deposit.setCategory(Constants.MONEYLOG_CATEGORY_EXCHANGE);
            moneylog_deposit.setAmountBefore(new BigDecimal(amount_before));
            moneylog_deposit.setAmount(new BigDecimal(0));
            moneylog_deposit.setAmountAfter(new BigDecimal(amount_before));
            moneylog_deposit.setLog("股票交易计划委托订单，订单号[" + order.getOrderNo() + "]");
            moneylog_deposit.setUserId(order.getPartyId());
            moneylog_deposit.setWalletType(Constants.WALLET);
            moneylog_deposit.setSymbol(order.getSymbol());
            moneylog_deposit.setContentType(Constants.MONEYLOG_CONTENT_EXCHANGE_OPEN);
            moneyLogService.save(moneylog_deposit);
            save(order);
        }
        if (!order.isTriggerOrder()) {
            if (wallet.getMoney().doubleValue() < order.getVolume().doubleValue()) {
                throw new YamiShopBindException("余额不足");
            }
            this.walletService.update(wallet.getUserId().toString(), Arith.sub(0, order.getVolume()));
            /*
             * 保存资金日志
             */
            MoneyLog moneylog_deposit = new MoneyLog();
            moneylog_deposit.setCategory(Constants.MONEYLOG_CATEGORY_EXCHANGE);
            moneylog_deposit.setAmountBefore(new BigDecimal(amount_before));
            moneylog_deposit.setAmount(new BigDecimal(Arith.sub(0, order.getVolume().doubleValue())));
            moneylog_deposit.setAmountAfter(new BigDecimal(Arith.sub(wallet.getMoney().doubleValue(), order.getVolume())));
            moneylog_deposit.setLog("股票交易，订单号[" + order.getOrderNo() + "]");
            moneylog_deposit.setTitle("stock shell transaction");//股票交易卖出数量
            moneylog_deposit.setConf("shell");//卖出数量
            moneylog_deposit.setUserId(order.getPartyId());
            moneylog_deposit.setWalletType(Constants.WALLET);
            moneylog_deposit.setSymbol(order.getSymbol());
            moneylog_deposit.setContentType(Constants.MONEYLOG_CONTENT_EXCHANGE_OPEN);
            moneyLogService.save(moneylog_deposit);
            save(order);
            userDataService.saveBuy(order);
        }
    }

    /**
     * 卖股票
     */
    public void closeYd(ExchangeApplyOrder order, String pid) {
        order.setOrderNo(DateUtil.getToday("yyMMddHHmmss") + RandomUtil.getRandomNum(8));
        order.setCreateTime(new Date());
        order.setFee(Arith.mul(order.getVolume() * order.getPrice(), sysparaService.find("exchange_apply_order_sell_fee").getDouble()));
        //order.setFee(Arith.mul(order.getVolume(), sysparaService.find("exchange_apply_order_sell_fee").getDouble()));
        double sub = Arith.sub(order.getVolume(), order.getFee());
        double amount = Arith.mul(sub, order.getClosePrice());
        order.setSymbolValue(amount);
        order.setPid(pid);
        // .close
        WalletExtend walletExtend = walletService.saveExtendByPara(order.getPartyId(), order.getSymbol());
        double amount_before = walletExtend.getAmount();
        // 如果是计划委托，则先不扣钱
        if (order.isTriggerOrder()) {
            /*
             * 保存资金日志
             */
            MoneyLog moneylog = new MoneyLog();
            moneylog.setCategory(Constants.MONEYLOG_CATEGORY_EXCHANGE);
            moneylog.setAmountBefore(new BigDecimal(amount_before));
            moneylog.setAmount(new BigDecimal(0));
            moneylog.setAmountAfter(new BigDecimal(amount_before));
            moneylog.setLog("股票交易计划委托卖出订单，订单号[" + order.getOrderNo() + "]");
            moneylog.setTitle("stock shell transaction");//股票卖出交易
            moneylog.setConf("shell");//卖出
            moneylog.setUserId(order.getPartyId());
            moneylog.setWalletType(order.getSymbol());
            moneylog.setSymbol(order.getSymbol());
            moneylog.setContentType(Constants.MONEYLOG_CONTENT_EXCHANGE_CLOSE);
            moneyLogService.save(moneylog);
            save(order);
        }
        if (!order.isTriggerOrder()) {
            if (order.getVolume() > walletExtend.getAmount()) {
                throw new YamiShopBindException("仓位不足");
            }
            walletService.updateExtend(walletExtend.getPartyId().toString(), walletExtend.getWallettype(),
                    Arith.sub(0, order.getVolume()));
            /*
             * 保存资金日志
             */
           /* MoneyLog moneylog = new MoneyLog();
            moneylog.setCategory(Constants.MONEYLOG_CATEGORY_EXCHANGE);
            moneylog.setAmountBefore(new BigDecimal(amount_before));
            moneylog.setAmount(new BigDecimal(Arith.sub(0, order.getVolume())));
            moneylog.setAmountAfter(new BigDecimal(Arith.sub(walletExtend.getAmount(), order.getVolume())));
            moneylog.setLog("股票交易，订单号[" + order.getOrderNo() + "]");
            moneylog.setTitle("stock shell transaction");//股票卖出交易
            moneylog.setConf("shell");//卖出
            moneylog.setUserId(order.getPartyId());
            moneylog.setWalletType(order.getSymbol());
            moneylog.setSymbol(order.getSymbol());
            moneylog.setContentType(Constants.MONEYLOG_CONTENT_EXCHANGE_CLOSE);
            moneyLogService.save(moneylog);*/
            save(order);
            userDataService.saveSellYd(order, pid);
        }
    }

    /**
     * 卖股票
     */
    public void close(ExchangeApplyOrder order) {

        order.setOrderNo(DateUtil.getToday("yyMMddHHmmss") + RandomUtil.getRandomNum(8));
        order.setCreateTime(new Date());
        order.setFee(Arith.mul(order.getVolume(), sysparaService.find("exchange_apply_order_sell_fee").getDouble()));
        double sub = Arith.sub(order.getVolume(), order.getFee());
        double amount = Arith.mul(sub, order.getClosePrice());
        order.setSymbolValue(amount);
        // .close
        WalletExtend walletExtend = walletService.saveExtendByPara(order.getPartyId(), order.getSymbol());
        double amount_before = walletExtend.getAmount();
        // 如果是计划委托，则先不扣钱
        if (order.isTriggerOrder()) {
            /*
             * 保存资金日志
             */
            MoneyLog moneylog = new MoneyLog();
            moneylog.setCategory(Constants.MONEYLOG_CATEGORY_EXCHANGE);
            moneylog.setAmountBefore(new BigDecimal(amount_before));
            moneylog.setAmount(new BigDecimal(0));
            moneylog.setAmountAfter(new BigDecimal(amount_before));
            moneylog.setLog("股票交易计划委托订单，订单号[" + order.getOrderNo() + "]");
            moneylog.setTitle("股票交易卖出4");
            moneylog.setConf("卖出4");
            moneylog.setUserId(order.getPartyId());
            moneylog.setWalletType(order.getSymbol());
            moneylog.setSymbol(order.getSymbol());
            moneylog.setContentType(Constants.MONEYLOG_CONTENT_EXCHANGE_CLOSE);
            moneyLogService.save(moneylog);
            save(order);
        }
        if (!order.isTriggerOrder()) {
            if (order.getVolume() > walletExtend.getAmount()) {
                throw new YamiShopBindException("仓位不足");
            }
            walletService.updateExtend(walletExtend.getPartyId().toString(), walletExtend.getWallettype(),
                    Arith.sub(0, order.getVolume()));
            /*
             * 保存资金日志
             */
            MoneyLog moneylog = new MoneyLog();
            moneylog.setCategory(Constants.MONEYLOG_CATEGORY_EXCHANGE);
            moneylog.setAmountBefore(new BigDecimal(amount_before));
            moneylog.setAmount(new BigDecimal(Arith.sub(0, order.getVolume())));
            moneylog.setAmountAfter(new BigDecimal(Arith.sub(walletExtend.getAmount(), order.getVolume())));
            moneylog.setLog("股票交易，订单号[" + order.getOrderNo() + "]");
            moneylog.setTitle("股票交易卖出9");
            moneylog.setConf("卖出9");
            moneylog.setUserId(order.getPartyId());
            moneylog.setWalletType(order.getSymbol());
            moneylog.setSymbol(order.getSymbol());
            moneylog.setContentType(Constants.MONEYLOG_CONTENT_EXCHANGE_CLOSE);
            moneyLogService.save(moneylog);
            save(order);
            userDataService.saveSell(order);
            // userDataService.saveSellYd(order);
        }
    }

}
