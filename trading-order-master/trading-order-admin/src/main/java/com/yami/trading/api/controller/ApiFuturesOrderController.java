package com.yami.trading.api.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.api.dto.FutureOpenAction;
import com.yami.trading.bean.future.domain.FuturesLock;
import com.yami.trading.bean.future.domain.FuturesOrder;
import com.yami.trading.bean.future.domain.FuturesPara;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.model.Wallet;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.BusinessException;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.MarketOpenChecker;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.common.util.*;
import com.yami.trading.service.SessionTokenService;
import com.yami.trading.service.WalletService;
import com.yami.trading.service.future.FuturesOrderService;
import com.yami.trading.service.future.FuturesParaService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 交割合约订单
 */
@CrossOrigin
@Api(tags = "【h5】交割合约订单")
@RestController
@Slf4j
public class ApiFuturesOrderController {
    public final static String action = "/api/futuresOrder!";
    @Autowired
    private SessionTokenService sessionTokenService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private UserService partyService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private FuturesOrderService futuresOrderService;
    @Autowired
    private FuturesParaService futuresParaService;
    /**
     * 开仓页面参数
     * <p>
     * symbol 币种
     */
    @GetMapping(action + "openview.action")
    @ApiOperation(value = "开仓页面参数")
    public Result<Map<String, Object>> openview(@RequestParam @NotEmpty String symbol) {


        Map<String, Object> data = new HashMap<>();
       // Item bySymbol = itemService.findBySymbol(symbol);
        QueryWrapper<Item> itemSummaryQueryWrapper = new QueryWrapper<>();
        itemSummaryQueryWrapper.eq("symbol", symbol);
        List<Item> list = itemService.list(itemSummaryQueryWrapper);
        Item bySymbol = itemService.findBySymbol(list.get(0).getSymbol());
        if(bySymbol == null){
            throw new YamiShopBindException("当前币对不存在");
        }
        List<Map<String,Object>> futuresParas = new ArrayList<>();
        for (FuturesPara para : this.futuresParaService.getBySymbolSort(symbol)) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("para_id", para.getUuid().toString());
            map.put("symbol", para.getSymbol());
            map.put("time_num", para.getTimenum());
            map.put("time_unit", para.getTimeunit());
            map.put("entityVersion", 0);
            map.put("timeUnitCn", para.getTimeunit());
            map.put("timestamp", para.getCreateTimeTs());
            map.put("now_time", DateUtils.format(new Date(),DateUtils.NORMAL_DATE_FORMAT));


            DecimalFormat df = new DecimalFormat("#");
            if (para.getProfitRatio().doubleValue() < 0.01 || para.getProfitRatioMax().doubleValue() < 0.01) {
                df = new DecimalFormat("#.#");
            }
            // TMX start voaex
//		map.put("profit_ratio", df.format(Arith.mul(para.getProfit_ratio(), 100)));
            // TMX end
//		if(para.getProfit_ratio() != para.getProfit_ratio_max()) {
            map.put("profit_ratio", df.format(Arith.mul(para.getProfitRatio().doubleValue(), 100)) + "~"
                    + df.format(Arith.mul(para.getProfitRatioMax().doubleValue(), 100)));
//		}
            map.put("buy_min", para.getUnitAmount());
            map.put("unit_fee", para.getUnitFee());
            map.put("buy_max", para.getUnitMaxAmount() .compareTo(BigDecimal.ZERO)<=0 ? null : para.getUnitMaxAmount());
            futuresParas.add(map);
        }
        data.put("para", futuresParas);

