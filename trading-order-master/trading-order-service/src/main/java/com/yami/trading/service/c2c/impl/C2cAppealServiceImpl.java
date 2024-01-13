package com.yami.trading.service.c2c.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.c2c.C2cAppeal;
import com.yami.trading.bean.c2c.C2cOrder;
import com.yami.trading.bean.model.Log;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.dao.c2c.C2cAppealMapper;
import com.yami.trading.service.c2c.C2cAppealService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.service.system.TipService;
import com.yami.trading.service.user.UserService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class C2cAppealServiceImpl extends ServiceImpl<C2cAppealMapper, C2cAppeal> implements C2cAppealService {


    @Autowired
    TipService tipService;

    @Autowired
    UserService userService;

    @Autowired
    LogService logService;

    @Override
    public Page pagedQuery(Page page, String status, String orderNo, String userCode, String roleName, String c2cUserCode, String c2cUserType, String c2cUserPartyCode) {
        return baseMapper.pagedQuery(page,status,orderNo,userCode,roleName,c2cUserCode,c2cUserType,c2cUserPartyCode);
    }

    @Override
    public C2cAppeal findByOrderNo(String orderNo) {
       List<C2cAppeal> list= list(Wrappers.<C2cAppeal>query().lambda().eq(C2cAppeal::getOrderNo,orderNo));
        if (CollectionUtils.isNotEmpty(list)){
            return list.get(0);
        }
        return null;
    }
    /*
     * 查询未处理申诉数量，根据广告ID
     */
    public Long findNoHandleAppealsCountByAdvertId(String c2cAdvertId) {
        return baseMapper.findNoHandleAppealsCountByAdvertId(c2cAdvertId);
    }



    @Override
    @Transactional
    public void handled(C2cAppeal appeal, String username, String partyId) {
        if ("1".equals(appeal.getState())) {
            throw new YamiShopBindException("申诉已处理了");
        }
        appeal.setState("1");
        appeal.setUpdateTime(new Date());
        updateById(appeal);
        tipService.deleteTip(appeal.getUuid());
        User  user = userService.getById(partyId);
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_C2C);
        log.setUsername(user.getUserName());
        log.setUserId(partyId);
        log.setOperator(username);
        log.setLog("处理申诉");
        log.setCreateTime(new Date());
        logService.save(log);
    }
}
