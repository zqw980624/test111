package com.yami.trading.admin.task.contract;

import com.yami.trading.bean.contract.domain.ContractApplyOrder;
import com.yami.trading.bean.contract.domain.ContractOrder;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.MarketOpenChecker;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.service.contract.ContractApplyOrderService;
import com.yami.trading.service.contract.ContractLock;
import com.yami.trading.service.contract.ContractOrderService;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.item.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 
 * 委托单进入市场
 */
@Slf4j
@Component
public class ContractApplyOrderHandleJob implements Runnable {
	@Autowired
	private ContractOrderService contractOrderService;
	@Autowired
	private ContractApplyOrderService contractApplyOrderService;
	@Autowired
	@Qualifier("dataService")
	private DataService dataService;

	@Autowired
	private ItemService itemService;

	public void run() {
		/*
		 * 系统启动先暂停30秒
		 */
		ThreadUtils.sleep(1000 * 30);
		while (true)
			try {
				List<ContractApplyOrder> list = this.contractApplyOrderService.findSubmitted();
				for (int i = 0; i < list.size(); i++) {
					ContractApplyOrder order = list.get(i);
					List<Realtime> realtime_list = this.dataService.realtime(order.getSymbol());
					Realtime realtime = null;
					if (realtime_list.size() > 0) {
						realtime = realtime_list.get(0);
					} else {
						continue;
					}

					// 休市不做撮合
					Item bySymbol = itemService.findBySymbol(order.getSymbol());
					if(bySymbol == null){
						continue;
					}
					boolean isOpen = MarketOpenChecker.isMarketOpenByItemCloseType(bySymbol.getOpenCloseType());
					if(!isOpen){
						continue;
					}

					if ("limit".equals(order.getOrderPriceType())) {
						/**
						 * 限价单
						 */
						if ("buy".equals(order.getDirection())) {
							/**
							 * 买涨
							 */
							if (realtime.getClose().compareTo(order.getPrice())<=0) {
								this.handle(order, realtime);
							}
						} else {
							/**
							 * 买跌
							 */
							if (realtime.getClose().compareTo(order.getPrice())>=0){
								this.handle(order, realtime);
							}

						}

					} else {
						/**
						 * 非限制，直接进入市 场
						 */
						this.handle(order, realtime);
					}

				}

			} catch (Exception e) {
				log.error("run fail", e);
			} finally {
				ThreadUtils.sleep(1000 * 1);
			}

	}

	public void handle(ContractApplyOrder applyOrder, Realtime realtime) {
		boolean lock = false;
		try {
			if (!ContractLock.add(applyOrder.getOrderNo())) {
				return;
			}
			lock = true;
			if ("open".equals(applyOrder.getOffSet())) {
				this.contractOrderService.saveOpen(applyOrder, realtime);
			} else if ("close".equals(applyOrder.getOffSet())) {
				/**
				 * 平仓
				 */
				List<ContractOrder> list = this.contractOrderService.findSubmitted(applyOrder.getPartyId().toString(),
						applyOrder.getSymbol(), applyOrder.getDirection());
				if (list.size() == 0) {
					applyOrder.setVolume(BigDecimal.ZERO);
					applyOrder.setState(ContractApplyOrder.STATE_CREATED);
					this.contractApplyOrderService.updateById(applyOrder);
				}
				for (int i = 0; i < list.size(); i++) {
					ContractOrder order = list.get(i);
					boolean lock_order = false;
					try {
						if (!ContractLock.add(order.getOrderNo())) {
							continue;
						}
						lock_order = true;
						applyOrder = this.contractOrderService.saveClose(applyOrder, realtime, order.getOrderNo());

						if (ContractApplyOrder.STATE_CREATED.equals(applyOrder.getState())) {
							break;
						}
					} catch (Exception e) {
						log.error("error:", e);
					} finally {
						if (lock_order) {
							ThreadUtils.sleep(100);
							ContractLock.remove(order.getOrderNo());
						}

					}

				}

			}

		} catch (Exception e) {
			log.error("error:", e);
		} finally {
			if (lock) {
				ThreadUtils.sleep(100);
				ContractLock.remove(applyOrder.getOrderNo());
			}

		}

	}
	public void start(){
		new Thread(this, "ContractApplyOrderHandleJob").start();
		if (log.isInfoEnabled())
			log.info("委托单处理线程启动！");
	}

	public void setContractOrderService(ContractOrderService contractOrderService) {
		this.contractOrderService = contractOrderService;
	}

	public void setContractApplyOrderService(ContractApplyOrderService contractApplyOrderService) {
		this.contractApplyOrderService = contractApplyOrderService;
	}

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

}
