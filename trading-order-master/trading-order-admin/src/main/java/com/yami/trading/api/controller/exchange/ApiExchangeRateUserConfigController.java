package com.yami.trading.api.controller.exchange;

import com.mysql.cj.util.StringUtils;
import com.yami.trading.bean.rate.domain.ExchangeRate;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.rate.UserRateConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping()
@Api(tags = "API 计价方式")
public class ApiExchangeRateUserConfigController {
    private final String action = "api/exchangerateuserconfig!";
    @Autowired
    private UserRateConfigService userRateConfigService;

    /**
     * 获取 汇率
     */
    @GetMapping(action + "get.action")
    @ApiOperation("获取 汇率")
    public Result get() throws IOException {
        String partyId = SecurityUtils.getCurrentUserId();
        Map<String, Object> data = new HashMap<String, Object>();
        ExchangeRate exchangeRate = this.userRateConfigService.findUserConfig(partyId);
        data.put("currency", exchangeRate.getCurrency());
        data.put("name", exchangeRate.getName());
        data.put("currency_symbol", exchangeRate.getCurrencySymbol());
        data.put("rate", exchangeRate.getRata());
        return Result.succeed(data);

       /* data.put("currency", "INR");
        data.put("name", "印度");
        data.put("currency_symbol", "₽");
        data.put("rate", "11");*/
    }

    @GetMapping(action + "userSetRate.action")
    @ApiOperation("设置计价方式")
    public Object userSetRate(@RequestParam String rateId) throws IOException {
        String partyId = SecurityUtils.getUser().getUserId();
        if (StringUtils.isNullOrEmpty(rateId)) {
            throw new YamiShopBindException("rateId is null");
        }
        this.userRateConfigService.update(rateId, partyId);
        return Result.succeed();
    }
}
