package com.yami.trading.admin.facade;


import cn.hutool.core.util.StrUtil;
import com.yami.trading.bean.future.domain.FuturesPara;
import com.yami.trading.bean.model.Log;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.service.future.FuturesParaService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.service.SysUserService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Date;

@Component
@Slf4j
public class FuturesParaFacade {
    @Autowired
    private LogService logService;
    @Autowired
    private SysparaService sysparaService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    @Getter
    private FuturesParaService futuresParaService;

    public void addFutures(FuturesPara entity, String ip, String operaUsername, String loginSafeword) {
        sysUserService.checkSafeWord(loginSafeword);
        SysUser sec = sysUserService.getByUserName(operaUsername);
        String logContent = "ip:" + ip;
        String id = entity.getUuid();
        if (id != null && StrUtil.isNotEmpty(id)) {
            FuturesPara futuresById = futuresParaService.getById(id);
            if (null == futuresById) {
                log.info("futures is null ,id:{}", id);
                throw new YamiShopBindException("合约参数不存在");
            }
            logContent += MessageFormat.format(",管理员修改交割参数，币种:{0},原时间长度:{1},原时间单位:{2},原最低购买金额:{3},原手续费:{4},原浮动最小收益率:{5},原浮动最大收益率:{6}",
                    futuresById.getSymbol(), futuresById.getTimenum(), futuresById.getTimeunit(), futuresById.getUnitAmount(), futuresById.getUnitFee(), futuresById.getProfitRatio(), futuresById.getProfitRatioMax());
            BeanUtils.copyProperties(entity, futuresById);// 是否做用户控制
            futuresParaService.updateById(entity);
            logContent += MessageFormat.format(",新时间长度:{0},新时间单位:{1},新最低购买金额:{2},新手续费:{3},新浮动最小收益率:{4},新浮动最大收益率:{5}",
                    futuresById.getTimenum(), futuresById.getTimeunit(), futuresById.getUnitAmount(), futuresById.getUnitFee(), futuresById.getProfitRatio(), futuresById.getProfitRatioMax());
            saveLog(sec, operaUsername, logContent);
        }else{
            logContent += MessageFormat.format(",管理员新增交割参数，币种:{0},时间长度:{1},时间单位:{2},最低购买金额:{3},手续费:{4},浮动最小收益率:{5},浮动最大收益率:{6}",
                    entity.getSymbol(), entity.getTimenum(), entity.getTimeunit(), entity.getUnitAmount(), entity.getUnitFee(), entity.getProfitRatio(), entity.getProfitRatioMax());
            futuresParaService.save(entity);
            saveLog(sec, operaUsername, logContent);
        }

    }

    public void deleteFuturesPara(String id, String ip, String operaUsername, String loginSafeword, String superGoogleAuthCode) {
        sysUserService.checkSuperGoogleAuthCode(superGoogleAuthCode);
        sysUserService.checkSafeWord(loginSafeword);
        SysUser sec = sysUserService.getByUserName(operaUsername);

        FuturesPara entity = futuresParaService.getById(id);
        if (null == entity) {
            throw new YamiShopBindException("交易参数不存在");
        }
        String logContent = "ip:" + ip;
        logContent += MessageFormat.format(",管理员删除交割参数，币种:{0},时间长度:{1},时间单位:{2},最低购买金额:{3},手续费:{4},浮动最小收益率:{5},浮动最大收益率:{6}",
                entity.getSymbol(), entity.getTimenum(), entity.getTimeunit(), entity.getUnitAmount(), entity.getUnitFee(), entity.getProfitRatio(), entity.getProfitRatioMax());
        futuresParaService.removeById(entity.getUuid());
        saveLog(sec, operaUsername, logContent);
    }


    public void saveLog(SysUser secUser, String operator, String context) {
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_OPERATION);
        log.setOperator(operator);
        log.setUsername(secUser.getUsername());
        log.setUserId(secUser.getUserId().toString());
        log.setLog(context);
        log.setCreateTime(new Date());
        logService.save(log);
    }

}
