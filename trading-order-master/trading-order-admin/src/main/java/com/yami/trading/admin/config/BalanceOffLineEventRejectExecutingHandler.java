package com.yami.trading.admin.config;

import com.yami.trading.common.util.ThreadUtils;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class BalanceOffLineEventRejectExecutingHandler implements RejectedExecutionHandler {

	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		//等1秒后重试
		ThreadUtils.sleep(200);
		executor.execute(r);
	}
}
