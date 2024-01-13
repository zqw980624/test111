package com.yami.trading.service.rate;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yami.trading.common.constants.RedisKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.rate.domain.ExchangeRate;
import com.yami.trading.dao.rate.ExchangeRateMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 汇率管理Service
 * @author lucas
 * @version 2023-03-28
 */
@Service
@Transactional
public class ExchangeRateService extends ServiceImpl<ExchangeRateMapper, ExchangeRate> {

    @Autowired
    RedisTemplate redisTemplate;



    public ExchangeRate findBy(String out_or_in, String currency) {

        return getOne(Wrappers.<ExchangeRate>query().lambda().eq(ExchangeRate::getOutOrIn,out_or_in).eq(ExchangeRate::getCurrency,currency));
    }

    public List<ExchangeRate> findBy(String out_or_in) {
        List<ExchangeRate> list=  list(Wrappers.<ExchangeRate>query().lambda().eq(ExchangeRate::getOutOrIn,out_or_in));
        return list;

    }


}
