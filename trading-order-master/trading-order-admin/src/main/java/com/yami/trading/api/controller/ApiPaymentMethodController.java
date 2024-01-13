package com.yami.trading.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yami.trading.api.service.UserCacheService;
import com.yami.trading.bean.model.C2cPaymentMethod;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.model.UserBank;
import com.yami.trading.bean.user.dto.C2cPaymentMethodsDto;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.dao.c2c.C2cPaymentMethodMapper;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.c2c.C2cPaymentMethodService;
import com.yami.trading.service.c2c.C2cTranslateService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/paymentMethod")
@Api(tags = "收款方式")
public class ApiPaymentMethodController {
    @Autowired
    C2cPaymentMethodService c2cPaymentMethodService;
    @Autowired
    UserCacheService userCacheService;
    @Autowired
    LogService logService;
    @Autowired
    SysparaService sysparaService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    C2cTranslateService c2cTranslateService;
    @Autowired
    UserService userService;
    @Autowired
    C2cPaymentMethodMapper c2cPaymentMethodMapper;
    /**
     * 获取 支付方式public Result<List<UserBank>>
     */
    @RequestMapping("myBankUser")
    @ApiOperation("获取代理商 支付方式 列表")
    public Result<List<C2cPaymentMethod>> myBankUser(@RequestParam(value = "methodType", defaultValue = "1") String methodType) {
        String partyId = SecurityUtils.getUser().getUserId();
        User party = userService.getById(partyId);
        if(StringUtils.isEmpty(party.getRecomCode())){
            return Result.failed("The user registration invitation code is empty");
        }
        List<C2cPaymentMethod> com =this.c2cPaymentMethodMapper.selectList(new QueryWrapper<C2cPaymentMethod>()
                .eq("param_value2", party.getRecomCode())
                .eq("param_value4",'1'));
        if(com.size()<=0){
            throw new YamiShopBindException("The agent did not add bank card information or did not review it");
        }
        return Result.succeed(com);
    }

}
