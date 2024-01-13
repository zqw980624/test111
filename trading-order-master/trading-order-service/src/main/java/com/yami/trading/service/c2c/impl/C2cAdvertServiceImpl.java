package com.yami.trading.service.c2c.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.c2c.C2cAdvert;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.model.C2cTranslate;
import com.yami.trading.bean.rate.domain.ExchangeRate;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.Arith;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.dao.c2c.C2cAdvertMapper;
import com.yami.trading.service.c2c.C2cAdvertService;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.rate.ExchangeRateService;
import com.yami.trading.service.syspara.SysparaService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;

@Service
public class C2cAdvertServiceImpl extends ServiceImpl<C2cAdvertMapper, C2cAdvert> implements C2cAdvertService {
    @Autowired
    SysparaService sysparaService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ExchangeRateService exchangeRateService;

    @Autowired
    ItemService itemService;

    @Autowired
    DataService dataService;

    /**
     * 获取 上架币种Map
     */
    @Override
    public Map<String, String> getSymbolMap() {
        // 获取 C2C上架币种配置
        Map<String, String> symMap = this.getC2cSyspara("c2c_advert_symbol");
        if (null == symMap) {
            symMap = new HashMap<String, String>();
        }
        Map<String, String> symbolMap = new HashMap<String, String>();
        List<Item> itemList = this.itemService.cacheGetAll();
        if (symMap.keySet().contains("USDT")) {
            symbolMap.put("usdt", "USDT");
        }
        for (Item item : itemList) {
            if (symMap.keySet().contains(item.getSymbol().toUpperCase())) {
                symbolMap.put(item.getSymbol(), item.getSymbol().toUpperCase());
            }
        }
        return symbolMap;
    }

    public List<C2cAdvert> getByC2cUserId(String c2c_user_id) {

        return list(Wrappers.<C2cAdvert>query().lambda().eq(C2cAdvert::getC2cUserId, c2c_user_id));
    }




    @Override
    public Page pagedQuery(long pageNo, long pageSize, String c2cUserCode, String c2cUserType,String userCode, String direction,String currency,String symbol){
        Page page=new Page(pageNo,pageSize);
        return baseMapper.pagedQuery(page,c2cUserCode,c2cUserType,userCode,direction,currency,symbol);
    }

    /**
     * 计算广告参数
     */
    public Map<String, Object> getComputeValue(double deposit_total, String currency, String symbol, double coin_amount, double symbol_value) {

        Map<String, Object> result = new HashMap<String, Object>();
        double symbol_close = 0d;
        if (symbol.equals("usdt")) {
            symbol_close = 1;
        } else {
            List<Realtime> list = this.dataService.realtime(symbol);
            if (0 == list.size()) {
//				throw new BusinessException(symbol.toUpperCase() + "行情获取异常，请重试");
                throw new YamiShopBindException("行情获取异常，请重试");
            }
            Realtime realtime = list.get(0);
            symbol_close = realtime.getClose().doubleValue();
        }
        if (0 == symbol_close) {
//			throw new BusinessException(symbol.toUpperCase() + "行情获取异常，请重试");
            throw new YamiShopBindException("行情获取异常，请重试");
        }
        ExchangeRate ex = this.exchangeRateService.findBy(Constants.OUT_OR_IN_DEFAULT, currency);
        // 支付比率=支付币种汇率*上架币种实时行情价/币种单价；例如，支付比率95%，1USDT=7.3CNY*1*95%=6.935CNY
        double payRate = Arith.mul(Arith.div(Arith.mul(ex.getRata().doubleValue(), symbol_close), symbol_value), 100);
        // 广告保证金=交易币种数量*上架币种实时行情价
        double depositOpen = Arith.mul(coin_amount, symbol_close);
        // 支付币种市价=支付币种汇率*上架币种实时行情价；
        double price = Arith.mul(ex.getRata().doubleValue(), symbol_close);
        // 交易币种数量=广告派单额度/上架币种实时行情价
        double coinAmountMax = Arith.div(deposit_total, symbol_close);
        // 最小支付金额
        double investmentMinLimit = 0;
        // C2C承兑商广告：单笔订单最低限额不能低于（支付币种金额折算成USDT）
        Object obj = this.sysparaService.find("c2c_user_advert_investment_min_limit");
        if (null != obj) {
            String c2c_user_advert_investment_min_limit = this.sysparaService.find("c2c_user_advert_investment_min_limit").getSvalue();
            if (!StringUtils.isEmptyString(c2c_user_advert_investment_min_limit)) {
                double limit_usdt = Double.valueOf(c2c_user_advert_investment_min_limit).doubleValue();
                investmentMinLimit = Arith.mul(Arith.div(limit_usdt, symbol_close), symbol_value);
            }
        }
        // 最大支付金额
        double investmentMaxLimit = Arith.mul(coinAmountMax, symbol_value);
        DecimalFormat df = new DecimalFormat("#.########");
        result.put("pay_rate", (int) payRate);
        result.put("deposit_open", df.format(new Double(depositOpen)));
        result.put("all_deposit", df.format(Arith.sub(deposit_total, depositOpen)));
        result.put("symbol_close", df.format(new Double(symbol_close)));
        result.put("price", df.format(new Double(price)));
        result.put("coin_amount_max", df.format(new Double(coinAmountMax)));
        result.put("investment_min_limit", df.format(new Double(investmentMinLimit)));
        result.put("investment_max_limit", df.format(new Double(investmentMaxLimit)));
        return result;
    }

