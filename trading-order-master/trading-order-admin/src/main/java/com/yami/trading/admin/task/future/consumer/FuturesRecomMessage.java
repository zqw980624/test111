package com.yami.trading.admin.task.future.consumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FuturesRecomMessage {

	private String orderNo;
	
	private String partyId;
	
	private BigDecimal volume;
	
	private Date createTime;

}
