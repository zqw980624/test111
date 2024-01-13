package com.yami.trading.service.rate;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yami.trading.bean.rate.domain.ExchangeRate;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.rate.domain.UserRateConfig;
import com.yami.trading.dao.rate.UserRateConfigMapper;

/**
 * 用户汇率管理Service
 *
 * @author lucas
 * @version 2023-03-28
 */
@Service
@Transactional
public class UserRateConfigService extends ServiceImpl<UserRateConfigMapper, UserRateConfig> {
    @Autowired
    ExchangeRateService exchangeRateService;

    public void update(String rateId, String partyId) {
        ExchangeRate exchangeRate = exchangeRateService.getById(rateId);
        if (null == exchangeRate) {
            throw new YamiShopBindException("rate is null");
        }
        UserRateConfig userConfig = this.getByPartyId(partyId);
        if (userConfig == null) {
            userConfig = new UserRateConfig();
            userConfig.setPartyId(partyId);
        }
        userConfig.setCurrency(exchangeRate.getCurrency());
        saveOrUpdate(userConfig);
    }

    /**
     * 查询用户计价方式，如果没有配置，则返回默认的计价方式
     */
    public ExchangeRate findUserConfig(String partyId) {
        ExchangeRate exchangeRate = null;
        /*String user_default_currency = "USD";
        if (StringUtils.isNullOrEmpty(partyId)) {
            exchangeRate = exchangeRateService.findBy(ExchangeRate.IN, user_default_currency);
        } else {
            UserRateConfig userRateConfig = getByPartyId(partyId);
            if (userRateConfig == null) {
                exchangeRate = exchangeRateService.findBy(ExchangeRate.IN, user_default_currency);
            } else {
                exchangeRate = exchangeRateService.findBy(ExchangeRate.IN, userRateConfig.getCurrency());
            }
        }*/
        exchangeRate = exchangeRateService.findBy(ExchangeRate.IN, "INR");
        return exchangeRate;
    }

    public UserRateConfig getByPartyId(String partyId) {
        return getOne(Wrappers.<UserRateConfig>query().lambda().eq(UserRateConfig::getPartyId, partyId));
    }
}