    @Override
    public Page pagedQuery(int page_no, int page_size, String c2c_user_id, String direction, String currency, String symbol, String amount, Integer on_sale, Integer closed, boolean is_c2c_user) {
        Page page=new Page(page_no,page_size);
        double amount_double=0;
        if (StringUtils.isNotEmpty(amount)){
            amount_double= Double.valueOf(amount).doubleValue();
        }


        if (is_c2c_user) {

            baseMapper.pagedQueryC2cUser(page,c2c_user_id,direction,currency,symbol,
                    amount_double,on_sale,closed);
        } else {
            baseMapper.pagedQueryNotC2cUser(page,c2c_user_id,direction,currency,symbol,
                    amount_double,on_sale,closed);
        }
        DecimalFormat df = new DecimalFormat("#.##");
        // 币种默认保留8位
        DecimalFormat dfCoin = new DecimalFormat("#.########");

        for (Map<String, Object> data : (List<Map<String, Object>>) page.getRecords()) {
            data.put("symbol_value", df.format(data.get("symbol_value")));
            data.put("coin_amount", dfCoin.format(data.get("coin_amount")));
        }
        return page;
    }

    @Override
    public Map<String, String> getCurrencyMap() {
        // 获取 C2C支付币种配置
        Map<String, String> curMap = this.getC2cSyspara("c2c_advert_currency");
        if (null == curMap) {
            curMap = new HashMap<String, String>();
        }
        Map<String, String> currencyMap = new HashMap<String, String>();
        List<ExchangeRate> exchangeRateList = this.exchangeRateService.findBy(Constants.OUT_OR_IN_DEFAULT);
        for (ExchangeRate er : exchangeRateList) {
            if (curMap.keySet().contains(er.getCurrency())) {
                currencyMap.put(er.getCurrency(), String.format("%s(%s)", er.getCurrency(), er.getName()));
            }
        }
        return currencyMap;
    }

    /*
     * 获取 C2C支付方式类型、C2C支付币种配置、C2C上架币种配置、C2C广告支付时效
     */

