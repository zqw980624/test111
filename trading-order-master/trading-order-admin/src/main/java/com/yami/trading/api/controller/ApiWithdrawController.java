package com.yami.trading.api.controller;

import com.yami.trading.bean.model.Withdraw;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.Arith;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.SessionTokenService;
import com.yami.trading.service.WithdrawService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.service.user.WalletLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提现
 */
@RestController
@RequestMapping("api/withdraw")
@Api(tags = "提现")
@Slf4j
public class ApiWithdrawController {
    @Autowired
    private WithdrawService withdrawService;
    @Autowired
    private UserService userService;
    @Autowired
    private SessionTokenService sessionTokenService;
    @Autowired
    private SysparaService sysparaService;
    @Autowired
    protected WalletLogService walletLogService;

    /**
     * 首次进入页面，传递session_token
     */
    @GetMapping("withdrawOpen")
    @ApiOperation("首次进入页面，传递session_token")
    public Result withdrawOpen() {
        String partyId = SecurityUtils.getUser().getUserId();
        String session_token = this.sessionTokenService.savePut(partyId);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("session_token", session_token);
        return Result.succeed(data);
    }

    /**
     * 提现申请
     * <p>
     * safeword 资金密码
     * amount 提现金额
     * from 客户转出地址
     * currency 货币 CNY USD
     * channel 渠道 USDT,BTC,ETH
     */
    @ApiOperation("提现申请")
    @PostMapping("apply")
    public Result apply(String session_token, String safeword,
                                String amount, String from, String currency,
                                String channel){
        String partyId = SecurityUtils.getUser().getUserId();
        boolean lock = false;
        String error = this.verif(amount);
        if (!StringUtils.isNullOrEmpty(error)) {
            throw new YamiShopBindException(error);
        }
        double amount_double = Double.valueOf(amount).doubleValue();

        // 交易所提现是否需要资金密码
        String exchange_withdraw_need_safeword = this.sysparaService.find("exchange_withdraw_need_safeword").getSvalue();
        if(StringUtils.isEmptyString(exchange_withdraw_need_safeword)) {
            throw new YamiShopBindException("系统参数错误");
        }

        if ("true".equals(exchange_withdraw_need_safeword)) {

            if (StringUtils.isEmptyString(safeword)) {
                throw new YamiShopBindException("资金密码不能为空");
            }

            if (safeword.length() < 6 || safeword.length() > 12) {
                throw new YamiShopBindException("资金密码必须6-12位");
            }
            if (!userService.checkLoginSafeword(SecurityUtils.getUser().getUserId(),safeword)){
                throw new YamiShopBindException("资金密码错误");
            }
        }
        Object object = this.sessionTokenService.cacheGet(session_token);
        this.sessionTokenService.del(session_token);
//        if (null == object || !SecurityUtils.getUser().getUserId().equals((String) object)) {
//            throw new YamiShopBindException("请稍后再试");
//        }
        Withdraw withdraw = new Withdraw();
        withdraw.setUserId(partyId);
        withdraw.setVolume(new BigDecimal(amount_double));
        withdraw.setAddress(from);
        withdraw.setCurrency(currency);
        withdraw.setTx("");
        // 保存
        this.withdrawService.saveApply(withdraw, channel, null);
        return Result.succeed(null);
    }

