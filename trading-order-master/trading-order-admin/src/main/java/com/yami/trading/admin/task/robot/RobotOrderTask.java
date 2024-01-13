package com.yami.trading.admin.task.robot;

import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.service.etf.MarketService;
import com.yami.trading.service.robot.RobotOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class RobotOrderTask {

    @Autowired
    RobotOrderService robotOrderService;

    @Autowired
    MarketService marketService;

    @Scheduled(cron = "*/20 * * * * ?")
    public void updateRobotOrder() {
        robotOrderService.query().eq("status", 1).list().forEach(robotOrder -> {
            Realtime realtime = marketService.queryRealtime(robotOrder.getSymbol());
            if (realtime != null && realtime.getTs() != 0) {
                if (robotOrder.getDirection() == 1 && realtime.getClose().compareTo(BigDecimal.valueOf(robotOrder.getPrice())) <= 0) {
                    robotOrder.setStatus(2);
                    robotOrder.setTurnover(robotOrder.getOrderQuantity());
                    robotOrderService.updateById(robotOrder);
                } else if (robotOrder.getDirection() == 2 && realtime.getClose().compareTo(BigDecimal.valueOf(robotOrder.getPrice())) >= 0) {
                    robotOrder.setStatus(2);
                    robotOrder.setTurnover(robotOrder.getOrderQuantity());
                    robotOrderService.updateById(robotOrder);
                }
            }
        });
    }

}
