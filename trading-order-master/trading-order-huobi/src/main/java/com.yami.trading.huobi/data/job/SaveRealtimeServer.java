package com.yami.trading.huobi.data.job;

import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.huobi.data.internal.DataDBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SaveRealtimeServer implements Runnable {
	
	private static Logger logger = LoggerFactory.getLogger(SaveRealtimeServer.class);

	@Autowired
	private DataDBService dataDBService;
	
	public void start() {
		new Thread(this, "SaveRealtimeServer").start();
		if (logger.isInfoEnabled()) {
			logger.info("启动SaveRealtimeServer！");
		}

	}
	public void run() {
		while (true) {
			try {
				int size = RealtimeQueue.size();
				/**
				 * 现量轮询一圈
				 */
				List<Realtime> list = new ArrayList<Realtime>();
				for (int i = 0; i < size; i++) {
					Realtime item = RealtimeQueue.poll();
					list.add(item);

				}
				if (list.size() > 0) {
					dataDBService.saveBatch(list);
				}

			} catch (Throwable e) {
				logger.error(" run fail", e);
			} finally {
				ThreadUtils.sleep(60 * 1000);
			}
		}
	}

	

	public void setDataDBService(DataDBService dataDBService) {
		this.dataDBService = dataDBService;
	}

}
