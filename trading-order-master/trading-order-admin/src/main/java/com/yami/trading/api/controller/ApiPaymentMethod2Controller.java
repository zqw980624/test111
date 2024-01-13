package com.yami.trading.api.controller;

import com.yami.trading.api.service.UserCacheService;
import com.yami.trading.bean.c2c.C2cAdvert;
import com.yami.trading.bean.c2c.C2cOrder;
import com.yami.trading.bean.c2c.C2cUser;
import com.yami.trading.bean.model.*;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.constants.RedisKeys;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.BusinessException;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.IPHelper;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.AwsS3OSSFileService;
import com.yami.trading.service.c2c.*;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.system.LogService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping()
public class ApiPaymentMethod2Controller {
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
    C2cAdvertService c2cAdvertService;
    @Autowired
    C2cOrderService c2cOrderService;
    private final String action = "/api/c2cPaymentMethod!";
    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;
    @Autowired
    C2cUserService c2cUserService;

    /**
     * 获取 支付方式 列表
     */
    @RequestMapping(action + "list.action")
    public Result list(HttpServletRequest request) {
        String language = request.getParameter("language");
        List<C2cPaymentMethod> list = new ArrayList<C2cPaymentMethod>();
        Map<String, C2cPaymentMethod> map = this.c2cPaymentMethodService.getByPartyId(SecurityUtils.getCurrentUserId());
        if (null != map && 0 != map.size()) {
            for (C2cPaymentMethod method : map.values()) {
                if (null != method) {
                    if (StringUtils.isNotEmpty(method.getMethodImg())) {
                        String path = awsS3OSSFileService.getUrl(method.getMethodImg());
                        method.setMethodImg(path);
                    }
                    list.add(this.c2cTranslateService.translatePm(method, language));
                }
            }
        }
        return Result.succeed(list);
    }

    /**
     * 获取 支付方式类型 列表
     */
    @RequestMapping(action + "method_type.action")
    public Result method_type(HttpServletRequest request) {
        String language = request.getParameter("language");
        Map<String, String> data = c2cAdvertService.getC2cSyspara("c2c_payment_method_type");
        // 多语言
        for (String typeId : data.keySet()) {
            String name = data.get(typeId);
            if (null != name) {
                C2cTranslate trans = this.c2cTranslateService.get(name, language);
                if (null != trans) {
                    data.put(typeId, trans.getTranslate());
                }
            }
        }
        return Result.succeed(data);
    }

    /**
     * 删除支付方式
     */
    @RequestMapping(action + "delete.action")
    public Object delete(HttpServletRequest request) {
        String id = request.getParameter("id");
        List<C2cOrder> order = c2cOrderService.getByPayId(id);
        if (ObjectUtils.isNotEmpty(order)) {
            throw new YamiShopBindException("当前支付方式有未处理完成的订单!");
        }
        this.c2cPaymentMethodService.removeById(id);
        return Result.succeed();
    }

    /**
     * 获取 支付方式 详情
     */
    @RequestMapping(action + "get.action")
    public Result get(HttpServletRequest request) {
        String id = request.getParameter("id");
        String language = request.getParameter("language");
        C2cPaymentMethod method = this.c2cPaymentMethodService.getById(id);
        if (null == method) {
            throw new BusinessException("支付方式不存在");
        }
        if (StringUtils.isNotEmpty(method.getMethodImg())) {
            method.setMethodImg(awsS3OSSFileService.getUrl(method.getMethodImg()));
        }
        return Result.succeed(method);
    }

