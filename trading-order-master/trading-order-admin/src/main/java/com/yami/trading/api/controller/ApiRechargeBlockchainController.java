package com.yami.trading.api.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.model.RechargeBlockchainOrder;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.RechargeBlockchainOrderService;
import com.yami.trading.service.SessionTokenService;
import com.yami.trading.service.c2c.C2cOrderService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.service.user.WalletLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 充值
 */
@RestController
@RequestMapping("api/rechargeBlockchain")
@Api(tags = "充值")
public class ApiRechargeBlockchainController {
    private static Logger logger = LoggerFactory.getLogger(ApiRechargeBlockchainController.class);
    @Autowired
    private RechargeBlockchainOrderService rechargeBlockchainOrderService;
    @Autowired
    private SessionTokenService sessionTokenService;
    @Autowired
    private SysparaService sysparaService;
    @Autowired
    protected UserService userService;
    @Autowired
    protected WalletLogService walletLogService;
    @Autowired
    C2cOrderService c2cOrderService;

    /**
     * 首次进入页面，传递session_token
     */
    @GetMapping("rechargeOpen")
    @ApiOperation("首次进入页面，传递session_token")
    public Result recharge_open() {
        String partyId = SecurityUtils.getUser().getUserId();
        String session_token = this.sessionTokenService.savePut(partyId);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("session_token", session_token);
        return Result.succeed(data);
    }

    /**
     * 充值申请
     * <p>
     * from 客户自己的区块链地址
     * blockchain_name 充值链名称
     * amount 充值数量
     * img 已充值的上传图片
     * coin 充值币种
     * channel_address 通道充值地址
     * tx 转账hash
     */
    @RequestMapping("recharge")
    @ApiOperation("充值申请")
    public Result recharge(String session_token, String amount, String from, String blockchain_name, String img,
                           String coin, String channel_address, String tx) {
        String error = this.verif(amount, coin, blockchain_name, channel_address);
        if (!StringUtils.isNullOrEmpty(error)) {
            throw new YamiShopBindException(error);
        }
        // 区块链充值方式选择 0:联系客服充值，1：可以线上充值
        if (1 == this.sysparaService.find("can_recharge").getInteger()) {
            // 币充值是否强制需要上传图片，需要true,不需要false
            boolean recharge_must_need_qr = this.sysparaService.find("recharge_must_need_qr").getBoolean();
            if (recharge_must_need_qr) {
                if (StringUtils.isEmptyString(img)) {
                    throw new YamiShopBindException("请上传图片");
                }
            }
        }
        double amount_double = Double.valueOf(amount).doubleValue();
        Object object = this.sessionTokenService.cacheGet(session_token);
        this.sessionTokenService.del(session_token);
        if (null == object || !SecurityUtils.getUser().getUserId().equals((String) object)) {
            throw new YamiShopBindException("请稍后再试");
        }
        User party = userService.getById(SecurityUtils.getUser().getUserId());
        if (Constants.SECURITY_ROLE_TEST.equals(party.getRoleName())) {
            throw new YamiShopBindException("无权限");
        }
        // 充值申请中的订单是否只能唯一：1唯一，2不限制
        double recharge_only_one = Double.valueOf(sysparaService.find("recharge_only_one").getSvalue());
        // 用户未结束银行卡订单数量
        Long nofinishOrderCount = this.c2cOrderService.getNofinishOrderCount(SecurityUtils.getUser().getUserId().toString());
        if (null != nofinishOrderCount && 0 != nofinishOrderCount.longValue() && 1 == recharge_only_one) {
            throw new YamiShopBindException("提交失败，当前有未处理银行卡订单");
        }
        RechargeBlockchainOrder recharge = new RechargeBlockchainOrder();
        recharge.setAddress(from);
        recharge.setBlockchainName(blockchain_name);
        recharge.setVolume(amount_double);
        recharge.setImg(img);
        recharge.setSymbol(coin.toLowerCase());
        recharge.setPartyId(SecurityUtils.getUser().getUserId());
        recharge.setSucceeded(0);
        recharge.setChannelAddress(channel_address);
        recharge.setTx(StringUtils.isEmptyString(tx) ? "" : tx);
        rechargeBlockchainOrderService.saveOrder(recharge);
        return Result.succeed(null);
    }

