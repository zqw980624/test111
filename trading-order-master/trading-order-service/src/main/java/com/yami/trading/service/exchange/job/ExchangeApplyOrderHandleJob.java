package com.yami.trading.service.exchange.job;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yami.trading.bean.exchange.ExchangeApplyOrder;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.item.dto.RealtimeDTO;
import com.yami.trading.bean.purchasing.dto.ExchangeLock;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.MarketOpenChecker;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.exchange.ExchangeApplyOrderService;
import com.yami.trading.service.item.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 委托单进入市场
 */

@Slf4j
@Component
public class ExchangeApplyOrderHandleJob implements Runnable {
    @Autowired
    private ExchangeApplyOrderService exchangeApplyOrderService;
    @Autowired
    private DataService dataService;
    @Autowired
    private ItemService itemService;
    @Autowired
    RedisTemplate redisTemplate;

    public void run() {
        /*
         * 系统启动先暂停30秒
         */
        ThreadUtils.sleep(1000 * 30);
        while (true)
            try {
                List<ExchangeApplyOrder> list = this.exchangeApplyOrderService.findSubmitted();
                log.info("扫描委托单.......{}", list.size());
                for (int i = 0; i < list.size(); i++) {
                    ExchangeApplyOrder order = list.get(i);
                    Item bySymbol = itemService.findByPid(order.getPid());
                    if (bySymbol == null) {
                        log.info(order.getSymbol() + "=====");
                        throw new YamiShopBindException("当前币对不存在");
                    }
                    Object results = redisTemplate.opsForValue().get("ydTask" + order.getPid());//最新价
                    JSONObject msgObject = JSONUtil.parseObj(results);
                    double close = 1;
                    if (msgObject != null) {
                        close = Double.parseDouble(msgObject.getStr("last"));
                    } else {
                        continue;
                    }
                    //如果不是计划委托则按原代码执行
                    if (!order.isTriggerOrder()) {
                        if ("limit".equals(order.getOrderPriceType())) {
                            /**
                             * 限价单
                             */
                            if (ExchangeApplyOrder.OFFSET_OPEN.equals(order.getOffset())) {// 买入时 限制 低价买入
                                if (close <= order.getPrice().doubleValue()) {
                                    this.handle(order, msgObject);
                                }
                            } else {// 卖出时 限制 高价卖出
                                if (close >= order.getPrice()) {
                                    this.handle(order, msgObject);
                                }
                            }
                        } else {
                            /**
                             * 非限制，直接进入市 场
                             */
                            this.handle(order, msgObject);
                        }
                    }
                    //如果是计划委托单则
                    if (order.isTriggerOrder()) {
                        //需要满足触发价条件
                        if (ExchangeApplyOrder.OFFSET_OPEN.equals(order.getOffset())) {// 买入时 限制 低价买入
                            if (Double.parseDouble(msgObject.getStr("last")) <= order.getTriggerPrice()) {
                                this.handleTrigger(order, msgObject);
                            }
                        } else {// 卖出时 限制 高价卖出
                            if (Double.parseDouble(msgObject.getStr("last")) >= order.getTriggerPrice().doubleValue()) {
                                this.handleTrigger(order, msgObject);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("run fail", e);
            } finally {
                ThreadUtils.sleep(100 * 10);
            }
    }

    public void handle(ExchangeApplyOrder applyOrder, JSONObject msgObject) {
        boolean lock = false;
        try {
            if (!ExchangeLock.add(applyOrder.getOrderNo())) {
                return;
            }
            lock = true;
            if ("open".equals(applyOrder.getOffset())) {
                this.exchangeApplyOrderService.saveOpen(applyOrder, msgObject);
            } else if ("close".equals(applyOrder.getOffset())) {
                /**
                 * 平仓
                 */
                this.exchangeApplyOrderService.saveClose(applyOrder, msgObject);
            }
        } catch (Exception e) {
            log.error("error:", e);
        } finally {
            if (lock) {
                ThreadUtils.sleep(100 * 10);
                ExchangeLock.remove(applyOrder.getOrderNo());
            }
        }
    }
    public void handles(ExchangeApplyOrder applyOrder, RealtimeDTO realtime) {
        boolean lock = false;
        try {
            if (!ExchangeLock.add(applyOrder.getOrderNo())) {
                return;
            }
            lock = true;
            if ("open".equals(applyOrder.getOffset())) {
                this.exchangeApplyOrderService.saveOpens(applyOrder, realtime);
            } else if ("close".equals(applyOrder.getOffset())) {
                /**
                 * 平仓
                 */
                this.exchangeApplyOrderService.saveCloses(applyOrder, realtime);
            }
        } catch (Exception e) {
            log.error("error:", e);
        } finally {
            if (lock) {
                ThreadUtils.sleep(100 * 10);
                ExchangeLock.remove(applyOrder.getOrderNo());
            }
        }
    }

    public void handleTrigger(ExchangeApplyOrder applyOrder, JSONObject realtime) {
        boolean lock = false;
        try {
            if (!ExchangeLock.add(applyOrder.getOrderNo())) {
                return;
            }
            lock = true;
            if ("open".equals(applyOrder.getOffset())) {
                ExchangeApplyOrder order = new ExchangeApplyOrder();
                order.setPartyId(applyOrder.getPartyId());
                order.setSymbol(applyOrder.getSymbol());
                order.setOffset(applyOrder.getOffset());
                order.setVolume(applyOrder.getVolume());
                order.setPrice(applyOrder.getPrice());
                order.setTriggerOrder(false);
                order.setTriggerPrice(applyOrder.getTriggerPrice());
                order.setOrderPriceType(applyOrder.getOrderPriceType());
                this.exchangeApplyOrderService.saveCreate(order);
                applyOrder.setCloseTime(new Date());
                applyOrder.setState(ExchangeApplyOrder.STATE_CREATED);
                this.exchangeApplyOrderService.updateById(applyOrder);
            } else if ("close".equals(applyOrder.getOffset())) {
                ExchangeApplyOrder order = new ExchangeApplyOrder();
                order.setPartyId(applyOrder.getPartyId());
                order.setSymbol(applyOrder.getSymbol());
                order.setOffset(ExchangeApplyOrder.OFFSET_CLOSE);
                order.setVolume(applyOrder.getVolume());
                order.setPrice(applyOrder.getPrice());
                order.setTriggerOrder(false);
                order.setTriggerPrice(applyOrder.getTriggerPrice());
                order.setOrderPriceType(applyOrder.getOrderPriceType());
                this.exchangeApplyOrderService.saveCreate(order);
                /**
                 * 平仓
                 */
                applyOrder.setCloseTime(new Date());
                applyOrder.setState(ExchangeApplyOrder.STATE_CREATED);
                this.exchangeApplyOrderService.updateById(applyOrder);
            }
        } catch (Exception e) {
            log.error("error:", e);
        } finally {
            if (lock) {
                ThreadUtils.sleep(20 * 10);
                ExchangeLock.remove(applyOrder.getOrderNo());
            }
        }
    }

    public void start() {
        new Thread(this, "ExchangeApplyOrderHandleJob").start();
        log.info("币币委托单处理线程启动！");
    }
}

