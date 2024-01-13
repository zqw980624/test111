package com.yami.trading.service.contract;

import com.yami.trading.bean.contract.domain.ContractOrder;

import java.math.BigDecimal;
import java.util.List;

/**
 * 合约盈亏计算
 */
public interface ContractOrderCalculationService {

	/*
	 * 订单盈亏计算
	 */
	public void saveCalculation(String order_no, List<ContractOrder> partyContractOrders);
	
	public void setOrder_close_line(BigDecimal order_close_line);

	public void setOrder_close_line_type(int order_close_line_type);

}
