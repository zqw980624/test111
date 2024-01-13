package com.yami.trading.common.util;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class OffLineEventRejectExecutingHandler implements RejectedExecutionHandler {
	
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        ThreadUtils.sleep(1000 * 10);
        executor.execute(r);
    }
}
