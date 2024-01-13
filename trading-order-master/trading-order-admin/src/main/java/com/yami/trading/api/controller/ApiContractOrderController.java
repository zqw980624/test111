package com.yami.trading.api.controller;

import com.yami.trading.bean.contract.domain.ContractOrder;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.item.dto.RealtimeDTOS;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.lang.LangUtils;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.contract.ContractLockService;
import com.yami.trading.service.contract.ContractOrderService;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.item.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * 永续合约持仓单
 */
@RestController
@CrossOrigin
@Slf4j
public class ApiContractOrderController {

    @Autowired
    private ContractOrderService contractOrderService;
    @Autowired
    private DataService dataService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private ContractLockService contractLockService;
    @Autowired
    RedisTemplate redisTemplate;
    private final String action = "/api/contractOrder!";

    /**
     * 平仓
     * <p>
     * order_no 订单号
     */
    @RequestMapping(action + "close.action")
    public Result<String> close(@RequestParam String order_no) {
        try {
            CloseDelayThread lockDelayThread = new CloseDelayThread(SecurityUtils.getCurrentUserId(), order_no, this.contractOrderService, false);
            Thread t = new Thread(lockDelayThread);
            t.start();
        } catch (Exception e) {
            log.error("平仓失败", e);
            return Result.failed("平仓失败");
        }

        return Result.succeed("平仓成功");
    }

    /**
     * 一键平仓
     */
    @RequestMapping(action + "closeAll.action")
    public Result<String> closeAll() {


        try {

            CloseDelayThread lockDelayThread = new CloseDelayThread(SecurityUtils.getCurrentUserId(), "", this.contractOrderService, true);
            Thread t = new Thread(lockDelayThread);
            t.start();

        } catch (Exception e) {
            log.error("一键平仓失败", e);
            return Result.failed("一键平仓失败");
        }

        return Result.succeed("一键平仓成功");
    }

