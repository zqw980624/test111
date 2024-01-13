package com.yami.trading.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class RejectExecutionHandlerDelegator implements RejectedExecutionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(RejectExecutionHandlerDelegator.class);
    
    private Collection<RejectedExecutionHandler> rejectExecutionHandlers = new ArrayList<RejectedExecutionHandler>();
    /* (non-Javadoc)
     * @see java.util.concurrent.RejectedExecutionHandler#rejectedExecution(java.lang.Runnable, java.util.concurrent.ThreadPoolExecutor)
     */
    public void rejectedExecution(Runnable runner, ThreadPoolExecutor executor) {
        logger.warn("do rejected Execution with runner[" + runner + "], executor[" + executor + "]");
        for(RejectedExecutionHandler rejectExecutionHandler : rejectExecutionHandlers){
            rejectExecutionHandler.rejectedExecution(runner, executor);
        }
    }
    
    public void setRejectExecutionHandlers(Collection<RejectedExecutionHandler> rejectExecutionHandlers) {
        Assert.notEmpty(rejectExecutionHandlers);
        this.rejectExecutionHandlers = rejectExecutionHandlers;
    }

}
