package com.yami.trading.admin.task.contract;

import cn.hutool.core.collection.CollectionUtil;
import com.yami.trading.bean.contract.domain.ContractOrder;
import com.yami.trading.common.config.ThreadPoolComponent;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.service.contract.ContractLock;
import com.yami.trading.service.contract.ContractOrderCalculationService;
import com.yami.trading.service.contract.ContractOrderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ContractOrderCalculationJob implements Runnable {
	private static Log logger = LogFactory.getLog(ContractOrderCalculationJob.class);
	@Autowired
	private ContractOrderService contractOrderService;
	@Autowired
	private ContractOrderCalculationService contractOrderCalculationService;
	@Autowired
	private ThreadPoolComponent threadPoolComponent;
	public void run() {

		while (true) {
			try {
				List<ContractOrder> list = this.contractOrderService.findSubmitted();
				Map<String, List<ContractOrder>> partyIdContractOrders = list.stream().collect(Collectors.groupingBy(ContractOrder::getPartyId));

				Set<String> partyIds = partyIdContractOrders.keySet();
				// 按提交者进行分组，避免多次查询
				for(String partyId : partyIds){
					threadPoolComponent.getExecutor().execute(()->{
						List<ContractOrder> l = partyIdContractOrders.get(partyId);
						for (int i = 0; i < l.size(); i++) {
							ContractOrder order = l.get(i);

							boolean lock = false;
							try {
								if (!ContractLock.add(order.getOrderNo())) {
									continue;
								}
								lock = true;
								this.contractOrderCalculationService.saveCalculation(order.getOrderNo(), l);

							} catch (Throwable e) {
								logger.error("error:", e);
							} finally {
								if (lock) {
									ContractLock.remove(order.getOrderNo());
								}

							}

						}
					});
				}





			} catch (Throwable e) {
				e.printStackTrace();
				logger.error("run fail", e);
			} finally {
				/**
				 * 暂停0.1秒
				 */
				ThreadUtils.sleep(3000);
			}
		}
	}

	public void start(){

		new Thread(this, "ContractOrderCalculationJob").start();
		if (logger.isInfoEnabled()) {
			logger.info("持仓单盈亏计算线程启动！");
		}

	}

	public void setContractOrderService(ContractOrderService contractOrderService) {
		this.contractOrderService = contractOrderService;
	}

	public void setContractOrderCalculationService(ContractOrderCalculationService contractOrderCalculationService) {
		this.contractOrderCalculationService = contractOrderCalculationService;
	}

}
