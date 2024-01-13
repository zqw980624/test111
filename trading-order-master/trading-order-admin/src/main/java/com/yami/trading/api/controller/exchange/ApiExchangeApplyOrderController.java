package com.yami.trading.api.controller.exchange;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.exchange.ExchangeApplyOrder;
import com.yami.trading.bean.exchange.dto.ExchangeSymbolDto;
import com.yami.trading.bean.exchange.dto.SumEtfDto;
import com.yami.trading.bean.item.dto.RealtimeDTO;
import com.yami.trading.bean.model.*;
import com.yami.trading.bean.purchasing.dto.ExchangeLock;
import com.yami.trading.bean.syspara.domain.OpenClose;
import com.yami.trading.bean.syspara.domain.Syspara;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.*;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.RealNameAuthRecordService;
import com.yami.trading.service.SessionTokenService;
import com.yami.trading.service.WalletService;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.exchange.ExchangeApplyOrderService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.syspara.OpenCloseService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.user.UserService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 兑换
 * 股票交易 低买高卖
 */
@RestController
@CrossOrigin
@Slf4j
@Api(tags = " 股票交易 -api")
public class ApiExchangeApplyOrderController {
    private final String action = "/api/exchangeapplyorder!";
    @Autowired
    private SessionTokenService sessionTokenService;
    @Autowired
    UserService userService;
    @Autowired
    private SysparaService sysparaService;
    @Autowired
    private DataService dataService;
    @Autowired
    private ExchangeApplyOrderService exchangeApplyOrderService;
    @Autowired
    WalletService walletService;
    @Autowired
    RealNameAuthRecordService realNameAuthRecordService;
    @Autowired
    ItemService itemService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    OpenCloseService openCloseService;

    /**
     * 兑换币 如果是使用usdt兑换其他币种，则直接执行正常买币open流程 如果是其他币种--》usdt 则是直接卖币流程
     * 如果是其他币种到另一个币种，则需要先卖出，然后再买入
     * <p>
     * 兑换接口
     */
    @RequestMapping(action + "buy_and_sell.action")
    public Object buy_and_sell(HttpServletRequest request) {
        String volume_temp = request.getParameter("volume");
        if (StringUtils.isNullOrEmpty(volume_temp)
                || !StringUtils.isDouble(volume_temp) || Double.valueOf(volume_temp) <= 0) {
            throw new YamiShopBindException("请输入正确的货币数量");
        }
        double volume = Double.valueOf(volume_temp);
        String symbol = request.getParameter("symbol");
        String symbol_to = request.getParameter("symbol_to");
        if (symbol.equals(symbol_to)) {
            throw new YamiShopBindException("请选择正确的币种");
        }

        String session_token = request.getParameter("session_token");
        String partyId = SecurityUtils.getUser().getUserId();
        Object object = this.sessionTokenService.cacheGet(session_token);
        this.sessionTokenService.del(session_token);
        if ((object == null) || (!partyId.equals((String) object))) {
            log.info("sessionToken{}", object);
            System.out.println("sessionToken " + object);
            throw new YamiShopBindException("请稍后再试");
        }
        User party = userService.getById(partyId);
        if (!party.isEnabled()) {
            throw new YamiShopBindException("用户已禁用!");
        }

        symbol = symbol.toLowerCase();
        symbol_to = symbol_to.toLowerCase();
        String relation_order_no = UUID.randomUUID().toString();
        // 如果是使用usdt兑换其他币种，则直接执行正常买币open流程
        if ("usdt".equals(symbol) || "usdt".equals(symbol_to)) {
            ExchangeApplyOrder order = new ExchangeApplyOrder();
            order.setPartyId(partyId);
            order.setVolume(volume);
            order.setOrderPriceType("opponent");
            order.setRelationOrderNo(relation_order_no);
            if ("usdt".equals(symbol)) {
                order.setSymbol(symbol_to);
                double openPrice = getRealtimePrice(symbol_to);
                order.setPrice(openPrice);
                order.setOffset(ExchangeApplyOrder.OFFSET_OPEN);
            } else if ("usdt".equals(symbol_to)) {
                order.setSymbol(symbol);
                double closePrice = getRealtimePrice(symbol);
                order.setPrice(closePrice);
                order.setOffset(ExchangeApplyOrder.OFFSET_CLOSE);
            }
            exchangeApplyOrderService.saveCreate(order);
        } else {
            // 非usdt则需要进行一次卖出
            ExchangeApplyOrder order_sell = new ExchangeApplyOrder();
            order_sell.setPartyId(partyId);
            order_sell.setSymbol(symbol);
            order_sell.setOffset(ExchangeApplyOrder.OFFSET_CLOSE);
            order_sell.setVolume(volume);
            order_sell.setOrderPriceType("opponent");
            order_sell.setRelationOrderNo(relation_order_no);
            double sellClose = getRealtimePrice(symbol);
            order_sell.setPrice(sellClose);
            this.exchangeApplyOrderService.saveCreate(order_sell);
            double close = getRealtimePrice(symbol);
            double sub = Arith.sub(volume,
                    Arith.mul(volume, sysparaService.find("exchange_apply_order_sell_fee").getDouble()));
            double amount = Arith.mul(sub, close);
            // 再买入币种
            ExchangeApplyOrder order_buy = new ExchangeApplyOrder();
            order_buy.setPartyId(partyId);
            order_buy.setSymbol(symbol_to);
            order_buy.setOffset(ExchangeApplyOrder.OFFSET_OPEN);
            order_buy.setVolume(amount);
            order_buy.setRelationOrderNo(relation_order_no);
            order_buy.setOrderPriceType("opponent");
            double buyClose = getRealtimePrice(symbol_to);
            order_buy.setPrice(buyClose);
            exchangeApplyOrderService.saveCreate(order_buy);
        }
        return Result.succeed();
    }

