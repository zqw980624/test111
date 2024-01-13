package com.yami.trading.api.controller;

import com.yami.trading.bean.c2c.C2cAdvert;
import com.yami.trading.bean.c2c.C2cOrder;
import com.yami.trading.bean.model.C2cPaymentMethod;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.constants.RedisKeys;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.BusinessException;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.*;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.SessionTokenService;
import com.yami.trading.service.c2c.C2cAdvertService;
import com.yami.trading.service.c2c.C2cOrderService;
import com.yami.trading.service.c2c.C2cPaymentMethodService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class ApiC2cOrderController {

    @Autowired
    private C2cOrderService c2cOrderService;
    private final String action = "/api/c2cOrder!";

    @Autowired
    SessionTokenService sessionTokenService;

    @Autowired
    C2cAdvertService c2cAdvertService;
    @Autowired
    SysparaService sysparaService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserService userService;

    @Autowired
    C2cPaymentMethodService c2cPaymentMethodService;

    /**
     * 首次进入下单页面，传递session_token
     */
    @RequestMapping(action + "order_open.action")
    public Object order_open(HttpServletRequest request) throws IOException {
        String currency = request.getParameter("currency");
        if (StringUtils.isEmptyString(currency)) {
            throw new YamiShopBindException("支付币种不正确");
        }
        String partyId = SecurityUtils.getCurrentUserId();
        String session_token = sessionTokenService.savePut(partyId);
        Map<String, String> allPrice = c2cAdvertService.getAllSymbolPrice(currency);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("session_token", session_token);
        data.put("all_price", allPrice);
        return Result.succeed(data);
    }


    /**
     * 自选区下单：购买、出售
     *
     * safe_password 资金密码
     * c2c_advert_id 广告id
     * payment_method_id 支付方式ID：购买为承兑商收款方式ID，出售为用户收款方式ID
     * order_type 订单类型：by_amount按支付金额/by_num按币种数量
     * amount 支付金额
     * coin_amount 币种数量
     * remark 备注
     */
    @RequestMapping(action + "open.action")
    public Object open(HttpServletRequest request) {
        String session_token = request.getParameter("session_token");
//		String safe_password = request.getParameter("safe_password");
        String c2c_advert_id = request.getParameter("c2c_advert_id");
        String payment_method_id = request.getParameter("payment_method_id");
        String order_type = request.getParameter("order_type");
        String direction = request.getParameter("direction");
        String amount = request.getParameter("amount");
        String coin_amount = request.getParameter("coin_amount");
        String remark = request.getParameter("remark");



        String partyId =SecurityUtils.getCurrentUserId();

        Result resultObject=new Result();
        boolean lock = false;
        User   party=null;
        String orderNo = DateUtil.getToday("yyMMddHHmmss") + RandomUtil.getRandomNum(8);
        try {

               party = userService.getById(partyId);
            log.error("用户"+direction+"开始当前用户uid:"+party.getUserCode()+"当前用户名:"+party.getUserName()+"生成的订单号:"+orderNo);
            if (Constants.SECURITY_ROLE_TEST.equals(party.getRoleName())) {
                throw new YamiShopBindException("无权限");
            }

            if (!C2cLock.add(partyId)) {
                throw new YamiShopBindException("Please try again later");
            }
            lock = true;

            Object object = this.sessionTokenService.cacheGet(session_token);
            this.sessionTokenService.del(session_token);
            if (null == object || !partyId.equals((String) object)) {
                throw new YamiShopBindException("请稍后再试");
            }

            if (!party.isEnabled()) {

                return Result.succeed("用户已锁定");
            }

            // C2C用户未结束订单最大数量
            Long nofinishOrderCount = c2cOrderService.getNofinishOrderCount(partyId);
            String c2c_sell_only_one = sysparaService.find("c2c_sell_only_one").getSvalue();
            if(StringUtils.isNotEmpty(c2c_sell_only_one)&&"1".equals(c2c_sell_only_one)) {
                if(nofinishOrderCount >= 1) {
                    throw new YamiShopBindException("提交失败，当前有未处理订单");
                }
                Long.valueOf(c2c_sell_only_one).longValue();
            }
            Object obj2 = this.sysparaService.find("c2c_nofinish_order_count_max");
            if (null != obj2) {
                if (nofinishOrderCount >= Long.valueOf(this.sysparaService.find("c2c_nofinish_order_count_max").getSvalue()).longValue()) {
                    throw new YamiShopBindException("用户未结束订单数量已达上限");
                }
            }

            // C2C用户下单是否需要基础认证（true:是，false:否）
            Object obj = this.sysparaService.find("c2c_order_need_kyc");
            if (null != obj) {
                if (!party.isRealNameAuthority() && "true".equals(this.sysparaService.find("c2c_order_need_kyc").getSvalue())) {
                    return Result.succeed("未实名认证，是否认证？");
                }
            }

            // C2C每日订单取消最大次数
            int orderCancelDayTimes = 0;
            Map<String, Integer> map = (Map<String, Integer>)redisTemplate.opsForValue() .get(RedisKeys.C2C_ORDER_CANCEL_DAY_TIMES);
            if (null != map && null != map.get(partyId)) {
                orderCancelDayTimes = map.get(partyId);
            }
            Object obj1 = this.sysparaService.find("c2c_order_cancel_day_times");
            if (null != obj1) {
                if (orderCancelDayTimes >= Integer.valueOf(this.sysparaService.find("c2c_order_cancel_day_times").getSvalue()).intValue()) {
                    throw new YamiShopBindException("今日取消订单次数太多了，请明日再试");
                }
            }

            C2cAdvert c2cAdvert = this.c2cAdvertService.getById(c2c_advert_id);
            if (null == c2cAdvert) {
                throw new YamiShopBindException("广告不存在");
            }

            C2cPaymentMethod method =c2cPaymentMethodService.get(payment_method_id);
            if (null == method) {
                throw new YamiShopBindException("支付方式不存在");
            }

            if (StringUtils.isEmptyString(order_type) || !Arrays.asList("by_amount", "by_num").contains(order_type)) {
                throw new YamiShopBindException("订单类型不正确");
            }

            if (C2cOrder.ORDER_TYPE_BY_AMOUNT.equals(order_type)) {
                // 按支付金额支付
                if (StringUtils.isEmptyString(amount) || !StringUtils.isDouble(amount) || Double.valueOf(amount).doubleValue() <= 0) {
                    throw new YamiShopBindException("支付金额不正确");
                }
                coin_amount = "0";
            } else {
                // 按币种数量支付
                if (StringUtils.isEmptyString(coin_amount) || !StringUtils.isDouble(coin_amount) || Double.valueOf(coin_amount).doubleValue() <= 0) {
                    throw new YamiShopBindException("币种数量不正确");
                }
                amount = "0";
            }

            Map<String, Object> data = new HashMap<String, Object>();

            C2cOrder c2cOrder = new C2cOrder();
            c2cOrder.setPartyId(partyId);
            c2cOrder.setC2cUserId(c2cAdvert.getC2cUserId());
            c2cOrder.setC2cAdvertId(c2c_advert_id);
            c2cOrder.setPaymentMethodId(payment_method_id);
            c2cOrder.setOrderType(order_type);
            c2cOrder.setOrderNo(orderNo);
            // 0未付款
            c2cOrder.setState("0");
            c2cOrder.setAmount(StringUtils.isEmptyString(amount) ? 0 : Double.valueOf(amount).doubleValue());
            c2cOrder.setCoinAmount(StringUtils.isEmptyString(coin_amount) ? 0 : Double.valueOf(coin_amount).doubleValue());
            c2cOrder.setRemark(remark);
            String remarks = "name:"+party.getUserName()+"code:"+party.getUserCode()+"direction:"+direction+"orderNo:"+orderNo;
            this.c2cOrderService.saveOpen(c2cOrder,remarks);
            data.put("order_no", c2cOrder.getOrderNo());
            resultObject.setData(data);
        } catch (BusinessException e) {
            log.error("用户"+direction+"执行异常1当前用户uid:"+party.getUserCode()+"当前用户名:"+party.getUserName()+"生成的订单号:"+orderNo);
            resultObject.setCode(1);
            resultObject.setMsg(e.getMessage());
            log.error("error:"+e.getMessage());
        } catch (Throwable t) {
            log.error("用户"+direction+"执行异常2当前用户uid:"+party.getUserCode()+"当前用户名:"+party.getUserName()+"生成的订单号:"+orderNo);
            resultObject.setCode(1);
            resultObject.setMsg("程序错误");
            log.error("error:", t);
        }  finally {
            if (lock) {
                C2cLock.remove(partyId);
            }
        }

        return resultObject;
    }

}