    /**
     * 新增 支付方式
     */
    @RequestMapping(action + "add.action")
    public Result add(HttpServletRequest request) {
        String method_config_id = request.getParameter("method_config_id");
        String real_name = request.getParameter("real_name");
        String param_value1 = request.getParameter("param_value1");
        String param_value2 = request.getParameter("param_value2");
        String param_value3 = request.getParameter("param_value3");
        String param_value4 = request.getParameter("param_value4");
        String remark = request.getParameter("remark");
        if (StringUtils.isNullOrEmpty(method_config_id)) {
            throw new BusinessException("支付方式模板不正确");
        }
        if (StringUtils.isNullOrEmpty(real_name)) {
            throw new BusinessException("真实姓名必填");
        }
        if (StringUtils.isNullOrEmpty(param_value1)) {
            throw new BusinessException("参数值1必填");
        }

        // C2C用户和承兑商添加支付方式最大数量
        Map<String, C2cPaymentMethod> methodMap = this.c2cPaymentMethodService.getByPartyId(SecurityUtils.getCurrentUserId());
        Object obj = this.sysparaService.find("c2c_payment_method_count_max");
        if (null != obj) {
            if (methodMap.size() >= Integer.valueOf(this.sysparaService.find("c2c_payment_method_count_max").getSvalue()).intValue()) {
                throw new BusinessException("支付方式数量已达上限");
            }
        }
        C2cPaymentMethod method = new C2cPaymentMethod();
        method.setPartyId(SecurityUtils.getCurrentUserId());
       // method.setMethodConfigId(method_config_id);
       // method.setMethodType(methodConfig.getMethodType());
       // method.setMethodName(methodConfig.getMethodName());
       // method.setMethodImg(methodConfig.getMethodImg());
        method.setRealName(real_name);
       // method.setParamName1(methodConfig.getParamName1());
        method.setParamValue1(param_value1);
       // method.setParamName2(methodConfig.getParamName2());
        method.setParamValue2(param_value2);
       // method.setParamName3(methodConfig.getParamName3());
        //method.setParamName4(methodConfig.getParamName4());
        method.setParamValue4(param_value4);
        method.setRemark(remark);
        method.setCreateTime(new Date());
        method.setUpdateTime(new Date());
        this.c2cPaymentMethodService.save(method);
        C2cPaymentMethod methodSaved = c2cPaymentMethodService.getC2cPaymentMethod(method.getUuid().toString());
        if (null != methodSaved) {
            redisTemplate.opsForValue().set(RedisKeys.C2C_PAYMENT_METHOD_ID + methodSaved.getUuid().toString(), methodSaved);
            Map<String, C2cPaymentMethod> map = (Map<String, C2cPaymentMethod>) redisTemplate.opsForValue().get(RedisKeys.C2C_PAYMENT_METHOD_PARTY_ID + methodSaved.getPartyId().toString());
            if (null == map) {
                map = new ConcurrentHashMap<String, C2cPaymentMethod>();
            }
            map.put(methodSaved.getUuid().toString(), methodSaved);
            redisTemplate.opsForValue().set(RedisKeys.C2C_PAYMENT_METHOD_PARTY_ID + methodSaved.getPartyId().toString(), map);
            Map<String, String> map1 = (Map<String, String>) redisTemplate.opsForValue().get(RedisKeys.C2C_PAYMENT_METHOD_ID_TYPE);
            if (null == map1) {
                map1 = new ConcurrentHashMap<String, String>();
            }
            map1.put(methodSaved.getUuid().toString(), String.valueOf(methodSaved.getMethodType()));
            redisTemplate.opsForValue().set(RedisKeys.C2C_PAYMENT_METHOD_ID_TYPE, map1);
        }
        String log = MessageFormat.format("ip:" + IPHelper.getIpAddr()
                        + ",用户新增支付方式,id:{0},用户PARTY_ID:{1},支付方式模板id:{2},支付方式类型:{3},支付方式名称:{4},支付方式图片:{5},真实姓名:{6},"
                        + "参数名1:{7},参数值1:{8},参数名2:{9},参数值2:{10},参数名3:{11},参数值3:{12},参数名4:{13},参数值4:{14},参数名5:{15},参数值5:{16},"
                        + "参数名6:{17},参数值6:{18},参数名7:{19},参数值7:{20},参数名8:{21},参数值8:{22},参数名9:{23},参数值9:{24},参数名10:{25},参数值10:{26},"
                        + "参数名11:{27},参数值11:{28},参数名12:{29},参数值12:{30},参数名13:{31},参数值13:{32},参数名14:{33},参数值14:{34},参数名15:{35},参数值15:{36},"
                        + "支付二维码:{37},备注:{38},创建时间:{39},更新时间:{40}",
                method.getUuid(), method.getPartyId(), method.getMethodType(), method.getMethodName(), method.getMethodImg(), method.getRealName(),
                method.getParamName1(), method.getParamValue1(), method.getParamName2(), method.getParamValue2(), method.getParamName3(), method.getParamName4(), method.getParamValue4()
                , method.getRemark(), method.getCreateTime(), method.getUpdateTime());
        User sec = userCacheService.currentUser();
        this.saveLog(sec, sec.getUserName(), log, Constants.LOG_CATEGORY_C2C);
        return Result.succeed();
    }

