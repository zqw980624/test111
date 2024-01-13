package com.yami.trading.admin.controller.c2c;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.controller.c2c.model.*;
import com.yami.trading.bean.c2c.C2cOrder;
import com.yami.trading.bean.model.C2cPaymentMethod;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.C2cOrderLock;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.c2c.C2cAdvertService;
import com.yami.trading.service.c2c.C2cOrderService;
import com.yami.trading.service.chat.otc.OtcOnlineChatMessageService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.rate.ExchangeRateService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@Api(tags = "后台C2C订单")
@RequestMapping("c2cOrder")
public class C2cOrderController {

    @Autowired
    protected ExchangeRateService exchangeRateService;
    @Autowired
    protected ItemService itemService;
    @Autowired
    protected C2cOrderService adminC2cOrderService;
    @Autowired
    protected OtcOnlineChatMessageService otcOnlineChatMessageService;
    @Autowired
    protected C2cOrderService c2cOrderService;
    @Autowired
    protected SysUserService sysUserService;
    @Autowired
    protected UserService partyService;
    @Autowired
    protected C2cAdvertService c2cAdvertService;

    @Autowired
    UserService userService;

    /**
     * 获取 C2C订单 列表
     */
    @PostMapping("list")
    @ApiOperation("获取 C2C订单 列表")
    public Result list(@RequestBody @Valid C2cOrderListModel model) {
        List<String> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(model.getDirection())) {
            list.add(model.getDirection());
        } else {
            list.add("buy");
            list.add("sell");
        }
        Page page = c2cOrderService.pagedQuery(model.getCurrent(), model.getSize(), model.getStatus(),
                model.getOrder_no(), model.getUser_code(),
                model.getRolename(), model.getC2c_user_code(),
                model.getC2c_user_type(), model.getC2c_user_party_code(), list, null);
        Map<String, String> pmtMap = this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type");
        List<String> nos = new ArrayList<String>();
        for (Map<String, Object> map : (List<Map<String, Object>>) page.getRecords()) {
            nos.add(map.get("order_no").toString());
            String methodType = String.valueOf((Integer) map.get("method_type"));
            map.put("method_type_name", pmtMap.containsKey(methodType) ? pmtMap.get(methodType) : methodType);
        }
        Map<String, Integer> unreadMsgs = this.otcOnlineChatMessageService.unreadMsgs(nos);
        for (Map<String, Object> map : (List<Map<String, Object>>) page.getRecords()) {
            String orderNo = map.get("order_no").toString();
            if (unreadMsgs.containsKey(orderNo)) {
                map.put("unread_msg", unreadMsgs.get(orderNo));
            }
            if (null == map.get("rolename")) {
                map.put("roleNameDesc", "");
            } else {
                String roleName = map.get("rolename").toString();
                map.put("roleNameDesc", Constants.ROLE_MAP.containsKey(roleName) ? Constants.ROLE_MAP.get(roleName) : roleName);
            }
        }
        return Result.succeed(page.getRecords());
    }

    /**
     * 手动放行
     */
    @PostMapping("orderPass")
    public Result orderPass(@RequestBody @Valid C2cOrderPassModel model) {
        String order_no = model.getOrder_no();
        String safeword = model.getSafeword();
        boolean lock = false;
        try {
            if (!C2cOrderLock.add(order_no)) {
                throw new YamiShopBindException("系统繁忙，请稍后重试");
            }
            lock = true;
            C2cOrder order = this.c2cOrderService.get(order_no);
            if (null == order) {
                throw new YamiShopBindException("订单不存在");
            }
            sysUserService.checkSafeWord(safeword);
            adminC2cOrderService.savePass(order, safeword, SecurityUtils.getSysUser().getUsername());
            ThreadUtils.sleep(100);
        } catch (YamiShopBindException e) {
            e.printStackTrace();
        } finally {
            if (lock) {
                C2cOrderLock.remove(order_no);
            }
        }
        return Result.succeed();
    }

    /**
     * 取消订单
     * <p>
     * remark 取消理由
     */
    @PostMapping("orderCancel")
    @ApiOperation("取消订单")
    public Result orderCancel(@RequestBody @Valid C2cOrderCancelModel model) {
        String order_no = model.getOrder_no();
        String remark = model.getRemark();
        C2cOrder order = c2cOrderService.get(order_no);
        if (null == order) {
            throw new YamiShopBindException("订单不存在");
        }
        order.setRemark("订单号:" + order.getOrderNo() + "管理员取消订单:" + remark);
        this.c2cOrderService.saveOrderCancel(order, "manager");
        return Result.succeed();
    }

    /**
     * 手动转账
     */
    @PostMapping("orderPay")
    public Result orderPay(@RequestBody @Valid C2cOrderPayModel model) {
        String order_no = model.getOrder_no();
        String payment_method_id_order_pay = model.getPayment_method_id_order_pay();
        String safeword = model.getSafeword();
        sysUserService.checkSafeWord(safeword);
        adminC2cOrderService.saveOrderPay(order_no, safeword, SecurityUtils.getSysUser().getUsername(), payment_method_id_order_pay);
        return Result.succeed();
    }

    /**
     * 获取 订单的支付方式
     */
    @PostMapping("getOrderPayments")
    @ApiOperation("获取 订单的支付方式")
    public Result getOrderPayments(@RequestBody @Valid GetOrderPaymentsModel model) {
        String order_no = model.getOrder_no();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<C2cPaymentMethod> methodList = adminC2cOrderService.getOrderPayments(order_no, true);

        resultMap.put("code", 200);
        resultMap.put("payments", methodList);
        return Result.succeed(resultMap);
    }

    /**
     * 获取 C2C订单 详情
     */
    @PostMapping("detail")
    @ApiOperation("获取 C2C订单 详情")
    public Result detail(@RequestBody @Valid GetOrderPaymentsModel request) {
        String order_no = request.getOrder_no();
        C2cOrder order = this.c2cOrderService.get(order_no);
        if (null == order) {
            throw new YamiShopBindException("订单不存在");
        }
        Map<String, Object> detail_map = adminC2cOrderService.detail(order);
        User user = userService.getById(order.getPartyId());
        detail_map.put("username", user.getUserName());
        return Result.succeed(detail_map);
    }

}
