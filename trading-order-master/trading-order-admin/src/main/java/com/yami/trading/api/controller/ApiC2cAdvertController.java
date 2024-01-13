package com.yami.trading.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.c2c.C2cAdvert;
import com.yami.trading.bean.c2c.C2cUser;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.model.C2cTranslate;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.rate.domain.ExchangeRate;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.AwsS3OSSFileService;
import com.yami.trading.service.c2c.C2cAdvertService;
import com.yami.trading.service.c2c.C2cTranslateService;
import com.yami.trading.service.c2c.C2cUserService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.rate.ExchangeRateService;
import com.yami.trading.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * C2C广告
 */
@RestController
@Slf4j
public class ApiC2cAdvertController {
    @Autowired
    private C2cAdvertService c2cAdvertService;
    @Autowired
    private C2cUserService c2cUserService;
    @Autowired
    private UserService userService;
    @Autowired
    private ExchangeRateService exchangeRateService;
    @Autowired
    private ItemService itemService;
    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;
    @Autowired
    private C2cTranslateService c2cTranslateService;
    private final String action = "/api/c2cAdvert!";

    /**
     * 获取 支付币种（法币） 列表
     */
    @RequestMapping(action + "currency.action")
    public Result currency(HttpServletRequest request) {
        Map<String, String> pmtMap = c2cAdvertService.getC2cSyspara("c2c_advert_currency");
        List<Map<String, Object>> data = new ArrayList<>();
        List<ExchangeRate> exchangeRateList =exchangeRateService.findBy(Constants.OUT_OR_IN_DEFAULT);
        for (ExchangeRate er : exchangeRateList) {
            if (pmtMap.keySet().contains(er.getCurrency())) {
                Map<String, Object> erMap = new HashMap<String, Object>();
                erMap.put("out_or_in", er.getOutOrIn());
                erMap.put("rate", er.getRata());
                erMap.put("currency", er.getCurrency());
                erMap.put("name", er.getName());
                erMap.put("currency_symbol", er.getCurrencySymbol());
                data.add(erMap);
            }
        }
        return Result.succeed(data);
    }
//
    /**
     * 获取 上架币种 列表
     */
    @RequestMapping(action + "symbol.action")
    public Result symbol(HttpServletRequest request) {
        Map<String, String> asMap =c2cAdvertService.getC2cSyspara("c2c_advert_symbol");
        Map<String, String> data = new HashMap<>();
        List<Item> itemList = itemService.cacheGetAll();
        data.put("usdt", "USDT");
        for (Item item : itemList) {
            if (asMap.keySet().contains(item.getSymbol().toUpperCase())) {
                data.put(item.getSymbol(), item.getSymbol().toUpperCase());
            }
        }
        return Result.succeed(data);
    }


}
