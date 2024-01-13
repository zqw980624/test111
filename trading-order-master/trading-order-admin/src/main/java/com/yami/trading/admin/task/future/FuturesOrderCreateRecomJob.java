package com.yami.trading.admin.task.future;

import com.yami.trading.sys.model.SysLog;
import com.yami.trading.sys.service.SysLogService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.Date;

/**
 * 凌晨4点执行
 */
@Slf4j
@Component
public class FuturesOrderCreateRecomJob {
	@Autowired
	protected SysLogService sysLogService;
	@Autowired
	private FuturesOrderCreateRecomService futuresOrderCreateRecomService;

	public void taskJob() {
		try {
			futuresOrderCreateRecomService.computeRecom();
		} catch (Exception e) {
			// todo 日志位置不对
			log.error("FuturesOrderCreateRecomJob run fail e:", e);
			SysLog entity = new SysLog();
			entity.setCreateDate(new Date());
			entity.setOperation("FuturesOrderCreateRecomJob 交割购买奖励任务 执行失败 e:" + e);
			sysLogService.save(entity);
		}
	}

}