    @Override
    public Map<String, String> getC2cSyspara(String syspara) {

        if ("c2c_payment_method_type".equals(syspara)) {
            // C2C支付方式类型
            Map<String, String> pmtMap = new HashMap<String, String>();
            Object obj = this.sysparaService.find("c2c_payment_method_type");
            if (null != obj) {
                String pmtStr = this.sysparaService.find("c2c_payment_method_type").getSvalue().toString();
                String[] pmtArray = pmtStr.split("&&");
                for (int i = 0; i < pmtArray.length; i++) {
                    String[] pmt = pmtArray[i].split("##");
                    pmtMap.put(pmt[0], pmt[1]);
                }
                return pmtMap;
            }
        } else if ("c2c_advert_currency".equals(syspara)) {
            // C2C支付币种配置
            Map<String, String> acMap = new HashMap<String, String>();
            Object obj = this.sysparaService.find("c2c_advert_currency");
            if (null != obj) {
                String acStr = this.sysparaService.find("c2c_advert_currency").getSvalue().toString();
                String[] acArray = acStr.split("&&");
                for (int i = 0; i < acArray.length; i++) {
                    String[] ac = acArray[i].split("##");
                    acMap.put(ac[0], ac[1]);
                }
                return acMap;
            }
        } else if ("c2c_advert_symbol".equals(syspara)) {
            // C2C上架币种配置
            Map<String, String> asMap = new HashMap<String, String>();
            Object obj = this.sysparaService.find("c2c_advert_symbol");
            if (null != obj) {
                String asStr = this.sysparaService.find("c2c_advert_symbol").getSvalue().toString();
                String[] asArray = asStr.split("##");
                for (int i = 0; i < asArray.length; i++) {
                    asMap.put(asArray[i], asArray[i]);
                }
                return asMap;
            }
        } else if ("c2c_advert_expire_time".equals(syspara)) {
            // C2C广告支付时效
            Map<String, String> aetMap = new HashMap<String, String>();
            Object obj = this.sysparaService.find("c2c_advert_expire_time");
            if (null != obj) {
                String aetStr = this.sysparaService.find("c2c_advert_expire_time").getSvalue().toString();
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

    /**
     * 获取所有上架币种单价
     */
    public Map<String, String> getAllSymbolPrice(String currency) {

        DecimalFormat df = new DecimalFormat("#.##");

        Map<String, String> allPrice = new HashMap<String, String>();

        ExchangeRate ex = this.exchangeRateService.findBy(Constants.OUT_OR_IN_DEFAULT, currency);

        Map<String, String> allSymbol = this.getC2cSyspara("c2c_advert_symbol");
        if (null == allSymbol || 0 == allSymbol.size()) {
            return new HashMap<String, String>();
        }

        for (String symbol : allSymbol.keySet()) {
            symbol = symbol.toLowerCase();
            double symbol_close = 0d;

            if (symbol.equals("usdt")) {
                symbol_close = 1;
            } else {
                List<Realtime> list = this.dataService.realtime(symbol);
                if (0 == list.size()) {
                    continue;
                }
                Realtime realtime = list.get(0);
                symbol_close = realtime.getClose().doubleValue();
            }
            if (0 == symbol_close) {
                continue;
            }

            // 支付币种市价=支付币种汇率*上架币种实时行情价；
            double price = Arith.mul(ex.getRata().doubleValue(), symbol_close);
            allPrice.put(symbol, df.format(new Double(price)));
        }

        return allPrice;
    }

    /*
     * 获取 语种说明
     */
    public String getLanguageIntro() {

        Map<String, String> langMap = Constants.LANGUAGE;
        String retStr = "";
        for (String lang : langMap.keySet()) {
            String langName = langMap.get(lang);
            if (StringUtils.isEmptyString(retStr)) {
                retStr = lang + " " + langName;
            } else {
                retStr += "; " + lang + " " + langName;
            }
        }
        return retStr + ";";
    }

    /*
     * 获取 支付方式类型说明
     */
    public String getMethodTypeIntro() {

        Map<String, String> pmtMap = this.getC2cSyspara("c2c_payment_method_type");
        String retStr = "";
        for (String pmt : pmtMap.keySet()) {
            String pmtName = pmtMap.get(pmt);
            if (StringUtils.isEmptyString(retStr)) {
                retStr = pmt + " " + pmtName;
            } else {
                retStr += "; " + pmt + " " + pmtName;
            }
        }
        return retStr + ";";
    }

}
