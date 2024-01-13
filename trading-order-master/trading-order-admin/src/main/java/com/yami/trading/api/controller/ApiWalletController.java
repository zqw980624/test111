package com.yami.trading.api.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yami.trading.api.dto.ChannelBlockchainDto;
import com.yami.trading.api.model.GetChannelBlockchainModel;
import com.yami.trading.api.model.WithdrawFeeModel;
import com.yami.trading.api.service.UserCacheService;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.model.ChannelBlockchain;
import com.yami.trading.bean.model.Wallet;
import com.yami.trading.bean.model.WalletExtend;
import com.yami.trading.bean.vo.WithdrawFeeVo;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.Arith;
import com.yami.trading.common.util.HttpContextUtils;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.ChannelBlockchainService;
import com.yami.trading.service.RechargeBlockchainOrderService;
import com.yami.trading.service.WalletService;
import com.yami.trading.service.WithdrawService;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.system.TipService;
import com.yami.trading.service.user.WalletLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/wallet")
@Api(tags = "钱包")
@Slf4j
public class ApiWalletController {
    @Autowired
    ChannelBlockchainService channelBlockchainService;
    @Autowired
    RechargeBlockchainOrderService rechargeBlockchainOrderService;
    @Autowired
    WalletService walletService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserCacheService userCacheService;
    @Autowired
    WithdrawService withdrawService;
    @Autowired
    SysparaService sysparaService;
    @Autowired
    DataService dataService;
    @Autowired
    TipService tipService;
    @Autowired
    ItemService itemService;
    @Autowired
    WalletLogService walletLogService;
    @GetMapping("/getUsdt")
    @ApiOperation(value = "获取usdt余额")
    public Result getUsdt() {
        // usdt余额
        Map<String, Object> data = new HashMap<String, Object>();
        String partyId = SecurityUtils.getUser().getUserId();
        DecimalFormat df2 = new DecimalFormat("#.##");
        // 向下取整
        df2.setRoundingMode(RoundingMode.FLOOR);
        Wallet wallet = new Wallet();
        if (!"".equals(partyId) && partyId != null) {
            wallet = walletService.saveWalletByPartyId(partyId);
        }
        double money = wallet.getMoney().doubleValue();
        // 账户剩余资金
        data.put("money", df2.format(money));
        return Result.succeed(data);
    }


    @PostMapping("/getChannelBlockchain")
    @ApiOperation(value = "获取充值渠道")
    public Result<List<ChannelBlockchainDto>> getChannelBlockchain(@Valid GetChannelBlockchainModel model) {
        List<ChannelBlockchain> list = channelBlockchainService.list(Wrappers.<ChannelBlockchain>query().lambda().eq(ChannelBlockchain::getCoin, model.getCoin()));
        List<ChannelBlockchainDto> resultList = new ArrayList<>();
        for (ChannelBlockchain c : list) {
            ChannelBlockchainDto channelBlockchainDto = new ChannelBlockchainDto();
            BeanUtils.copyProperties(c, channelBlockchainDto);
            resultList.add(channelBlockchainDto);
        }
        return Result.succeed(resultList);
    }

