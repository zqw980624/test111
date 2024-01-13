package com.yami.trading.service.c2c.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.c2c.C2cUserParamBaseSet;
import com.yami.trading.bean.model.C2cTranslate;
import com.yami.trading.dao.c2c.C2cTranslateMapper;
import com.yami.trading.dao.c2c.C2cUserParamBaseSetMapper;
import com.yami.trading.service.c2c.C2cUserParamBaseSetService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class C2cUserParamBaseSetServiceImpl extends ServiceImpl<C2cUserParamBaseSetMapper, C2cUserParamBaseSet> implements C2cUserParamBaseSetService {
    @Override
    public C2cUserParamBaseSet getByPartyId(String c2c_user_party_id) {
        List<C2cUserParamBaseSet> list = list(Wrappers.<C2cUserParamBaseSet>query().lambda().eq(C2cUserParamBaseSet::getC2cUserPartyId, c2c_user_party_id));
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }
}
