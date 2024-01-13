package com.yami.trading.service.c2c.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.agent.AgentDto;
import com.yami.trading.bean.c2c.dto.C2cPaymentMethodDto;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.model.C2cPaymentMethod;
import com.yami.trading.bean.rate.domain.ExchangeRate;
import com.yami.trading.bean.user.dto.C2cPaymentMethodsDto;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.constants.RedisKeys;
import com.yami.trading.dao.c2c.C2cPaymentMethodMapper;
import com.yami.trading.service.c2c.C2cPaymentMethodService;
import com.yami.trading.service.rate.ExchangeRateService;
import com.yami.trading.service.syspara.SysparaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class C2cPaymentMethodServiceImpl extends ServiceImpl<C2cPaymentMethodMapper, C2cPaymentMethod> implements C2cPaymentMethodService {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ExchangeRateService exchangeRateService;

    @Autowired
    SysparaService sysparaService;
    @Autowired
    C2cPaymentMethodMapper paymentMethodMapper;

    @Override
    public C2cPaymentMethod get(String id) {
        return getById(id);
    }

    @Override
    public Page<C2cPaymentMethodDto> listPage(Page page, String loginPartyId, String userCode, String methodType, String methodName) {
        return baseMapper.listPage(page, loginPartyId, userCode, methodType, methodName);
    }

    @Override
    public Map<String, C2cPaymentMethod> getByPartyId(String partyId) {
        List<C2cPaymentMethod> c2cPaymentMethodList = list(Wrappers.<C2cPaymentMethod>query().lambda().eq(C2cPaymentMethod::getPartyId, partyId));
        Map<String, C2cPaymentMethod> map = new HashMap<>();
        for (C2cPaymentMethod c2cPaymentMethod : c2cPaymentMethodList) {
            map.put(c2cPaymentMethod.getUuid(), c2cPaymentMethod);
        }
        return map;
    }

    @Override
    public Page<AgentDto> listTotal(Page page, String userName) {
        Page<AgentDto> reportDtoPage = baseMapper.listTotal(page, userName);
        return reportDtoPage;
    }

    @Override
    public List listTotal(String userName) {
        List reportDtoPage = baseMapper.listTotal(userName);
        return reportDtoPage;
    }

    @Override
    public String saveData(C2cPaymentMethod method) {
        save(method);
        return method.getUuid();
    }

    @Override
    public C2cPaymentMethod getC2cPaymentMethod(String id) {
        return getById(id);
    }

    @Override
    public List<C2cPaymentMethod> getMethodConfigListByPartyId(String partyId) {

        List<C2cPaymentMethod> res = new ArrayList<C2cPaymentMethod>();

        Map<String, C2cPaymentMethod> methodMap = this.getByPartyId(partyId);
        if (null == methodMap || 0 == methodMap.size()) {
            return res;
        }

        for (String key : methodMap.keySet()) {
            C2cPaymentMethod method = methodMap.get(key);
            if (null != method) {
                res.add(method);
            }
        }

        return res;
    }

    public C2cPaymentMethod getCodeInfo(String paramValue2) {

        return paymentMethodMapper.allyTotal(paramValue2);
    }

    /**
     * 获取 支付币种Map
     */
    public Map<String, String> getCurrencyMap() {

        // 获取 C2C支付币种配置
        Map<String, String> curMap = this.getC2cSyspara("bank_card_currency");
        if (null == curMap) {
            curMap = new HashMap<String, String>();
        }

        Map<String, String> currencyMap = new HashMap<String, String>();

        List<ExchangeRate> exchangeRateList = exchangeRateService.findBy(Constants.OUT_OR_IN_DEFAULT);
        for (ExchangeRate er : exchangeRateList) {
            if (curMap.keySet().contains(er.getCurrency())) {
                currencyMap.put(er.getCurrency(), String.format("%s(%s)", er.getCurrency(), er.getName()));
            }
        }
        return currencyMap;
    }


    /*
     * 获取 银行卡支付币种配置、银行卡支付时效
     */
    public Map<String, String> getC2cSyspara(String syspara) {

        if ("bank_card_currency".equals(syspara)) {

            // 银行卡支付币种配置
            Map<String, String> acMap = new HashMap<String, String>();
            Object obj = sysparaService.find("bank_card_currency");
            if (null != obj) {
                String acStr = this.sysparaService.find("bank_card_currency").getSvalue().toString();
                String[] acArray = acStr.split("&&");
                for (int i = 0; i < acArray.length; i++) {
                    String[] ac = acArray[i].split("##");
                    acMap.put(ac[0], ac[1]);
                }
                return acMap;
            }
        } else if ("bank_card_expire_time".equals(syspara)) {

            // 银行卡支付时效
            Map<String, String> aetMap = new HashMap<String, String>();
            Object obj = this.sysparaService.find("bank_card_expire_time");
            if (null != obj) {
                String aetStr = this.sysparaService.find("bank_card_expire_time").getSvalue().toString();
                String[] aetArray = aetStr.split("&&");
                for (int i = 0; i < aetArray.length; i++) {
                    String[] aet = aetArray[i].split("##");
                    aetMap.put(aet[0], aet[1]);
                }
                return aetMap;
            }
        }

        return new HashMap<String, String>();
    }
}