    private double getRealtimePrice(String symbol) {
        List<Realtime> realtimes = dataService.realtime(symbol);
        double close = 1;
        if (realtimes != null && realtimes.size() > 0) {
            close = realtimes.get(0).getClose().doubleValue();
        } else {
            throw new YamiShopBindException("参数错误");
        }
        return close;
    }

    /**
     * 首次进入页面，传递session_token
     */
    @RequestMapping(action + "view.action")
    public Result view() {
        String partyId = SecurityUtils.getUser().getUserId();
        Map<String, Object> session = new HashMap<String, Object>();
        String session_token = sessionTokenService.savePut(partyId);
        session.put("session_token", session_token);
        return Result.succeed(session);
    }

    /**
     * 兑换汇率
     */
    @RequestMapping(action + "buy_and_sell_fee.action")
    public Object buy_and_sell_fee(HttpServletRequest request) {
        // 需兑换币种
        String symbol = request.getParameter("symbol");
        // 兑换后的币种
        String symbol_to = request.getParameter("symbol_to");
        if (symbol.equals(symbol_to)) {
            throw new YamiShopBindException("请选择正确的币种");
        }
        // 委托数量
        String volume_temp = request.getParameter("volume");
        if (StringUtils.isNullOrEmpty(volume_temp)
                || !StringUtils.isDouble(volume_temp) || Double.valueOf(volume_temp) < 0) {
            throw new YamiShopBindException("请输入正确的兑换数量");
        }
        Map<String, Object> data = new HashMap<String, Object>();
        symbol = symbol.toLowerCase();
        symbol_to = symbol_to.toLowerCase();
        double buy_fee = Double.valueOf(sysparaService.find("exchange_apply_order_buy_fee").getSvalue());
        double sell_fee = Double.valueOf(sysparaService.find("exchange_apply_order_sell_fee").getSvalue());
        double volume = Double.valueOf(volume_temp);
        if ("usdt".equals(symbol) || "usdt".equals(symbol_to)) {
            if ("usdt".equals(symbol)) {
                // 如果是使用Usdt 则计算收益
                List<Realtime> realtime_list = this.dataService.realtime(symbol_to);
                Realtime realtime = null;
                if (realtime_list.size() > 0) {
                    realtime = realtime_list.get(0);
                } else {
                    throw new YamiShopBindException("系统错误，请稍后重试");
                }
                double symbol_to_price = realtime.getClose().doubleValue();
                // usdt除以的数量
                data.put("get_rate", Arith.div(1, symbol_to_price, 5));
                // 实际兑换数量= 兑换数量-手续费数量
                double fact_volume = Arith.sub(volume, Arith.mul(volume, buy_fee));
                // 实际价值 = 实际兑换数量 * 被兑换品种价格
                double fact_price = Arith.mul(fact_volume, 1);
                // 实际获取数量 = 实际价值 / 将要兑换的品种的价值
                data.put("get_volume", Arith.div(fact_price, symbol_to_price, 5));
            }
            if ("usdt".equals(symbol_to)) {
                /**
                 * 如果是转成Usdt 则计算收益
                 */
                List<Realtime> realtime_list = this.dataService.realtime(symbol);
                Realtime realtime = null;
                if (realtime_list.size() > 0) {
                    realtime = realtime_list.get(0);
                } else {
                    throw new YamiShopBindException("系统错误，请稍后重试");
                }
                double symbol_price = realtime.getClose().doubleValue();
                // 对应usdt数量
                data.put("get_rate", Arith.div(symbol_price, 1, 5));
                // 实际兑换数量= 兑换数量-手续费数量
                double fact_volume = Arith.sub(volume, Arith.mul(volume, sell_fee));
                // 实际价值 = 实际兑换数量 * 被兑换品种价格
                double fact_price = Arith.mul(Arith.div(symbol_price, 1, 5), fact_volume);
                // 实际获取数量 = 实际价值 / 将要兑换的品种的价值
                data.put("get_volume", Arith.div(fact_price, 1, 5));
            }
        } else {
            double symbol_price = 0;
            double symbol_to_price = 0;
            // 获取币种最新价格
            List<Realtime> realtime_list = this.dataService.realtime(symbol);
            Realtime realtime = null;
            if (realtime_list.size() > 0) {
                realtime = realtime_list.get(0);
                symbol_price = realtime.getClose().doubleValue();
            } else {
                throw new YamiShopBindException("系统错误，请稍后重试");
            }
            // 获取币种最新价格
            List<Realtime> realtime_list_to = this.dataService.realtime(symbol_to);
            Realtime realtime_to = null;
            if (realtime_list_to.size() > 0) {
                realtime_to = realtime_list_to.get(0);
                symbol_to_price = realtime_to.getClose().doubleValue();
            } else {
                throw new YamiShopBindException("系统错误，请稍后重试");
            }
            if (symbol_to_price == 0) {
                symbol_to_price = 1;
            }
            if (symbol_price == 0) {
                symbol_price = 1;
            }
            data.put("get_rate", Arith.div(symbol_price, symbol_to_price, 5));
            // 总手续费比例
            double all_fee = Arith.add(buy_fee, sell_fee);
            // 实际兑换数量= 兑换数量-手续费数量
            double fact_volume = Arith.sub(volume, Arith.mul(volume, all_fee));
            // 实际价值 = 实际兑换数量 * 被兑换品种价格
            double fact_price = Arith.mul(fact_volume, symbol_price);
            // 实际获取数量 = 实际价值 / 将要兑换的品种的价值
            data.put("get_volume", Arith.div(fact_price, symbol_to_price, 5));
        }
        return Result.succeed(data);
    }

