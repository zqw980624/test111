package com.yami.trading.admin.facade;

import com.yami.trading.bean.future.domain.ProfitLossConfig;
import com.yami.trading.bean.model.Log;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.service.future.ProfitLossConfigService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.service.user.UserService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class ProfitAndLossConfigServiceFacade {
    @Autowired
    private UserService userService;
    @Autowired
    private LogService logService;
    @Autowired
    @Getter
    private ProfitLossConfigService profitLossConfigService;
    public void save(ProfitLossConfig entity, String operaterUsername) {
        ProfitLossConfig profitAndLossConfig = profitLossConfigService.findByPartyId(entity.getPartyId());
        // 如果存在则更新
        if (profitAndLossConfig != null) {
            profitAndLossConfig.setRemark(entity.getRemark());
            profitAndLossConfig.setType(entity.getType());
            profitLossConfigService.updateById(profitAndLossConfig);
        } else {
            profitLossConfigService.save(entity);
        }

        String type = "";

        if("profit".equals(entity.getType())) {
            type = "盈利";
        }
        if("loss".equals(entity.getType())) {
            type = "亏损";
        }
        if("buy_profit".equals(entity.getType())) {
            type = "买多盈利";
        }
        if("sell_profit".equals(entity.getType())) {
            type = "买空盈利";
        }
        if("buy_profit_sell_loss".equals(entity.getType())) {
            type = "买多盈利并且买空亏损";
        }
        if("sell_profit_buy_loss".equals(entity.getType())) {
            type = "买空盈利并且买多亏损";
        }

        User party = this.userService.getById(entity.getPartyId());
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_OPERATION);
        log.setOperator(operaterUsername);
        log.setUsername(party.getUserName());
        log.setUserId(entity.getPartyId());
        log.setCreateTime(new Date());
        log.setLog("管理员手动添加场控交割状态。操作类型[" + type + "]。");
        this.logService.save(log);
    }

    public void update(ProfitLossConfig entity,String Operater_username) {
        profitLossConfigService.updateById(entity);

        String type = "";

        if("profit".equals(entity.getType())) {
            type = "盈利";
        }
        if("loss".equals(entity.getType())) {
            type = "亏损";
        }
        if("buy_profit".equals(entity.getType())) {
            type = "买多盈利";
        }
        if("sell_profit".equals(entity.getType())) {
            type = "买空盈利";
        }
        if("buy_profit_sell_loss".equals(entity.getType())) {
            type = "买多盈利并且买空亏损";
        }
        if("sell_profit_buy_loss".equals(entity.getType())) {
            type = "买空盈利并且买多亏损";
        }
        User party = this.userService.getById(entity.getPartyId());
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_OPERATION);
        log.setOperator(Operater_username);
        log.setUsername(party.getUserName());
        log.setUserId(entity.getPartyId());
        log.setCreateTime(new Date());
        log.setLog("管理员手动修改场控交割状态。修改后操作类型为[" + type + "]。");
        this.logService.save(log);
    }

    public void delete(String id,String operaterUsername) {
        ProfitLossConfig entity = profitLossConfigService.getById(id);
        if(entity == null){
            log.error("ProfitLossConfig id 为 {} 不存在", id );
            return;
        }
        profitLossConfigService.removeById(id);
        User party = userService.getById(entity.getPartyId());
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_OPERATION);
        log.setOperator(operaterUsername);
        log.setUsername(party.getUserName());
        log.setUserId(entity.getPartyId());
        log.setCreateTime(new Date());
        log.setLog("管理员手动删除场控交割状态");
        this.logService.save(log);
    }
}