    /**
     * 获取币种钱包
     */
    @GetMapping("list.action")
    @ApiOperation("获取币种钱包")
    public Result list(HttpServletRequest request) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        Wallet usdt = null;
        String partyId = SecurityUtils.getUser().getUserId();
        if (StringUtils.isNotEmpty(partyId)) {
            usdt = this.walletService.saveWalletByPartyId(partyId);
        }
        DecimalFormat df2 = new DecimalFormat("#.########");
        // 向下取整
        df2.setRoundingMode(RoundingMode.FLOOR);
        if (null == usdt) {
            usdt = new Wallet();
            usdt.setMoney(new BigDecimal(0));
            map.put("USDT", usdt);
        } else {
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.FLOOR);
            usdt.setMoney(new BigDecimal(df.format(usdt.getMoney())));
            map.put("USDT", usdt);
        }
        // 其他币账户
        List<Item> list_it = this.itemService.cacheGetByMarket("");
        List<String> list_symbol = new ArrayList<String>();
        for (int i = 0; i < list_it.size(); i++) {
            Item items = list_it.get(i);
            list_symbol.add(items.getSymbol());
        }
        List<Item> items = this.itemService.cacheGetAll();
        Collections.sort(items, new Comparator<Item>() {
            // 按id排序
            @Override
            public int compare(Item arg0, Item arg1) {
                return arg0.getUuid().toString().compareTo(arg1.getUuid().toString());
            }
        });
        List<WalletExtend> walletExtends = null;
        if (StringUtils.isNotEmpty(partyId)) {
            walletExtends = this.walletService.findExtend(partyId, list_symbol);
        }
        WalletExtend walletExtend = new WalletExtend();
        // 如果是空
        if (null == walletExtends) {
            for (int i = 0; i < items.size(); i++) {
                walletExtend.setWallettype(items.get(i).getSymbol().toUpperCase());
                walletExtend.setAmount(0);
                map.put(walletExtend.getWallettype().toUpperCase(), walletExtend);
            }
        }
        // 如果不为空且2个相同
        if (walletExtends != null && walletExtends.size() == items.size()) {
            for (int i = 0; i < walletExtends.size(); i++) {
                if (null == walletExtends.get(i)) {
                    continue;
                }
                walletExtend = walletExtends.get(i);
                usdt.setMoney(new BigDecimal(df2.format(usdt.getMoney())));
                walletExtend.setAmount(Double.valueOf(df2.format(walletExtend.getAmount())));
                map.put(walletExtend.getWallettype().toUpperCase(), walletExtend);
            }
        }
        // 如果不为空 且数据库里的少于币种
        int temp = 0;
        if (walletExtends != null && walletExtends.size() < items.size()) {
            for (int i = 0; i < items.size(); i++) {
                for (int j = 0; j < walletExtends.size(); j++) {
                    walletExtend = walletExtends.get(j);
                    if (walletExtend.getWallettype().equals(items.get(i).getSymbol())) {
                        walletExtend.setAmount(Double.valueOf(df2.format(walletExtend.getAmount())));
                        map.put(walletExtend.getWallettype().toUpperCase(), walletExtend);
                        temp = 1;
                        break;
                    }
                }
                if (0 == temp) {
                    walletExtend = new WalletExtend();
                    walletExtend.setWallettype(items.get(i).getSymbol());
                    walletExtend.setAmount(0);
                    map.put(walletExtend.getWallettype().toUpperCase(), walletExtend);
                } else {
                    temp = 0;
                }
            }
        }
        return Result.succeed(map);
    }

    /**
     * 钱包账户资产（所有币种）
     */
    @GetMapping("getAll.action")
    public Object getAll(HttpServletRequest request) throws IOException {
        String symbolType = request.getParameter("symbolType");
        return this.getWalletExtends(request, true, symbolType);
    }

    /**
     * all：true/获取全部；false/获取usdt、btc、eth；
     */
    public Result getWalletExtends(HttpServletRequest request, boolean all, String symbolType) {
        String symbol = request.getParameter("symbol");
        Map<String, Object> mapRet = new LinkedHashMap<String, Object>();
        DecimalFormat df2 = new DecimalFormat("#.########");
        // 向下取整
        df2.setRoundingMode(RoundingMode.FLOOR);
        // String partyId ="dcc0dd35a49c383dadabc4dc030afe70";
        String partyId = SecurityUtils.getCurrentUserId();
        Wallet usdt = null;
        if (StringUtils.isNotEmpty(partyId)) {
            usdt = this.walletService.saveWalletByPartyId(partyId);
        }
        if (null == usdt) {
            usdt = new Wallet();
            usdt.setMoney(new BigDecimal(0));
            usdt.setLockMoney(new BigDecimal(0));
            usdt.setFreezeMoney(new BigDecimal(0));
            mapRet.put("usdt", usdt.getMoney());
            mapRet.put("lock_money", usdt.getLockMoney());
            mapRet.put("freeze_money", usdt.getFreezeMoney());
        } else {
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.FLOOR);
            usdt.setMoney(new BigDecimal(Double.valueOf(df.format(usdt.getMoney()))));
            usdt.setLockMoney(new BigDecimal(Double.valueOf(df.format(usdt.getLockMoney()))));
            usdt.setFreezeMoney(new BigDecimal(Double.valueOf(df.format(usdt.getFreezeMoney()))));
            mapRet.put("usdt", usdt.getMoney());
            mapRet.put("lock_money", usdt.getLockMoney());
            mapRet.put("freeze_money", usdt.getFreezeMoney());
        }
        // 其他币账户
        List<Item> list_it = this.itemService.cacheGetByMarket("");
        if (StringUtils.isNotEmpty(symbolType)) {
            list_it = list_it.stream().filter(i -> symbolType.equalsIgnoreCase(i.getType())).collect(Collectors.toList());
        }
        List<String> list_symbol = new ArrayList<String>();
        if (!StringUtils.isNotEmpty(symbol)) {
            // symbol为空，获取所有的
            for (int i = 0; i < list_it.size(); i++) {

                Item items = list_it.get(i);
                list_symbol.add(items.getSymbol());
            }
        } else {
            List<String> symbolList = Arrays.asList(symbol.split(","));
            for (int i = 0; i < list_it.size(); i++) {
                Item items = list_it.get(i);
                // 只添加所有币种和参数symbol都有的
                if (symbolList.contains(items.getSymbol())) {
                    list_symbol.add(items.getSymbol());
                }
            }
        }
        List<Item> items = this.itemService.cacheGetAll();
        // 按id排序
        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item arg0, Item arg1) {
                return arg0.getUuid().toString().compareTo(arg1.getUuid().toString());
            }
        });
        Map<String, Item> itemMap = new HashMap<String, Item>();
        for (int i = 0; i < items.size(); i++) {
            itemMap.put(items.get(i).getSymbol(), items.get(i));
        }
        List<WalletExtend> walletExtends = null;
        log.info(list_symbol.toString() + "=============");
        if (StringUtils.isNotEmpty(partyId)) {
            walletExtends = this.walletService.findExtend(partyId, list_symbol);
        }
        if (null == walletExtends) {
            walletExtends = new ArrayList<WalletExtend>();
        }
        List<WalletExtend> walletExtendsRet = new ArrayList<WalletExtend>();
        int temp = 0;
        for (int i = 0; i < list_symbol.size(); i++) {
            for (int j = 0; j < walletExtends.size(); j++) {
                WalletExtend walletExtend = walletExtends.get(j);
                if (walletExtend.getWallettype().equals(list_symbol.get(i))) {
                    walletExtend.setAmount(Double.valueOf(df2.format(walletExtend.getAmount())));
                    walletExtend.setLockAmount(Double.valueOf(df2.format(walletExtend.getLockAmount())));
                    walletExtend.setFreezeAmount(Double.valueOf(df2.format(walletExtend.getFreezeAmount())));
                    walletExtendsRet.add(walletExtend);
                    temp = 1;
                }
            }
            if (0 == temp) {
                WalletExtend walletExtend = new WalletExtend();
                if (StringUtils.isNotEmpty(partyId)) {
                    walletExtend.setPartyId(partyId);
                }
                walletExtend.setWallettype(list_symbol.get(i));
                walletExtend.setAmount(0);
                walletExtend.setLockAmount(0);
                walletExtend.setFreezeAmount(0);
                walletExtend.setName(itemMap.get(list_symbol.get(i)).getName());
                walletExtendsRet.add(walletExtend);
            }
            temp = 0;
        }
        String symbolsStr = "";
        for (int i = 0; i < list_symbol.size(); i++) {
            if (i != 0) {
                symbolsStr = symbolsStr + "," + list_symbol.get(i);
            } else {
                symbolsStr = list_symbol.get(i);
            }
        }
        List<Realtime> realtime_all = dataService.realtime(symbolsStr);
        if (realtime_all.size() <= 0) {
//				throw new BusinessException("系统错误，请稍后重试");
        }
        Map<String, Realtime> realtimeMap = new HashMap<String, Realtime>();
        for (int i = 0; i < realtime_all.size(); i++) {
            realtimeMap.put(realtime_all.get(i).getSymbol(), realtime_all.get(i));
        }
        List<Map<String, Object>> extendsList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < walletExtendsRet.size(); i++) {
            if (false == all) {
                // 只要btc、eth
                if (!walletExtendsRet.get(i).getWallettype().equals("btc") && !walletExtendsRet.get(i).getWallettype().equals("eth")) {
                    continue;
                }
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("name", walletExtendsRet.get(i).getName());
            String wallettype = walletExtendsRet.get(i).getWallettype();
            map.put("symbol", wallettype);
            map.put("symbol_data", itemMap.get(wallettype).getSymbolData());

            double volume = Arith.add(walletExtendsRet.get(i).getAmount(), walletExtendsRet.get(i).getFreezeAmount());
            map.put("volume", df2.format(volume));
            map.put("lock_amount", walletExtendsRet.get(i).getLockAmount());
            map.put("freeze_amount", walletExtendsRet.get(i).getFreezeAmount());
            map.put("usable", df2.format(walletExtendsRet.get(i).getAmount()));
            map.put("frozenAmount", walletExtendsRet.get(i).getFreezeAmount());
            Realtime rt = realtimeMap.get(walletExtendsRet.get(i).getWallettype());
            if (null != rt) {
                map.put("usdt", df2.format(Arith.mul(rt.getClose().doubleValue(), volume)));
            } else {
                map.put("usdt", 0);
            }
            extendsList.add(map);
        }
        if (!StringUtils.isNotEmpty(symbol) || symbol.contains("usdt")) {
            // 添加usdt到列表最前面
            Map<String, Object> mapUsdt = new HashMap<String, Object>();
            mapUsdt.put("name", "USDT/USDT");
            mapUsdt.put("symbol", "usdt");
            mapUsdt.put("symbol_data", "usdt");

            mapUsdt.put("volume", df2.format(Double.parseDouble(mapRet.get("usdt").toString())));
            if (mapRet.get("lock_amount") != null) {
                mapUsdt.put("lock_amount", df2.format(Double.parseDouble(mapRet.get("lock_amount").toString())));
            } else {
                mapUsdt.put("lock_amount", null);
            }
            if (mapRet.get("freeze_amount") != null) {
                mapUsdt.put("freeze_amount", df2.format(Double.parseDouble(mapRet.get("freeze_amount").toString())));
            } else {
                mapUsdt.put("freeze_amount", null);
            }
            mapUsdt.put("usdt", df2.format(Double.parseDouble(mapRet.get("usdt").toString())));
            mapUsdt.put("usable", df2.format(Double.parseDouble(mapRet.get("usdt").toString())));
            mapUsdt.put("frozenAmount", 0);
            extendsList.add(0, mapUsdt);
        }
        mapRet.put("extends", extendsList);
        return Result.succeed(mapRet);
    }

    /**
     * 钱包账户资产（币对）
     */
    @GetMapping("getPairs.action")
    public Result getPairs(@RequestParam String pairs, String symbolType) throws IOException {
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        if (StringUtils.isEmptyString(pairs) || !pairs.contains("/")) {
            throw new YamiShopBindException("参数错误");
        }
        List<String> symbolList = Arrays.asList(pairs.split("/"));
        if (null == symbolList || 2 != symbolList.size()) {
            throw new YamiShopBindException("参数错误");
        }
        String symbol1 = symbolList.get(0).toLowerCase();
        String symbol2 = symbolList.get(1).toLowerCase();
        // 获取所有币种的全称
        List<Item> list_it = this.itemService.cacheGetByMarket("");
        if (null == list_it || 0 == list_it.size()) {
            list_it = new ArrayList<Item>();
        }
        Map<String, String> itemMap = new HashMap<String, String>();
        Map<String, String> itemSymbolDataMap = new HashMap<String, String>();

        for (int i = 0; i < list_it.size(); i++) {
            itemMap.put(list_it.get(i).getSymbol(), list_it.get(i).getSymbolFullName());
            itemSymbolDataMap.put(list_it.get(i).getSymbol(), list_it.get(i).getSymbolData());
        }
        itemMap.put("usdt", "Usdt");
        // 并没有确定是否要过滤symbolType
        Result ret = getWalletExtends(request, true, symbolType);
        Map<String, Object> mapRetDefault = new LinkedHashMap<String, Object>();
        mapRetDefault.put("usdt", 0.0D);
        mapRetDefault.put("no_zero", new ArrayList<Map<String, Object>>());
        Map<String, Object> mapDefault1 = new HashMap<String, Object>();
        mapDefault1.put("symbol", symbol1);
        mapDefault1.put("full_name", StringUtils.isEmptyString(itemMap.get(symbol1)) ? symbol1.toUpperCase() : itemMap.get(symbol1));
        mapDefault1.put("volume", 0.0D);
        mapDefault1.put("usdt", 0.0D);
        Map<String, Object> mapDefault2 = new HashMap<String, Object>();
        mapDefault2.put("symbol", symbol2);
        mapDefault2.put("symbol_data", itemSymbolDataMap.get(symbol2));

        mapDefault1.put("full_name", StringUtils.isEmptyString(itemMap.get(symbol2)) ? symbol2.toUpperCase() : itemMap.get(symbol2));
        mapDefault2.put("volume", 0.0D);
        mapDefault2.put("usdt", 0.0D);
        List<Map<String, Object>> pairsDefaultList = new ArrayList<Map<String, Object>>();
        pairsDefaultList.add(mapDefault1);
        pairsDefaultList.add(mapDefault2);
        mapRetDefault.put("pairs", pairsDefaultList);
        Map<String, Object> mapRet = (Map<String, Object>) ret.getData();
        if (null == mapRet || 0 == mapRet.size()) {
            mapRet = mapRetDefault;
        } else {
            if (null == mapRet.get("usdt")) {
                mapRet.put("usdt", 0.0D);
            }
            Map<String, Object> map1 = new HashMap<String, Object>();
            Map<String, Object> map2 = new HashMap<String, Object>();
            List<Map<String, Object>> pairsList = new ArrayList<Map<String, Object>>();
            List<Map<String, Object>> noZeroList = new ArrayList<Map<String, Object>>();
            List<Map<String, Object>> extendsList = (List<Map<String, Object>>) mapRet.get("extends");
            if (null == extendsList || 0 == extendsList.size()) {
                mapRet.put("pairs", pairsDefaultList);
                mapRet.put("no_zero", new ArrayList<Map<String, Object>>());
            } else {
                for (int i = 0; i < extendsList.size(); i++) {
                    if (symbol1.equals(extendsList.get(i).get("symbol").toString().toLowerCase())) {
                        map1 = extendsList.get(i);
                        map1.remove("name");
                        if (Item.indices.equals(symbolType) || Item.US_STOCKS.equalsIgnoreCase(symbolType) || Item.forex.equalsIgnoreCase(symbolType)) {
                            if (symbol1.equals("usdt")) {
                                map1.put("full_name", "usd");
                                map1.put("symbol", "usd");
                                map1.put("symbol_data", "USD");

                            } else {
                                map1.put("full_name", "usd");
                                map1.put("symbol_data", itemSymbolDataMap.get(symbol1));
                            }

                        } else {
                            map1.put("full_name", StringUtils.isEmptyString(itemMap.get(symbol1)) ? symbol1.toUpperCase() : itemMap.get(symbol1));
                            map1.put("symbol_data", itemSymbolDataMap.get(symbol1));

                        }
                        continue;
                    }
                    if (symbol2.equals(extendsList.get(i).get("symbol").toString().toLowerCase())) {
                        map2 = extendsList.get(i);
                        map2.remove("name");
                        if (Item.indices.equals(symbolType) || Item.US_STOCKS.equalsIgnoreCase(symbolType) || Item.forex.equalsIgnoreCase(symbolType)) {
                            map2.put("full_name", "usd");
                            if (symbol2.equals("usdt")) {
                                map2.put("full_name", "usd");
                                map2.put("symbol", "usd");
                                map2.put("symbol_data", "USD");
                            } else {
                                map2.put("full_name", "usd");
                                map1.put("symbol_data", itemSymbolDataMap.get(symbol2));
                            }
                        } else {
                            map2.put("full_name", StringUtils.isEmptyString(itemMap.get(symbol2)) ? symbol2.toUpperCase() : itemMap.get(symbol2));
                            map1.put("symbol_data", itemSymbolDataMap.get(symbol2));

                        }
                        continue;
                    }
                    if (0.0D != Double.parseDouble(extendsList.get(i).get("volume").toString())) {
                        extendsList.get(i).remove("name");
                        String symbolI = (String) extendsList.get(i).get("symbol");
                        if (Item.indices.equals(symbolType) || Item.US_STOCKS.equalsIgnoreCase(symbolType) || Item.forex.equalsIgnoreCase(symbolType)) {
                            if (symbolI.equals("usdt")) {
                                extendsList.get(i).put("full_name", "usd");
                                extendsList.get(i).put("symbol", "usd");
                                extendsList.get(i).put("symbol_data", "USD");
                            } else {
                                extendsList.get(i).put("full_name", "usd");
                                extendsList.get(i).put("symbol_data", itemSymbolDataMap.get(symbolI));
                            }

                        } else {
                            extendsList.get(i).put("symbol_data", itemSymbolDataMap.get(symbolI));
                            extendsList.get(i).put("full_name", StringUtils.isEmptyString(itemMap.get(symbolI)) ? symbolI.toUpperCase() : itemMap.get(symbolI));
                        }
                        noZeroList.add(extendsList.get(i));
                    }
                }
                if (null == map1 || 0 == map1.size()) {
                    map1 = mapDefault1;
                }
                if (null == map2 || 0 == map2.size()) {
                    map2 = mapDefault2;
                }
                pairsList.add(map1);
                pairsList.add(map2);
                mapRet.put("pairs", pairsList);
                mapRet.put("no_zero", noZeroList);
            }
            mapRet.remove("extends");
        }

        List<Map<String, Object>> pairList = (List<Map<String, Object>>) mapRet.get("pairs");
        for (Map<String, Object> pair : pairList) {
            if (pair.get("symbol_data") == null && pair.get("symbol") != null) {
                String symbol = pair.get("symbol").toString();
                Item bySymbol = itemService.findBySymbol(symbol);
                if (bySymbol != null) {
                    pair.put("symbol_data", bySymbol.getSymbolData());
                }
            }

        }

        return Result.succeed(mapRet);
    }

    /**
     * 钱包历史记录
     */
    @RequestMapping("records.action")
    public Object records(HttpServletRequest request) throws IOException {
        // 页码
        String page_no = request.getParameter("page_no");
        // 充值category=recharge；提现category=withdraw；
        String category = request.getParameter("category");
        // 开始时间
        String start_time = request.getParameter("start_time");
        // 结束时间
        String end_time = request.getParameter("end_time");
        // 钱包类型 btc、eth...
        String wallet_type = request.getParameter("wallet_type");
        // 状态：0/初始状态，未知；1/成功；2/失败；
        String status = request.getParameter("status");
        Integer status_int = null;
        if (StringUtils.isNullOrEmpty(status)) {
            status_int = null;
        } else {
            if (!StringUtils.isInteger(status)) {
                throw new YamiShopBindException("状态不是整数");
            }
            if (Integer.valueOf(status).intValue() < 0) {
                throw new YamiShopBindException("状态不能小于0");
            }
            status_int = Integer.valueOf(status);
        }
        if (StringUtils.isNullOrEmpty(page_no)) {
            page_no = "1";
        }
        if (!StringUtils.isInteger(page_no)) {
            throw new YamiShopBindException("页码不是整数");
        }
        if (Integer.valueOf(page_no).intValue() <= 0) {
            throw new YamiShopBindException("页码不能小于等于0");
        }
        int page_no_int = Integer.valueOf(page_no).intValue();
        List<Map<String, Object>> data = this.walletLogService.pagedQueryRecords(page_no_int, 10, SecurityUtils.getUser().getUserId(), category, start_time, end_time, wallet_type, status_int).getRecords();
        for (Map<String, Object> log : data) {
            if (null == log.get("wallet_type") || !StringUtils.isNotEmpty(log.get("wallet_type").toString()))
                log.put("wallet_type", Constants.WALLET);
            else {
                log.put("wallet_type", log.get("wallet_type").toString().toUpperCase());
            }
        }
        return Result.succeed(data);
    }

    @PostMapping("/getFee")
    @ApiOperation(value = "获取提现手续费")
    public Result<WithdrawFeeVo> getFee(@Valid WithdrawFeeModel model) {
        return Result.succeed(withdrawService.getFee(model.getChannel(), model.getAmount()));
    }
}
