package com.yami.trading.admin.controller.c2c;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.controller.c2c.model.BankCardOrderModel;
import com.yami.trading.admin.controller.c2c.model.C2cOrderPassModel;
import com.yami.trading.admin.controller.c2c.model.C2cOrderPayModel;
import com.yami.trading.admin.controller.c2c.model.GetOrderPaymentsModel;
import com.yami.trading.admin.model.OrderCancelModel;
import com.yami.trading.bean.c2c.C2cOrder;
import com.yami.trading.bean.model.C2cPaymentMethod;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.C2cOrderLock;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.security.common.manager.PasswordManager;
import com.yami.trading.service.AwsS3OSSFileService;
import com.yami.trading.service.c2c.C2cAdvertService;
import com.yami.trading.service.c2c.C2cOrderService;
import com.yami.trading.service.chat.otc.OtcOnlineChatMessageService;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("bankCardOrder")
@Api(tags = "银行卡订单")
public class BankCardController {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private PasswordManager passwordManager;
    @Autowired
    C2cOrderService c2cOrderService;
    @Autowired
    SysUserService sysUserService;
    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;
    @Autowired
    C2cAdvertService c2cAdvertService;
    @Autowired
    protected C2cOrderService adminC2cOrderService;
    @Autowired
    OtcOnlineChatMessageService otcOnlineChatMessageService;

    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<?> list(@RequestBody @Valid BankCardOrderModel model) {
        Page page = new Page(model.getCurrent(), model.getSize());
        List<String> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(model.getDirection())) {
            list.add(model.getDirection());
        } else {
            list.add("recharge");
            list.add("withdraw");
        }
        c2cOrderService.pagedBankCardOrderQuery(page, list
                , model.getStatus(), model.getUser_code(),
                model.getRolename(), model.getOrder_no());
        Map<String, String> pmtMap = this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type");
        List<String> nos = new ArrayList<String>();
        for (Map<String, Object> map : (List<Map<String, Object>>) page.getRecords()) {
            nos.add(map.get("order_no").toString());
            String methodType = String.valueOf((Integer) map.get("method_type"));
            if (map.containsKey("img")) {
                if (map.get("img") != null) {
                    map.put("img", map.get("img").toString());
                }
            }
            if (map.containsKey("method_img")) {
                if (map.get("method_img") != null) {
                    map.put("method_img", map.get("method_img").toString());
                }
            }
            map.put("method_type_name", pmtMap.containsKey(methodType) ? pmtMap.get(methodType) : methodType);
            map.put("paramValue15",1);
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
        return Result.ok(page);
    }

    /**
     * 手动放行
     */
    @PostMapping( "orderPass")
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
            adminC2cOrderService.savePass(order, safeword,SecurityUtils.getSysUser().getUsername());
            ThreadUtils.sleep(100);

        } catch (YamiShopBindException e) {
            e.printStackTrace();
        }finally {
            if (lock) {
                C2cOrderLock.remove(order_no);
            }
        }
        return Result.succeed();
    }

    @ApiOperation(value = "取消订单")
    @PostMapping("orderCancel")
    public Result<?> orderCancel(@RequestBody @Valid OrderCancelModel model) {
        C2cOrder order = this.c2cOrderService.get(model.getOrderNo());
        if (null == order) {
            throw new YamiShopBindException("订单不存在");
        }
        order.setRemark(model.getReason());
        this.c2cOrderService.saveOrderCancel(order, "manager");
        return Result.ok(null);
    }

    /**
     * 手动转账
     */
    @PostMapping("orderPayPwd")
    @ApiOperation("手动转账")
    public Result orderPayPwd(@RequestBody @Valid C2cOrderPayModel model) {
        boolean lock = false;
        try {
            if (!C2cOrderLock.add(model.getOrder_no())) {
                throw new YamiShopBindException("系统繁忙，请稍后重试");
            }
            lock = true;
            sysUserService.checkSafeWord(model.getSafeword());
            c2cOrderService.saveOrderPayPd(model.getOrder_no(), model.getSafeword(), SecurityUtils.getSysUser().getUsername(), model.getPayment_method_id_order_pay());
        } catch (Exception e) {
            return Result.failed(e.getMessage());
        } finally {
            if (lock) {
                C2cOrderLock.remove(model.getOrder_no());
            }
        }
        return Result.succeed();
    }

    /**
     * 手动转账
     */
    @PostMapping("orderPay")
    @ApiOperation("手动转账")
    public Result orderPay(@RequestBody @Valid C2cOrderPayModel model) {
        boolean lock = false;
        try {
            if (!C2cOrderLock.add(model.getOrder_no())) {
                throw new YamiShopBindException("系统繁忙，请稍后重试");
            }
            lock = true;
            sysUserService.checkSafeWord(model.getSafeword());
            c2cOrderService.saveOrderPay(model.getOrder_no(), model.getSafeword(), SecurityUtils.getSysUser().getUsername(), model.getPayment_method_id_order_pay());
        } catch (Exception e) {
            return Result.failed(e.getMessage());
        } finally {
            if (lock) {
                C2cOrderLock.remove(model.getOrder_no());
            }
        }
        return Result.succeed();
    }

    /**
     * 获取 银行卡订单的支付方式
     */
    @GetMapping("getOrderPayments")
    @ApiOperation("获取 银行卡订单的支付方式")
    public Result getOrderPayments( @Valid GetOrderPaymentsModel request) {
        List<C2cPaymentMethod> methodList = c2cOrderService.getOrderPayments(request.getOrder_no(),false);
        return Result.succeed(methodList);
    }

}