    /**
     * 修改 支付方式
     */
    @RequestMapping(action + "update.action")
    public Object update(HttpServletRequest request) {
        String id = request.getParameter("id");
        String real_name = request.getParameter("real_name");
        String param_value1 = request.getParameter("param_value1");
        String param_value2 = request.getParameter("param_value2");
        String param_value3 = request.getParameter("param_value3");
        String param_value4 = request.getParameter("param_value4");
        String param_value5 = request.getParameter("param_value5");
        String param_value6 = request.getParameter("param_value6");
        String param_value7 = request.getParameter("param_value7");
        String param_value8 = request.getParameter("param_value8");
        String param_value9 = request.getParameter("param_value9");
        String param_value10 = request.getParameter("param_value10");
        String param_value11 = request.getParameter("param_value11");
        String param_value12 = request.getParameter("param_value12");
        String param_value13 = request.getParameter("param_value13");
        String param_value14 = request.getParameter("param_value14");
        String param_value15 = request.getParameter("param_value15");
        String qrcode = request.getParameter("qrcode");
        String remark = request.getParameter("remark");
        if (StringUtils.isNullOrEmpty(real_name)) {
            throw new BusinessException("真实姓名必填");
        }
        if (StringUtils.isNullOrEmpty(param_value1)) {
            throw new BusinessException("参数值1必填");
        }
        C2cPaymentMethod method = this.c2cPaymentMethodService.getById(id);
        if (null == method) {
            throw new BusinessException("支付方式不存在");
        }
        String partyId = SecurityUtils.getCurrentUserId();
        if (!method.getPartyId().equals(partyId)) {
            throw new BusinessException("支付方式不匹配该用户");
        }
        String log = MessageFormat.format("ip:" + IPHelper.getIpAddr()
                        + ",用户修改支付方式,id:{0},原用户PARTY_ID:{1},原支付方式模板:{2},原支付方式类型:{3},原支付方式名称:{4},原支付方式图片:{5},原真实姓名:{6},"
                        + "原参数名1:{7},原参数值1:{8},原参数名2:{9},原参数值2:{10},原参数名3:{11},原参数值3:{12},原参数名4:{13},原参数值4:{14},原参数名5:{15},原参数值5:{16},"
                        + "原参数名6:{17},原参数值6:{18},原参数名7:{19},原参数值7:{20},原参数名8:{21},原参数值8:{22},原参数名9:{23},原参数值9:{24},原参数名10:{25},原参数值10:{26},"
                        + "原参数名11:{27},原参数值11:{28},原参数名12:{29},原参数值12:{30},原参数名13:{31},原参数值13:{32},原参数名14:{33},原参数值14:{34},原参数名15:{35},原参数值15:{36},"
                        + "原支付二维码:{37},原备注:{38},原创建时间:{39},原更新时间:{40}",
                method.getUuid(), method.getPartyId(), method.getMethodType(), method.getMethodName(), method.getMethodImg(), method.getRealName(),
                method.getParamName1(), method.getParamValue1(), method.getParamName2(), method.getParamValue2(), method.getParamName3(), method.getParamName4(), method.getParamValue4(),
                method.getRemark(), method.getCreateTime(), method.getUpdateTime());
        method.setRealName(real_name);
        method.setParamValue1(param_value1);
        method.setParamValue2(param_value2);
        method.setParamValue4(param_value4);
        method.setRemark(remark);
        method.setUpdateTime(new Date());
        boolean state = this.c2cPaymentMethodService.updateById(method);
        if (state) {
            redisTemplate.opsForValue().set(RedisKeys.C2C_PAYMENT_METHOD_ID + method.getUuid().toString(), method);
            Map<String, C2cPaymentMethod> map = (Map<String, C2cPaymentMethod>) redisTemplate.opsForValue().get(RedisKeys.C2C_PAYMENT_METHOD_PARTY_ID + method.getPartyId().toString());
            if (null == map) {
                map = new ConcurrentHashMap<String, C2cPaymentMethod>();
            } else {
                map.remove(method.getUuid().toString());
            }
            map.put(method.getUuid().toString(), method);
            redisTemplate.opsForValue().set(RedisKeys.C2C_PAYMENT_METHOD_PARTY_ID + method.getPartyId().toString(), map);
            Map<String, String> map1 = (Map<String, String>) redisTemplate.opsForValue().get(RedisKeys.C2C_PAYMENT_METHOD_ID_TYPE);
            if (null == map1) {
                map1 = new ConcurrentHashMap<String, String>();
            } else {
                map1.remove(method.getUuid().toString());
            }
            map1.put(method.getUuid().toString(), String.valueOf(method.getMethodType()));
            redisTemplate.opsForValue().set(RedisKeys.C2C_PAYMENT_METHOD_ID_TYPE, map1);
        }
        log += MessageFormat.format(",id:{0},新用户PARTY_ID:{1},新支付方式模板:{2},新支付方式类型:{3},新支付方式名称:{4},新支付方式图片:{5},新真实姓名:{6},"
                        + "新参数名1:{7},新参数值1:{8},新参数名2:{9},新参数值2:{10},新参数名3:{11},新参数值3:{12},新参数名4:{13},新参数值4:{14},新参数名5:{15},新参数值5:{16},"
                        + "新参数名6:{17},新参数值6:{18},新参数名7:{19},新参数值7:{20},新参数名8:{21},新参数值8:{22},新参数名9:{23},新参数值9:{24},新参数名10:{25},新参数值10:{26},"
                        + "新参数名11:{27},新参数值11:{28},新参数名12:{29},新参数值12:{30},新参数名13:{31},新参数值13:{32},新参数名14:{33},新参数值14:{34},新参数名15:{35},新参数值15:{36},"
                        + "新支付二维码:{37},新备注:{38},新创建时间:{39},新更新时间:{40}",
                method.getUuid(), method.getPartyId(), method.getMethodType(), method.getMethodName(), method.getMethodImg(), method.getRealName(),
                method.getParamName1(), method.getParamValue1(), method.getParamName2(), method.getParamValue2(), method.getParamName3(), method.getParamName4(), method.getParamValue4(),
               method.getRemark(), method.getCreateTime(), method.getUpdateTime());
        User sec = userCacheService.currentUser();
        this.saveLog(sec, sec.getUserName(), log, Constants.LOG_CATEGORY_C2C);
        return Result.succeed();
    }

