package com.yami.trading.admin.task.future;

import com.yami.trading.bean.future.domain.FuturesLock;
import com.yami.trading.bean.future.domain.FuturesOrder;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.util.MarketOpenChecker;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.service.future.FuturesOrderService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.syspara.SysparaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.List;

@Component
@Slf4j
public class FuturesOrderCalculationJob implements Runnable {
	@Autowired
	private FuturesOrderService futuresOrderService;
	@Autowired
	private FuturesOrderCalculationService futuresOrderCalculationService;
	@Autowired
	private SysparaService sysparaService;
	@Autowired
	private ItemService itemService;
	public void run() {
		try {
			while (true) {
				try {
					List<FuturesOrder> list = this.futuresOrderService.cacheSubmitted();
					
					//每单处理的时间间隔（毫秒）
					int futures_cal_time = sysparaService.find("futures_cal_time").getInteger();
					for (int i = 0; i < list.size(); i++) {
						FuturesOrder order = list.get(i);
						boolean lock = false;
						try {
							// 休市不做撮合
							Item bySymbol = itemService.findBySymbol(order.getSymbol());
							if(bySymbol == null){
								continue;
							}
							boolean isOpen = MarketOpenChecker.isMarketOpenByItemCloseType(bySymbol.getOpenCloseType());
							if(!isOpen){
								continue;
							}
							if (!FuturesLock.add(order.getOrderNo())) {
								continue;
							}
							lock = true;

							this.futuresOrderCalculationService.saveCalculation(order);

						} catch (Throwable e) {
							log.error("error:", e);
						} finally {
							if (lock) {
								if (futures_cal_time>0) {
									/**
									 * 每秒处理30个订单
									 */
									ThreadUtils.sleep(futures_cal_time);
								} else {
									/**
									 * 每秒处理200个订单
									 */
									ThreadUtils.sleep(5);
								}
								FuturesLock.remove(order.getOrderNo());
							}

						}

					}

				} catch (Throwable e) {
					log.error("run fail", e);
				} finally {
					/**
					 * 暂停0.1秒
					 */
					ThreadUtils.sleep(1000);
				}
			}
		} catch (Throwable e) {
			log.error("run fail", e);
		} finally{
			ThreadUtils.sleep(1000);
			/**
			 * 重新启动
			 */
			new Thread(this, "FuturesOrderCalculationJob").start();
			if (log.isInfoEnabled()) {
				log.info("交割合约持仓单盈亏计算线程启动！");
			}
		}

	}

	public void start() {

		new Thread(this, "FuturesOrderCalculationJob").start();
		if (log.isInfoEnabled()) {
			log.info("交割合约持仓单盈亏计算线程启动！");
		}

	}



}