    /**
     * 提现订单详情
     * <p>
     * order_no 订单号
     */
    @ApiOperation("提现订单详情")
    @GetMapping("get")
    public Result get(@RequestParam String order_no) throws IOException {
        Withdraw withdraw = this.withdrawService.findByOrderNo(order_no);
        if (withdraw==null){
            throw  new YamiShopBindException("订单不存在!");
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("order_no", withdraw.getOrderNo());
        map.put("volume", withdraw.getVolume());
        map.put("amount", withdraw.getAmount());
        map.put("create_time", DateUtils.format(withdraw.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
        map.put("to", withdraw.getAddress());
        map.put("fee", withdraw.getAmountFee().doubleValue());
        map.put("coin_blockchain", withdraw.getMethod());
        map.put("coin",
                withdraw.getMethod().indexOf("BTC") != -1 || withdraw.getMethod().indexOf("ETH") != -1
                        ? withdraw.getMethod()
                        : "USDT");
        map.put("state", withdraw.getStatus());
        map.put("tx", withdraw.getTx());
        map.put("failure_msg", withdraw.getFailureMsg());
        return Result.succeed(map);
    }

    /**
     * 提现记录
     */
    @GetMapping("list")
    @ApiOperation("提现记录")
    public Result list(@RequestParam String page_no) {
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
        List<Map<String, Object>> data = this.walletLogService.pagedQueryWithdraw(page_no_int, 10, SecurityUtils.getUser().getUserId(), "1").getRecords();
        for (Map<String, Object> log : data) {
            if (null == log.get("coin") || !StringUtils.isNotEmpty(log.get("coin").toString()))
                log.put("coin", Constants.WALLET);
            else {
                log.put("coin", log.get("coin").toString().toUpperCase());
            }
        }
        return Result.succeed(data);
    }

    /**
     * 提现手续费
     * <p>
     * channel 渠道 USDT,OTC
     * amount 提币数量
     */
    @GetMapping("fee")
    @ApiOperation("提现手续费")
    public Result fee(String channel, String amount) {
            String error = this.verif(amount);
            if (!StringUtils.isNullOrEmpty(error)) {
                throw new YamiShopBindException(error);
            }

            double amount_double = Double.valueOf(amount).doubleValue();

            Map<String, Object> map = new HashMap<String, Object>();

            DecimalFormat df = new DecimalFormat("#.########");

            /*channel = StringUtils.isEmptyString(channel) ? "USDT" : channel;
            if (channel.indexOf("BTC") != -1 || channel.indexOf("ETH") != -1) {
                map.put("withdraw_fee_type", "rate");
                fee = this.withdrawService.getOtherChannelWithdrawFee(amount_double);
            } else {
                // 手续费(USDT)

                // 提现手续费类型,fixed是单笔固定金额，rate是百分比，part是分段
                String withdraw_fee_type = this.sysparaService.find("withdraw_fee_type").getSvalue();

                // fixed单笔固定金额 和 rate百分比 的手续费数值
                double withdraw_fee = Double.valueOf(this.sysparaService.find("withdraw_fee").getSvalue());

                if ("fixed".equals(withdraw_fee_type)) {
                    fee = withdraw_fee;
                }

                if ("rate".equals(withdraw_fee_type)) {
                    withdraw_fee = Arith.div(withdraw_fee, 100);
                    fee = Arith.mul(amount_double, withdraw_fee);
                }

                if ("part".equals(withdraw_fee_type)) {

                    // 提现手续费part分段的值
                    String withdraw_fee_part = this.sysparaService.find("withdraw_fee_part").getSvalue();

                    String[] withdraw_fee_parts = withdraw_fee_part.split(",");
                    for (int i = 0; i < withdraw_fee_parts.length; i++) {
                        double part_amount = Double.valueOf(withdraw_fee_parts[i]);
                        double part_fee = Double.valueOf(withdraw_fee_parts[i + 1]);
                        if (amount_double <= part_amount) {
                            fee = part_fee;
                            break;
                        }
                        i++;
                    }
                }
                map.put("withdraw_fee_type", withdraw_fee_type);
            }*/
        double fee = 0.005;

        fee = Arith.mul(amount_double, fee);
        double volume_last = Arith.sub(amount_double, fee);
            map.put("fee", fee);
            map.put("volume_last", df.format(volume_last));
        return Result.succeed(map);
    }

    /**
     * 提现限额
     * <p>
     * channel 渠道 USDT,OTC
     */
    @GetMapping("limit")
    @ApiOperation("提现限额")
    public Result limit(@RequestParam String channel) {
        Map<String, Object> map = new HashMap<String, Object>();
        channel = StringUtils.isEmptyString(channel) ? "USDT" : channel;
        map.put("limit", this.sysparaService.find("withdraw_limit").getSvalue());
        map.put("limitMax", this.sysparaService.find("withdraw_limit_max").getSvalue());
        return Result.succeed(map);
    }

    private String verif(String amount) {
        if (StringUtils.isNullOrEmpty(amount)) {
            return "提币数量必填";
        }
        if (!StringUtils.isDouble(amount)) {
            return "提币数量输入错误，请输入浮点数";
        }
        if (Double.valueOf(amount).doubleValue() <= 0) {
            return "提币数量不能小于等于0";
        }
        return null;
    }
}
