package com.yami.trading.service.contract;


import org.springframework.stereotype.Component;

@Component
public class ContractLockServiceImpl implements ContractLockService {

	/**
	 * 获取data服永续合约订单锁
	 */
	public boolean getContractLock(String orderNo) {
		return ContractLock.add(orderNo);
	}
	
	/**
	 * 删除锁
	 */
	public void removeContractLock(String orderNo) {
		ContractLock.remove(orderNo);
	}
}