    public void saveLog(User secUser, String operator, String context, String category) {
        Log log = new Log();
        log.setCategory(category);
        log.setOperator(operator);
        log.setUsername(secUser.getUserName());
        log.setUserId(secUser.getUserId());
        log.setLog(context);
        log.setCreateTime(new Date());
        this.logService.save(log);
    }

    /**
     * 获取 承兑商广告 支付方式列表
     * <p>
     * 广告id
     */
    @RequestMapping(action + "getAdPayments.action")
    public Object getAdPayments(HttpServletRequest request) {
        String id = request.getParameter("id");
        String language = request.getParameter("language");
        if (StringUtils.isEmptyString(id)) {
            throw new YamiShopBindException("广告id不正确");
        }
        C2cAdvert c2cAdvert = c2cAdvertService.getById(id);
        if (null == c2cAdvert) {
            throw new YamiShopBindException("广告不存在");
        }
        C2cUser c2cUser = this.c2cUserService.getById(c2cAdvert.getC2cUserId());
        if (null == c2cUser) {
            throw new YamiShopBindException("承兑商不存在");
        }
        List<C2cPaymentMethod> list = new ArrayList<C2cPaymentMethod>();
        Map<String, C2cPaymentMethod> map = this.c2cPaymentMethodService.getByPartyId(c2cUser.getC2cUserPartyId());
        if (null != map && !map.isEmpty()) {
            for (C2cPaymentMethod method : map.values()) {
                if (null != method) {
                    list.add(this.c2cTranslateService.translatePm(method, language));
                }
            }
        }
        if (null == list || 0 == list.size()) {
            throw new YamiShopBindException("承兑商支付方式未配置");
        }
        String[] payTypes = c2cAdvert.getPayType().split(",");
        List<C2cPaymentMethod> resList = new ArrayList<C2cPaymentMethod>();
        for (int i = 0; i < list.size(); i++) {
            C2cPaymentMethod method = list.get(i);
            for (String type : payTypes) {
               // if (method.getMethodConfigId().equals(type)) {
                    resList.add(method);
                    break;
               // }
            }
        }
        if (null == resList || 0 == resList.size()) {
            throw new YamiShopBindException("承兑商广告支付方式未配置");
        }
        for (int i = 0; i < resList.size(); i++) {
            C2cPaymentMethod method = resList.get(i);
            if (null != method) {
                if (StringUtils.isNotEmpty(method.getMethodImg())) {
                    String path = awsS3OSSFileService.getUrl(method.getMethodImg());
                    method.setMethodImg(path);
                }
            }
        }
        return Result.succeed(resList);
    }
}