        String partyId = SecurityUtils.getCurrentUserId();
        if (StrUtil.isNotBlank(partyId) && futuresParas != null) {
            Wallet wallet = this.walletService.findByUserId(partyId);
            // 账户剩余资金
            String session_token = this.sessionTokenService.savePut(partyId);
            data.put("session_token", session_token);
            data.put("amount", wallet.getMoney().longValue());
        } else {
            data.put("amount", 0);
        }
        data.put("open", MarketOpenChecker.isMarketOpenByItemCloseType(bySymbol.getOpenCloseType()));
        return Result.succeed(data);


    }

    /**
     * 开仓
     * <p>
     * symbol 币种
     * direction "buy":多 "sell":空
     * amount 委托数量(张)
     * para_id 交割合约参数
     */
    @GetMapping(action+ "open.action")
    @ApiOperation(value = "开仓")
    public Result<Map<String, String>> open(FutureOpenAction futureOpenAction) {
        Item bySymbol = itemService.findBySymbol(futureOpenAction.getSymbol());
        if(bySymbol == null){
            throw  new YamiShopBindException("当前币对不存在");
        }
        boolean isOpen = MarketOpenChecker.isMarketOpenByItemCloseType(bySymbol.getOpenCloseType());
        if(!isOpen){
            throw  new YamiShopBindException("当前已经休市");
        }
        String partyId = SecurityUtils.getUser().getUserId();
        boolean lock = false;

        try {
            Map<String, String> data = new HashMap<String, String>();

            if (!FuturesLock.add(partyId)) {
                throw new YamiShopBindException("请稍后再试");
            }

            lock = true;
            String session_token = futureOpenAction.getSession_token();
            Object object = this.sessionTokenService.cacheGet(session_token);
            this.sessionTokenService.del(session_token);
            User party = this.partyService.findUserByUserCode(partyId);
            if (!party.isEnabled()) {
                throw new YamiShopBindException("用户已锁定");
            }
            if (null == object || !party.getUserId().equals((String) object)) {
                throw new BusinessException("请稍后再试");
            }

            FuturesOrder order = new FuturesOrder();
            order.setPartyId(partyId);
            order.setSymbol(futureOpenAction.getSymbol());
            order.setDirection(futureOpenAction.getDirection());
            order.setVolume(futureOpenAction.getAmount().doubleValue());

            order = this.futuresOrderService.saveOpen(order, futureOpenAction.getPara_id());
            data.put("order_no", order.getOrderNo());
            data.put("open_price", order.getTradeAvgPrice().toString());

            return Result.succeed(data);
        }catch (Exception e){
            log.error("开仓异常", e);
           throw new YamiShopBindException(e.getMessage());
        }finally {
            if (lock) {
                ThreadUtils.sleep(100);
                FuturesLock.remove(partyId);
            }
        }

    }

    /**
     * 查询交割持仓列表
     * <p>
     * page_no 页码
     * symbol 币种
     * type 开仓页面订单类型
     */
    @RequestMapping(action + "list.action")
    @ApiOperation("查询交割持仓列表,实时价格，通过价格接口取")
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false) String symbol, @RequestParam(required = false) String type, @RequestParam(required = false) String page_no
    , @RequestParam(required = false) String date,  @RequestParam(required = false)String startTime,
                                                  @RequestParam(required = false)String endTime,@RequestParam(required = false)String symbolType
    ) {
        IPage<FuturesOrder> page = new Page<>();
        if(StringUtils.isEmptyString(page_no)){
            page.setCurrent(1);
        }else{
            page.setCurrent(Long.parseLong(page_no));
;       }
        String loginPartyId = SecurityUtils.getUser().getUserId();
        IPage<FuturesOrder> paged = this.futuresOrderService.getPaged(page, loginPartyId, symbol, type, date, startTime, endTime, symbolType);
        List<FuturesOrder> list = paged.getRecords();
        Result<List<Map<String, Object>>> succeed = Result.succeed(futuresOrderService.bulidData(list));
        succeed.setTotal(paged.getTotal());
        return succeed;


    }

    /**
     * 查询交割持仓详情
     * <p>
     * order_no 订单号
     */
    @RequestMapping(action + "get.action")
    public Result<Map<String, Object>> get(@RequestParam @NotEmpty String order_no) {
        FuturesOrder order = this.futuresOrderService.cacheByOrderNo(order_no);
        if (null == order) {
            log.info("futuresOrder!get order_no:" + order_no + ", order null");
            throw new YamiShopBindException("订单不存在");
        }
        return Result.succeed(this.futuresOrderService.bulidOne(order));
    }

}