    /**
     * 兑换记录
     * 委托单记录
     */
    @RequestMapping(action + "list.action")
    public Result list(HttpServletRequest request) {
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        String symbol = request.getParameter("symbol");
        String type = request.getParameter("type");
        String isAll = request.getParameter("isAll");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String symbolType = "YD-stocks";
        String orderPriceType = request.getParameter("orderPriceType");
        String page_no = request.getParameter("page_no");
        int pageNo = Integer.valueOf(page_no);
        if ("orders".equals(type)) {
            // 页条数配成1000达到不分页的效果
            data = this.exchangeApplyOrderService.getPaged(pageNo, 1000, SecurityUtils.getCurrentUserId(), symbol, type, isAll, startTime, endTime, symbolType, orderPriceType);
        } else {
            data = this.exchangeApplyOrderService.getPaged(pageNo, 10, SecurityUtils.getCurrentUserId(), symbol, type, isAll, startTime, endTime, symbolType, orderPriceType);
        }
        return Result.succeed(data);
    }

    @RequestMapping(action + "listHis.action")
    public Result lists(HttpServletRequest request) {
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        data = this.exchangeApplyOrderService.getPageds(1, 20, SecurityUtils.getCurrentUserId(), "hisorders");
        if (data == null) {
            Result.succeed("历史股票无数据");
        }
        return Result.succeed(data);
    }

    /**
     * 获取etf 总资产
     *
     * @return
     */
    @RequestMapping(action + "getETFSum.action")
    public Result<SumEtfDto> getETFSum(String symbolType) {
        String userId = SecurityUtils.getCurrentUserId();
        SumEtfDto sumEtfDto = exchangeApplyOrderService.getProfitLossByUserId(userId, "YD-stocks");
        return Result.succeed(sumEtfDto);
    }