    /**
     * 充值订单详情
     * <p>
     * order_no 订单号
     */
    @GetMapping("get")
    @ApiOperation("充值订单详情")
    public Result get(String order_no) throws IOException {
        Map<String, Object> map = new HashMap<String, Object>();
        RechargeBlockchainOrder order = rechargeBlockchainOrderService.findByOrderNo(order_no);
        map.put("order_no", order.getOrderNo());
        map.put("volume", order.getVolume());
        map.put("amount", order.getVolume());
        map.put("create_time", DateUtils.format(order.getCreated(), DateUtils.DF_yyyyMMddHHmmss));
        map.put("from", order.getAddress());
        map.put("coin", order.getSymbol().toUpperCase());
        if (StringUtils.isNotEmpty(order.getBlockchainName())){
            map.put("coin_blockchain",
                    order.getSymbol().toUpperCase().indexOf("BTC") != -1
                            || order.getSymbol().toUpperCase().indexOf("ETH") != -1 ? order.getSymbol().toUpperCase()
                            : "USDT_" + order.getBlockchainName().toUpperCase());
        }

        map.put("fee", 0);
        map.put("state", order.getSucceeded());
        map.put("tx", order.getTx());
        map.put("failure_msg", order.getDescription());
        return Result.succeed(map);
    }

    /**
     * 充值记录
     */
    @GetMapping("list")
    @ApiOperation("充值记录")
    public Result list(String page_no) throws IOException {
        if (StringUtils.isNullOrEmpty(page_no)) {
            page_no = "1";
        }
        if (!StringUtils.isInteger(page_no)) {
            throw new YamiShopBindException("页码不是整数");
        }
        if (Integer.valueOf(page_no).intValue() <= 0) {
            throw new YamiShopBindException("页码不能小于等于0");
        }
        int page_no_int = Integer.valueOf(page_no).intValue();
        Page<Map> page = new Page<>(page_no_int, 10);
        List<Map> data = walletLogService.pagedQueryRecharge(SecurityUtils.getUser().getUserId(), "1", page).getRecords();
        for (Map<String, Object> log : data) {
            System.out.printf(JSONUtil.toJsonStr(log));
            if (null == log.get("coin") || !StringUtils.isNotEmpty(log.get("coin").toString()))
                log.put("coin", Constants.WALLET);
            else {
                log.put("coin", log.get("coin").toString().toUpperCase());
            }
            if (log.containsKey("symbol")){
                log.put("coin_blockchain", log.get("symbol").toString().toUpperCase().indexOf("BTC") != -1
                        || log.get("symbol").toString().toUpperCase().indexOf("ETH") != -1 ? log.get("symbol").toString().toUpperCase()
                        : "USDT_" + log.get("blockchain_name").toString().toUpperCase());
            }

            log.put("fee", 0);
            if(log.containsKey("address")){
                log.put("from", log.get("address").toString());
            }
        }
        return Result.succeed(data);
    }

    private String verif(String amount, String coin, String blockchain_name, String channel_address) {
        if (StringUtils.isNullOrEmpty(amount)) {
            return "充值数量必填";
        }
        if (!StringUtils.isDouble(amount)) {
            return "充值数量输入错误，请输入浮点数";
        }
        if (Double.valueOf(amount).doubleValue() <= 0) {
            return "充值数量不能小于等于0";
        }
        if (StringUtils.isEmptyString(coin)) {
            return "请输入充值币种";
        }
        if (StringUtils.isEmptyString(blockchain_name)) {
            return "Parameter Error";
        }
        if (StringUtils.isEmptyString(channel_address)) {
            return "请输入地址";
        }
        return null;
    }


}