    /**
     * 订单列表
     * <p>
     * page_no 页码
     * symbol 币种
     * type 查询类型：orders 当前持仓单；hisorders 历史持仓单；
     */
    @RequestMapping(action + "list.action")
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false, defaultValue = "1") Integer page_no
            , @RequestParam(required = false) String type,
                                                  @RequestParam(required = false) String symbol, @RequestParam(required = false) String startTime,
                                                  @RequestParam(required = false) String endTime,   @RequestParam(required = false) String symbolType
    ) {

        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        String partyId = SecurityUtils.getCurrentUserId();

        if ("orders".equals(type)) {
//				// 页条数配成1000达到不分页的效果
            data = this.contractOrderService.findSubmittedRedis(partyId, symbol, startTime, endTime, symbolType);
        } else if ("hisorders".equals(type)) {
            data = this.contractOrderService.getPaged(page_no, 10, partyId, symbol, type, startTime, endTime, symbolType);
        }
        String symbolsStr = "";
        Set<String> symbols = new HashSet<String>();
        for (int i = 0; i < data.size(); i++) {
            String sym = data.get(i).get("symbol").toString();
            if (!symbols.contains(sym)) {
                symbols.add(sym);
                if (i != 0) {
                    symbolsStr = symbolsStr + "," + sym;
                } else {
                    symbolsStr = sym;
                }
            }
            if (ObjectUtils.isEmpty(data.get(i).get("profit"))) {
                data.get(i).put("profit", 0);
            }
        }
        List<Realtime> realtime_all = this.dataService.realtime(symbolsStr);
        if (realtime_all.size() <= 0) {
            realtime_all = new ArrayList<Realtime>();
        }

        Map<String, Realtime> realtimeMap = new HashMap<String, Realtime>();
        for (int i = 0; i < realtime_all.size(); i++) {
            realtimeMap.put(realtime_all.get(i).getSymbol(), realtime_all.get(i));
        }

        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> map = data.get(i);

            // 标记价格
            Realtime realtime = realtimeMap.get(map.get("symbol"));
            if (null == realtime) {
                map.put("mark_price", 0);
            } else {

                String thisSymbol = realtime.getSymbol();
                Item bySymbol = itemService.findBySymbol(thisSymbol);
                if(LangUtils.isEnItem()){
                    bySymbol.transName();
                    map.put("name", bySymbol.getName());
                }
                map.put("mark_price", realtime.getClose());
            }
            if (ObjectUtils.isEmpty(data.get(i).get("close_avg_price"))) {
                data.get(i).put("close_avg_price", data.get(i).get("mark_price"));
            }
        }
        return Result.ok(data);

    }

    /**
     * 订单详情
     * <p>
     * order_no 订单号
     */
    @RequestMapping(action + "get.action")
    public Result<Map<String, Object>> get(@RequestParam String order_no) {


        ContractOrder order = this.contractOrderService.findByOrderNo(order_no);

        if (null == order) {
            log.info("contractOrder!get order_no:" + order_no + ", order null");
            throw new YamiShopBindException("订单不存在");
        }

        return Result.ok(this.contractOrderService.bulidOne(order));


    }

    /**
     * 订单列表
     * <p>
     * page_no 页码
     * symbol 币种
     * type 查询类型：orders 当前持仓单；hisorders 历史持仓单；
     */
    @RequestMapping(action + "assets.action")
    public Result<Map<String, Object>> assets(HttpServletRequest request) throws IOException {
        String page_no = request.getParameter("page_no");
        String symbol = request.getParameter("symbol");
        String type = request.getParameter("type");
        String symbolType = request.getParameter("symbolType");
        if(StringUtils.isEmptyString(symbolType)){
            symbolType = Item.forex;
        }

        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String page_size = request.getParameter("page_size");

        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();


        if (StringUtils.isNullOrEmpty(page_no)) {
            page_no = "1";
        }

        if (StringUtils.isNullOrEmpty(page_size)) {
            page_size = "10";
        }

        if (!StringUtils.isInteger(page_no)) {
            throw new YamiShopBindException("页码不是整数");
        }
        if (Integer.valueOf(page_no).intValue() <= 0) {
            throw new YamiShopBindException("页码不能小于等于0");
        }
        String partyId = SecurityUtils.getCurrentUserId();

        int page_no_int = Integer.valueOf(page_no).intValue();
        int page_size_int = 10;
        Long count = 0L;
        if ("orders".equals(type)) {
            // 页条数配成1000达到不分页的效果
            data = this.contractOrderService.findSubmittedRedis(partyId, symbol, null, null, symbolType);
            count = (long) data.size();
        } else if ("hisorders".equals(type)) {
            count = this.contractOrderService.getOrdersCount("hisorders", partyId, symbol, symbolType);
            if (page_size.equals("all")) {
                page_size_int = Math.toIntExact(count);
            } else {
                page_size_int = Integer.valueOf(page_size).intValue();
            }
            data = this.contractOrderService.getPaged(page_no_int, page_size_int, partyId, symbol, type, startTime, endTime, symbolType);

        }

        String symbolsStr = "";
        Set<String> symbols = new HashSet<String>();
        for (int i = 0; i < data.size(); i++) {
            String sym = data.get(i).get("symbol").toString();
            if (!symbols.contains(sym)) {
                symbols.add(sym);
                if (i != 0) {
                    symbolsStr = symbolsStr + "," + sym;
                } else {
                    symbolsStr = sym;
                }
            }
            if (org.springframework.util.ObjectUtils.isEmpty(data.get(i).get("profit"))) {
                data.get(i).put("profit", 0);
            }
        }

        List<Realtime> realtime_all = this.dataService.realtime(symbolsStr);
        if (realtime_all.size() <= 0) {
            realtime_all = new ArrayList<Realtime>();
        }

        Map<String, Realtime> realtimeMap = new HashMap<String, Realtime>();
        for (int i = 0; i < realtime_all.size(); i++) {
            realtimeMap.put(realtime_all.get(i).getSymbol(), realtime_all.get(i));
        }

        double profitAll = 0;
        double closeAll = 0;
        double inventory_charge_all = 0;
        double fee_all = 0;
        double deposit_all = 0;
        double profitAndLoss = 0;
        double expectedProfitAndLoss = 0;

        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> map = data.get(i);

            // 标记价格
            Realtime realtime = realtimeMap.get(map.get("symbol"));
            if (null == realtime) {
                map.put("mark_price", 0);
            } else {
                map.put("mark_price", realtime.getClose());
            }
            double profit = Double.parseDouble(data.get(i).get("profit").toString());
            if (org.springframework.util.ObjectUtils.isEmpty(data.get(i).get("close_avg_price"))) {
                if (profit <= 0) {
                    data.get(i).put("close_avg_price", data.get(i).get("trade_avg_price"));
                }
            }

            //利润
            if (data.get(i).containsKey("profit_state")) {
                String profit_state = data.get(i).get("profit_state").toString();
                if (profit_state.equals("1")) {
                    profitAll += Math.abs(profit);
                } else if (profit_state.equals("0")) {
                    profitAll -= Math.abs(profit);
                }
            } else {
                profitAll += Math.abs(profit);
            }


            //入金
            double close_avg_price = Double.parseDouble(data.get(i).get("close_avg_price").toString());
            closeAll += close_avg_price;


            //库存费
            double mark_price = Double.parseDouble(data.get(i).get("mark_price").toString());
            double inventory_charge = mark_price - close_avg_price;
            inventory_charge_all += inventory_charge;

            //手续费
            double fee = Double.parseDouble(data.get(i).get("fee").toString());
            fee_all += fee;

            //保证金
            double deposit = Double.parseDouble(data.get(i).get("deposit").toString());
            deposit_all += deposit;
            //deposit

            //盈亏
            double profitAndLossOne = 0;
            double mark_price1 = Double.parseDouble(data.get(i).get("mark_price").toString());
            double trade_avg_price1 = Double.parseDouble(data.get(i).get("trade_avg_price").toString());
            double volume1 = Double.parseDouble(data.get(i).get("volume").toString());
            double price1 = 0;
            if (!org.springframework.util.ObjectUtils.isEmpty(data.get(i).get("price"))) {
                price1 = Double.parseDouble(data.get(i).get("price").toString());
            }
            //盈亏  （cur.mark_price - cur.trade_avg_price) * cur.volume
            profitAndLossOne += (mark_price1 - trade_avg_price1) * volume1;
            // 如果 volume1 为0 说明是当前平仓了，直接算利润
        //   if (Math.abs(volume1 - 0.0) < 1E-10) {
                profitAndLoss += profit;

        //    }

//            else {
//                profitAndLoss += profitAndLossOne;
//
//            }
//


            //预期盈亏
            double expectedProfitAndLossOne = 0;
            expectedProfitAndLossOne += (mark_price1 - price1) * volume1;

            expectedProfitAndLoss += expectedProfitAndLossOne;

        }


        Map<String, Object> dateN = new HashMap<>();
        dateN.put("profit_all", Double.valueOf(String.format("%.5f", profitAll)));                //利润
        dateN.put("cash_deposit", Double.valueOf(String.format("%.5f", closeAll)));                //入金
        dateN.put("inventory_charge_all", Double.valueOf(String.format("%.5f", inventory_charge_all)));        //库存费
        dateN.put("fee_all", fee_all);                    //手续费
        dateN.put("deposit_all", Double.valueOf(String.format("%.5f", deposit_all)));

        dateN.put("profitAndLoss", Double.valueOf(String.format("%.4f", profitAndLoss)));
        dateN.put("expectedProfitAndLoss", Double.valueOf(String.format("%.2f", expectedProfitAndLoss)));
        dateN.put("count", count);
        return Result.ok(dateN);


    }


    /**
     * 新线程处理，直接拿到订单锁处理完成后退出
     */
    public class CloseDelayThread implements Runnable {
        private String partyId;
        private String order_no;
        private ContractOrderService contractOrderService;
        private boolean all = false;

        public void run() {

            try {

                while (true) {
                    if (true == all) {
                        // 一键平仓
                        // if (ContractLock.add("all")) {
                        if (contractLockService.getContractLock("all")) {
                            this.contractOrderService.saveCloseRemoveAllByPartyId(partyId);
                            // 处理完退出
                            break;
                        }
                        ThreadUtils.sleep(500);
                    } else {
                        // if (ContractLock.add(order_no)) {
                        if (contractLockService.getContractLock(order_no)) {
                            this.contractOrderService.saveClose(partyId, order_no);
                            // 处理完退出
                            break;
                        }
                        ThreadUtils.sleep(500);
                    }
                }

            } catch (Throwable t) {
                log.error("error:", t);
            } finally {
                if (true == all) {
                    // ContractLock.remove("all");
                    contractLockService.removeContractLock("all");
                } else {
                    // ContractLock.remove(order_no);
                    contractLockService.removeContractLock(order_no);
                }
            }
        }

        public CloseDelayThread(String partyId, String order_no, ContractOrderService contractOrderService, boolean all) {
            this.partyId = partyId;
            this.order_no = order_no;
            this.contractOrderService = contractOrderService;
            this.all = all;
        }
    }

}
