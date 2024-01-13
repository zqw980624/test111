package com.yami.trading.api.controller;

import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.model.RealNameAuthRecord;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.BusinessException;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.common.util.UTCDateUtils;
import com.yami.trading.common.web.ResultObject;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.RealNameAuthRecordService;
import com.yami.trading.service.WalletService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@Api(tags = "资产")
@Slf4j
public class ApiAssetsController {

    private final String action = "/api/assets!";

    @Autowired
    RealNameAuthRecordService realNameAuthRecordService;

    @Autowired
    WalletService walletService;

    /**
     * 总账户资产 所有币种，订单资产转换到Usdt余额
     */
    @RequestMapping(action + "getContractBySymbolType.action")
    @ApiOperation("总账户资产 所有币种，订单资产转换到Usdt余额")
    public Result<Map<String, String>> getContractBySymbolType(String symbolType) {
        Map<String, String> data = new HashMap<String, String>();
        DecimalFormat df2 = new DecimalFormat("#.##");
        // 向下取整
        df2.setRoundingMode(RoundingMode.HALF_UP);
        String partyId = SecurityUtils.getCurrentUserId();
        if ("".equals(partyId) || null == partyId) {
            // 当前外汇资产
            data.put("money_contract", df2.format(0));
            data.put("money_contract_deposit", df2.format(0));
            // 外汇浮动盈亏
            data.put("money_contract_profit", df2.format(0));
            // 当日盈亏
            data.put("money_contract_profit_today", df2.format(0));
            // 外汇可用余额
            data.put("money_wallet", df2.format(0));
        } else {
            Map<String, Double> moneyContract = walletService.getMoneyContract(partyId, symbolType);
            data.put("money_contract", df2.format(moneyContract.get("money_contract")));
            data.put("money_contract_deposit", df2.format(moneyContract.get("money_contract_deposit")));
            // 外汇浮动盈亏
            data.put("money_contract_profit", df2.format(moneyContract.get("money_contract_profit")));
            // 当日盈亏
            data.put("money_contract_profit_today", df2.format(moneyContract.get("money_contract_profit_today")));
            // 外汇可用余额
            data.put("money_wallet", df2.format(moneyContract.get("money_wallet")));
        }
        return Result.ok(data);
    }

    @RequestMapping(action + "getAllAggregation.action")
    @ApiOperation("总账户资产 所有币种，订单资产转换到Usdt余额")
    public Object getAllAggregation() {
        ResultObject resultObject = new ResultObject();
        Map<String, Object> data = new HashMap<String, Object>();
        try {
            List<String> types = new ArrayList<>();
            types.add(Item.indices);
            types.add(Item.forex);
            types.add(Item.cryptos);
            types.add(Item.US_STOCKS);
            for (String type : types) {
                data.put(type, getAssert(type));
            }
            data.put("all", getAssert(""));
            resultObject.setData(data);
            return resultObject;


        } catch (BusinessException e) {
            resultObject.setCode("1");
            resultObject.setMsg(e.getMessage());
        } catch (Throwable t) {
            resultObject.setCode("1");
            resultObject.setMsg("程序错误");
            log.error("error:", t);
        }
        return resultObject;

    }

    /**
     * 总账户资产 所有币种，订单资产转换到Usdt余额
     */
    @RequestMapping(action + "getAll.action")
    @ApiOperation("总账户资产 所有币种，订单资产转换到Usdt余额")
    public Object getAll(@RequestParam(required = false) String symbolType) {
        ResultObject resultObject = new ResultObject();

        Map<String, Object> data = new HashMap<String, Object>();
        DecimalFormat df2 = new DecimalFormat("#.##");
        // 向下取整
        df2.setRoundingMode(RoundingMode.FLOOR);

        String partyId = SecurityUtils.getCurrentUserId();
        try {

            if ("".equals(partyId) || null == partyId) {

                data.put("total", df2.format(0));
                data.put("lock_money", df2.format(0));
                //冻结金额
                data.put("freeze_money", df2.format(0));
                data.put("money_wallet", df2.format(0));
                data.put("money_coin", df2.format(0));
                data.put("money_all_coin", df2.format(0));
                data.put("money_miner", df2.format(0));
                data.put("money_finance", df2.format(0));
                data.put("money_contract", df2.format(0));
                data.put("money_contract_deposit", df2.format(0));
                data.put("money_contract_profit", df2.format(0));
                data.put("money_futures", df2.format(0));
                data.put("money_futures_profit", df2.format(0));

            } else {
                if (StringUtils.isNotEmpty(symbolType)) {
                    data = walletService.getMoneyAll(partyId, symbolType);
                } else {
                    data = walletService.getMoneyAll(partyId);
                }
            }

            RealNameAuthRecord kyc = realNameAuthRecordService.getByUserId(partyId);
            data.put("status", kyc == null ? 0 : kyc.getStatus());
            resultObject.setData(data);
        } catch (BusinessException e) {
            resultObject.setCode("1");
            resultObject.setMsg(e.getMessage());
        } catch (Throwable t) {
            resultObject.setCode("1");
            resultObject.setMsg("程序错误");
            log.error("error:", t);
        }

        return resultObject;
    }

    public Map<String, Object> getAssert(String symbolType) {
        Map<String, Object> data = new HashMap<String, Object>();
        DecimalFormat df2 = new DecimalFormat("#.##");
        // 向下取整
        df2.setRoundingMode(RoundingMode.FLOOR);

        String partyId = SecurityUtils.getCurrentUserId();
        if ("".equals(partyId) || null == partyId) {
            data.put("total", df2.format(0));
            data.put("lock_money", df2.format(0));
            //冻结金额
            data.put("freeze_money", df2.format(0));
            data.put("money_wallet", df2.format(0));
            data.put("money_coin", df2.format(0));
            data.put("money_all_coin", df2.format(0));
            data.put("money_miner", df2.format(0));
            data.put("money_finance", df2.format(0));
            data.put("money_contract", df2.format(0));
            data.put("money_contract_deposit", df2.format(0));
            data.put("money_contract_profit", df2.format(0));
            data.put("money_futures", df2.format(0));
            data.put("money_futures_profit", df2.format(0));

        } else {
            if (StringUtils.isNotEmpty(symbolType)) {
                data = walletService.getMoneyAll(partyId, symbolType);
            } else {
                data = walletService.getMoneyAll(partyId);
            }
        }

        RealNameAuthRecord kyc = realNameAuthRecordService.getByUserId(partyId);
        data.put("status", kyc == null ? 0 : kyc.getStatus());
        return data;

    }


    /**
     * 获取当前是否休市
     */
    @RequestMapping(action + "isClosed.action")
    public Result<Map<String, Object>> isClosed() throws IOException, ParseException {
        Map<String, Object> data = new HashMap<String, Object>();


        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        f.setTimeZone(TimeZone.getTimeZone(UTCDateUtils.GMT_TIME_ZONE));
        Date now = f.parse(f.format(new Date()));
//        if (now.before(UTCDateUtils.getClosedTime()) && now.after(UTCDateUtils.getOpenTime())) {
//            data.put("isClosed", "false");
//        } else {
//            // 休市
//            data.put("isClosed", "true");
//        }
        return Result.ok(data);

    }

    @RequestMapping(action + "getTime.action")
    public Result<Map<String, Object>> getTime() throws IOException {
        Map<String, Object> data = new HashMap<String, Object>();
        Date date = new Date();
        data.put("time", date.toGMTString());
        data.put("time2", System.currentTimeMillis());
        return Result.ok(data);
    }


}
