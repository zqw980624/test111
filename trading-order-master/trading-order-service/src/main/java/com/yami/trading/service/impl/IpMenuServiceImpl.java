package com.yami.trading.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.model.HighLevelAuthRecord;
import com.yami.trading.bean.model.IpMenu;
import com.yami.trading.common.constants.DdosRedisKeys;
import com.yami.trading.common.util.BlacklistIpSerivceTimeWindow;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.common.util.LocklistIpSerivceTimeWindow;
import com.yami.trading.dao.IpMenuMapper;
import com.yami.trading.dao.user.HighLevelAuthRecordMapper;
import com.yami.trading.service.IpMenuService;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class IpMenuServiceImpl   extends ServiceImpl<IpMenuMapper, IpMenu> implements IpMenuService, InitializingBean {

    @Autowired
    private LocklistIpSerivceTimeWindow locklistIpSerivceTimeWindow;

    @Autowired
    private BlacklistIpSerivceTimeWindow blacklistIpSerivceTimeWindow;

    @Autowired
    private RedisTemplate  redisTemplate;

    public void init() {


        List<IpMenu> list =  list(Wrappers.<IpMenu>query().lambda().eq(IpMenu::getDeleteStatus,0));;
        for (IpMenu ipMenu : list) {
            if (DateUtils.addHour(ipMenu.getLastOperaTime(), 24).after(new Date())) { // 黑名单 时间+1天>现在
                switch (ipMenu.getType()) {
                    case IpMenu.IP_BLACK:
                        blacklistIpSerivceTimeWindow.putBlackIp(ipMenu.getIp(), ipMenu.getIp());
                        break;
                    case IpMenu.IP_LOCK:
                        locklistIpSerivceTimeWindow.putLockIp(ipMenu.getIp(), ipMenu.getIp());
                        break;
                }
            } else {
                checkTimeWindows(ipMenu);
                ipMenu.setDeleteStatus(-1);
                redisTemplate.opsForValue().set(DdosRedisKeys.IP_MENU_IP + ipMenu.getIp(), ipMenu);
            }
        }
    }

    @Override
    public void saveIpMenu(IpMenu entity) {

        if(null==entity) return;
        checkTimeWindows(entity);
        save(entity);
        redisTemplate.opsForValue().set(DdosRedisKeys.IP_MENU_IP + entity.getIp(), entity);
    }

    @Override
    public void updateIpMenu(IpMenu entity) {
        if(null==entity) return;
        checkTimeWindows(entity);
        updateById(entity);
        redisTemplate.opsForValue().set(DdosRedisKeys.IP_MENU_IP + entity.getIp(), entity);
    }

    @Override
    public void deleteIpMenu(IpMenu entity) {
        if(null==entity) return;
        removeById(entity);
        redisTemplate.delete(DdosRedisKeys.IP_MENU_IP +entity.getIp());
    }

    @Override
    public IpMenu cacheByIp(String ip) {
        return (IpMenu) redisTemplate.opsForValue().get(DdosRedisKeys.IP_MENU_IP + ip);
    }

    /**
     * 新增ip到白名单
     * @param ip
     */
    public void saveIpMenuWhite(String ip) {
        IpMenu ipMenu = this.cacheByIp(ip);
        if (null == ipMenu) {
            ipMenu = new IpMenu();
            ipMenu.setCreateTime(new Date());
            ipMenu.setDeleteStatus(0);
            ipMenu.setLastOperaTime(new Date());
            ipMenu.setType(IpMenu.IP_WHITE);
            ipMenu.setIp(ip);
            saveIpMenu(ipMenu);
        } else if (ipMenu.getDeleteStatus() == -1 || !IpMenu.IP_WHITE.equals(ipMenu.getType())) {// 名单被删除或者不是白名单
            ipMenu.setDeleteStatus(0);
            ipMenu.setLastOperaTime(new Date());
            ipMenu.setType(IpMenu.IP_WHITE);
            updateIpMenu(ipMenu);
        }
    }

    public void checkTimeWindows(IpMenu entity) {
        if (entity.getDeleteStatus() == -1) {
            blacklistIpSerivceTimeWindow.delBlackIp(entity.getIp());
            locklistIpSerivceTimeWindow.delLockIp(entity.getIp());
            return;
        }

        switch (entity.getType()) {
            case IpMenu.IP_WHITE:
                if (blacklistIpSerivceTimeWindow.getBlackIp(entity.getIp()) != null) {// 白名单直接删除黑名单缓存
                    blacklistIpSerivceTimeWindow.delBlackIp(entity.getIp());
                }
                if (locklistIpSerivceTimeWindow.getLockIp(entity.getIp()) != null) {// 白名单直接删除锁定名单缓存
                    locklistIpSerivceTimeWindow.delLockIp(entity.getIp());
                }
                break;
            case IpMenu.IP_BLACK:
                if (locklistIpSerivceTimeWindow.getLockIp(entity.getIp()) != null) {// 删除锁定名单缓存
                    locklistIpSerivceTimeWindow.delLockIp(entity.getIp());
                }
                blacklistIpSerivceTimeWindow.putBlackIp(entity.getIp(), entity.getIp());
                break;
            case IpMenu.IP_LOCK:
                if (blacklistIpSerivceTimeWindow.getBlackIp(entity.getIp()) != null) {// 删除黑名单缓存
                    blacklistIpSerivceTimeWindow.delBlackIp(entity.getIp());
                }
                locklistIpSerivceTimeWindow.putLockIp(entity.getIp(), entity.getIp());
                break;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }
}
