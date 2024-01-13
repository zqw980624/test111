package com.yami.trading.service.contract;

public interface ContractLockService {

	/**
	 * 获取data服永续合约订单锁
	 */
	public boolean getContractLock(String orderNo);

	/**
	 * 删除锁
	 */
	public void removeContractLock(String orderNo);
}