    /**
     * 获取etf 总资产
     *
     * @return
     */
    @RequestMapping(action + "getETFSumPwd.action")
    public Result<SumEtfDto> getETFSumPwd(String symbolType) {
        String userId = SecurityUtils.getCurrentUserId();
        //if (StringUtils.isEmptyString(symbolType)){
        //symbolType="YD-stocks";
        // }
        SumEtfDto sumEtfDto = exchangeApplyOrderService.getProfitLossByUserId(userId, symbolType);
        return Result.succeed(sumEtfDto);
    }

    /**
     * 获取etf 持仓比例
     *
     * @return
     */
    @RequestMapping(action + "getETFList.action")
    public Result<List<ExchangeSymbolDto>> getETFList(String symbolType) {
        String userId = SecurityUtils.getCurrentUserId();
        List<ExchangeSymbolDto> sumEtfDto = exchangeApplyOrderService.getETFListByUserId(userId, "YD-stocks");
        return Result.succeed(sumEtfDto);
    }

    /**
     * 股票交易-买入
     * 开仓页面参数
     */
    @RequestMapping(action + "openview.action")
    public Result openview() {
        Map<String, Object> data = new HashMap<String, Object>();
        String partyId = SecurityUtils.getUser().getUserId();
        Wallet wallet = walletService.saveWalletByPartyId(partyId);
        // 账户剩余资金
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.FLOOR);// 向下取整
        data.put("volume", df.format(wallet.getMoney()));
        String session_token = sessionTokenService.savePut(partyId);
        data.put("session_token", session_token);
        data.put("fee", sysparaService.find("exchange_apply_order_buy_fee").getSvalue());
        return Result.succeed(data);
    }

    /**
     * 股票交易-卖出
     * 平仓页面参数
     */
    @RequestMapping(action + "closeview.action")
    public Result closeview(HttpServletRequest request) {
        Map<String, Object> data = new HashMap<String, Object>();
        String partyId = SecurityUtils.getUser().getUserId();
        String symbol = request.getParameter("symbol");
        if (!StringUtils.isNullOrEmpty(partyId)) {
            WalletExtend walletExtend = walletService.saveExtendByPara(partyId, symbol);
            int amount = (int) walletExtend.getAmount();
            data.put("volume", null == walletExtend ? 0
                    : String.valueOf(amount));
            String session_token = sessionTokenService.savePut(partyId);
            data.put("session_token", session_token);
            data.put("fee", sysparaService.find("exchange_apply_order_sell_fee").getSvalue());
        }
        return Result.succeed(data);
    }

    /**
     * 股票交易-买入 印度
     */
    @RequestMapping(action + "open.action")
    public Object open(HttpServletRequest request) {
        // 委托数量乘以最新价格
        String volume = request.getParameter("volume");
        String session_token = request.getParameter("session_token");
        String symbol = request.getParameter("symbol");
        String pid = request.getParameter("pid");
        if (StringUtils.isEmpty(pid)) {
            throw new YamiShopBindException("pid不能为空");
        }
        // limit order的交易价格
        String price = request.getParameter("price");
        // 计划委托 是之前火币那边拷贝学过来的一个功能 只是只有一个盘在用，暂时注释不用
        // 是否计划委托
        String is_trigger_order = request.getParameter("is_trigger_order");
        // 计划委托的触发价
        String trigger_price = request.getParameter("trigger_price");
        // 订单报价类型。 "limit":限价 "opponent":对手价（市价）
        String order_price_type = request.getParameter("order_price_type");
        String partyId = SecurityUtils.getUser().getUserId();
        if (StringUtils.isNullOrEmpty(volume)
                || !StringUtils.isDouble(volume)
                || Double.valueOf(volume) <= 0) {
            throw new YamiShopBindException("请输入正确的货币数量");
        }
        OpenClose closes = openCloseService.getOne(Wrappers.<OpenClose>lambdaQuery().eq(OpenClose::getSymbol, "YD-stocks"));
        if (closes.getFlag().equals("0")) {
            String am_begin = closes.getStartDate();
            String am_end = closes.getEndDate();
            try {
                boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
                if (!am_flag) {
                    return Result.failed("Buying outside the trading period");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /*Object object = this.sessionTokenService.cacheGet(session_token);
        this.sessionTokenService.del(session_token);
        if ((object == null) || (!partyId.equals((String) object))) {
            throw new YamiShopBindException("请稍后再试");
        }*/
        User party = userService.getById(partyId);
        if (!party.isEnabled()) {
            throw new YamiShopBindException("用户已禁用");
        }
        String userName = party.getUserName();
        Syspara syspara = sysparaService.find("stop_user_internet");
        String stopUserInternet = syspara.getSvalue();
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(stopUserInternet)) {
            String[] stopUsers = stopUserInternet.split(",");
            System.out.println("userName = " + userName);
            System.out.println("stopUserInternet = " + stopUserInternet);
            if (Arrays.asList(stopUsers).contains(userName.trim())) {
                throw new YamiShopBindException("无网络");
            }
        }
        RealNameAuthRecord party_kyc = realNameAuthRecordService.getByUserId(partyId);
        if (party_kyc != null) {
            if (!(party_kyc.getStatus() == 2) && "true".equals(sysparaService.find("exchange_by_kyc").getSvalue())) {
                throw new YamiShopBindException("无权限");
            }
        }
        Object results = redisTemplate.opsForValue().get("ydTask" + pid);
        double close = 1;
        if (results != null) {
            JSONObject msgObject = JSONUtil.parseObj(results);
            close = Double.parseDouble(msgObject.getStr("last"));
        } else {
            throw new YamiShopBindException("参数错误");
        }
        ExchangeApplyOrder order = new ExchangeApplyOrder();
        order.setPartyId(partyId);
        order.setSymbol(symbol);
        order.setOffset(ExchangeApplyOrder.OFFSET_OPEN);
        order.setVolume(Double.valueOf(volume));
        order.setPrice(StringUtils.isNullOrEmpty(price) ? 0 : Double.valueOf(price));
        order.setTriggerOrder(StringUtils.isNullOrEmpty(is_trigger_order) ? false : Boolean.valueOf(is_trigger_order));
        order.setTriggerPrice(StringUtils.isNullOrEmpty(trigger_price) ? 0 : Double.valueOf(trigger_price));
        order.setOrderPriceType(order_price_type);
        order.setRelationOrderNo(UUID.randomUUID().toString());
        order.setClosePrice(close);
        // 限价单 && limit order的交易价格 为空
        if ("limit".equals(order.getOrderPriceType()) && order.getPrice() == null) {
            order.setPrice(close);
        }
        exchangeApplyOrderService.saveCreateyd(order, pid);
        return Result.succeed();
    }

    /**
     * 股票交易-卖出 印度
     */
    @RequestMapping(action + "close.action")
    public Object close(HttpServletRequest request) {
        // 委托数量乘以价格
        String volume = request.getParameter("volume");
        String session_token = request.getParameter("session_token");
        String symbol = request.getParameter("symbol");
        String pid = request.getParameter("pid");
        if (StringUtils.isEmpty(pid)) {
            throw new YamiShopBindException("pid不能为空");
        }
        // limit order的交易价格
        String price = request.getParameter("price");
        // 计划委托 是之前火币那边拷贝学过来的一个功能 只是只有一个盘在用，暂时注释不用
        // 是否计划委托
        String is_trigger_order = request.getParameter("is_trigger_order");
        // 计划委托的触发价
        String trigger_price = request.getParameter("trigger_price");
        // 订单报价类型。 "limit":限价 "opponent":市价
        String order_price_type = request.getParameter("order_price_type");
        String partyId = SecurityUtils.getUser().getUserId();
        if (StringUtils.isNullOrEmpty(volume)
                || !StringUtils.isDouble(volume)
                || Double.valueOf(volume) <= 0) {
            throw new YamiShopBindException("请输入正确的货币数量");
        }
        OpenClose closes = openCloseService.getOne(Wrappers.<OpenClose>lambdaQuery().eq(OpenClose::getSymbol, "YD-stocks"));
        if (closes.getFlag().equals("0")) {
            String am_begin = closes.getStartDate();
            String am_end = closes.getEndDate();
            try {
                boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
                if (!am_flag) {
                    return Result.failed("Buying outside the trading period");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Object object = this.sessionTokenService.cacheGet(session_token);
        this.sessionTokenService.del(session_token);
        if ((object == null) || (!partyId.equals((String) object))) {
            throw new YamiShopBindException("请稍后再试");
        }
        User party = userService.getById(partyId);
        if (!party.isEnabled()) {
            throw new YamiShopBindException("用户已禁用");
        }
        Syspara syspara = sysparaService.find("stop_user_internet");
        String stopUserInternet = syspara.getSvalue();
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(stopUserInternet)) {
            String[] stopUsers = stopUserInternet.split(",");
            System.out.println("userName = " + party.getUserName());
            System.out.println("stopUserInternet = " + stopUserInternet);
            if (Arrays.asList(stopUsers).contains(party.getUserName())) {
                throw new YamiShopBindException("无网络");
            }
        }
        Object results = redisTemplate.opsForValue().get("ydTask" + pid);
        double close = 1;
        if (results != null) {
            JSONObject msgObject = JSONUtil.parseObj(results);
            close = Double.parseDouble(msgObject.getStr("last"));
        } else {
            throw new YamiShopBindException("参数错误");
        }
        ExchangeApplyOrder order = new ExchangeApplyOrder();
        order.setPartyId(partyId);
        order.setSymbol(symbol);
        order.setOffset(ExchangeApplyOrder.OFFSET_CLOSE);
        order.setVolume(Double.valueOf(volume));
        order.setPrice(StringUtils.isNullOrEmpty(price) ? 0 : Double.valueOf(price));
        order.setTriggerOrder(StringUtils.isNullOrEmpty(is_trigger_order) ? false : Boolean.valueOf(is_trigger_order));
        order.setTriggerPrice(StringUtils.isNullOrEmpty(trigger_price) ? 0 : Double.valueOf(trigger_price));
        order.setOrderPriceType(order_price_type);
        order.setRelationOrderNo(UUID.randomUUID().toString());
        order.setClosePrice(close);
        // 限价单 && limit order的交易价格 为空
        if ("limit".equals(order.getOrderPriceType()) && order.getPrice() == null) {
            order.setPrice(close);
        }
        this.exchangeApplyOrderService.saveCreateyd(order, pid);
        return Result.succeed();
    }

    /**
     * 撤单
     */
    @RequestMapping(action + "cancel.action")
    public Object cancel(HttpServletRequest request) {
        String order_no = request.getParameter("order_no");
        CancelDelayThread lockDelayThread = new CancelDelayThread(SecurityUtils.getUser().getUserId(), order_no,
                exchangeApplyOrderService);
        Thread t = new Thread(lockDelayThread);
        t.start();
        return Result.succeed();
    }

    /**
     * 新线程处理，直接拿到订单锁处理完成后退出
     */
    public class CancelDelayThread implements Runnable {
        private String partyId;
        private String order_no;
        private ExchangeApplyOrderService exchangeApplyOrderService;

        public void run() {
            try {
                while (true) {
                    if (ExchangeLock.add(order_no)) {
                        this.exchangeApplyOrderService.saveCancel(partyId, order_no);
                        /**
                         * 处理完退出
                         */
                        break;
                    }
                    ThreadUtils.sleep(100);
                }
            } catch (Exception e) {
                log.error("error:", e);
            } finally {
                ThreadUtils.sleep(100);
                ExchangeLock.remove(order_no);
            }
        }

        public CancelDelayThread(String partyId, String order_no, ExchangeApplyOrderService exchangeApplyOrderService) {
            this.partyId = partyId;
            this.order_no = order_no;
            this.exchangeApplyOrderService = exchangeApplyOrderService;
        }
    }

    /**
     * 详情接口
     */
    @RequestMapping(action + "get.action")
    public Object get(HttpServletRequest request) {
        String order_no = request.getParameter("order_no");
        ExchangeApplyOrder order = this.exchangeApplyOrderService.findByOrderNoAndPartyId(order_no,
                SecurityUtils.getUser().getUserId());
        return Result.succeed(bulidData(order));
    }

    private Map<String, Object> bulidData(ExchangeApplyOrder order) {
        DecimalFormat df = new DecimalFormat("#.##");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("order_no", order.getOrderNo());
        map.put("name", itemService.findBySymbol(order.getSymbol()).getName());
        map.put("symbol", order.getSymbol());
        map.put("create_time", DateUtils.format(order.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));

        //map.put("create_time", entry.getCreate_time());
        map.put("volume", order.getVolume());
        map.put("offset", order.getOffset());
        map.put("price", order.getPrice());
        map.put("order_price_type", order.getOrderPriceType());
        map.put("state", order.getState());
        map.put("fee", order.getFee());
        map.put("amount", order.getAmount());
        map.put("close_price", order.getClosePrice());
        map.put("close_time", DateUtils.format(order.getCloseTime(), DateUtils.DF_yyyyMMddHHmmss));
        map.put("trigger_price", order.getTriggerPrice());
        map.put("is_trigger_order", order.isTriggerOrder());
        return map;
    }
}
