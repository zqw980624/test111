package com.yami.trading.admin.controller.c2c;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.controller.c2c.model.*;
import com.yami.trading.bean.c2c.C2cAdvert;
import com.yami.trading.bean.c2c.C2cUser;
import com.yami.trading.bean.model.C2cPaymentMethod;
import com.yami.trading.bean.model.Log;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.Arith;
import com.yami.trading.common.util.HttpContextUtils;
import com.yami.trading.common.util.IPHelper;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.c2c.*;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.*;

@RestController
@RequestMapping("c2cAdvert")
@Api(tags = " C2C广告")
public class C2cAdvertController {

    @Autowired
    private C2cAdvertService adminC2cAdvertService;
    @Autowired
    private C2cAdvertService c2cAdvertService;
    @Autowired
    SysUserService sysUserService;
    @Autowired
    private C2cPaymentMethodService c2cPaymentMethodService;
    @Autowired
    private UserService secUserService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private LogService logService;
    @Autowired
    private C2cUserService c2cUserService;
    @Autowired
    private C2cOrderService c2cOrderService;
    @Autowired
    private C2cAppealService c2cAppealService;
    @Autowired
    private SysparaService sysparaService;

    /**
     * 获取 C2C广告 列表
     */
    @ApiOperation("获取 C2C广告 列表")
    @PostMapping("list")
    public Result list(@RequestBody @Valid C2cAdvertListModel model) {

        String c2cUserCode = model.getC2c_user_code();
        String c2cUserType = model.getC2c_user_type();
        String userCode =model.getUser_code();
        String direction = model.getDirection();
        String currency = model.getCurrency();
        String symbol = model.getSymbol();
        Page page =adminC2cAdvertService.pagedQuery(model.getCurrent(), model.getSize(), c2cUserCode, c2cUserType, userCode,
                direction, currency, symbol);
        return Result.succeed(page);
    }

    @GetMapping("getCurrencyMap")
    @ApiOperation("所有支付币种")
    public Result getCurrencyMap() {

        Map<String, String> currencyMap = c2cAdvertService.getCurrencyMap();
        return Result.succeed(currencyMap);
    }

    @GetMapping("getSymbolMap")
    @ApiOperation("所有上架币种")
    public Result getSymbolMap() {

        Map<String, String> symbolMap = c2cAdvertService.getSymbolMap();
        return Result.succeed(symbolMap);
    }


    @GetMapping("getExpireTimeMap")
    @ApiOperation(" 获取 广告支付时效 列表")
    public Result getExpireTimeMap() {

        return Result.succeed(c2cAdvertService.getC2cSyspara("c2c_advert_expire_time"));
    }

    /**
     * 新增 C2C广告
     */
    @PostMapping( "add")
    @ApiOperation("新增 C2C广告")
    public Result add(@RequestBody @Valid  C2cAdvertAddModel model) {

        String c2c_user_code = model.getC2c_user_code();
        String direction =model.getDirection();
        String payment_method1 = model.getPayment_method1();
        String payment_method2 = model.getPayment_method2();
        String payment_method3 = model.getPayment_method3();
        String currency =model.getCurrency();
        String symbol =model.getSymbol();
        String coin_amount = model.getCoin_amount();
        String symbol_value = model.getSymbol_value();
        String investment_min = model.getInvestment_min();
        String investment_max = model.getInvestment_max();
        String on_sale =model.getOn_sale();
        String sort_index = model.getSort_index();
        String expire_time =model.getExpire_time();
        String transaction_terms = model.getTransaction_terms();
        String order_msg =model.getOrder_msg();
        String remark =model.getRemark();
        String login_safeword = model.getRemark();
        // 支付方式拼接
        String pay_type = "";
        if (StringUtils.isNotEmpty(payment_method1)) {
            if ("".equals(pay_type)) {
                pay_type += payment_method1;
            } else {
                pay_type = pay_type + "," + payment_method1;
            }
        }
        if (StringUtils.isNotEmpty(payment_method2) && !pay_type.contains(payment_method2)) {
            if ("".equals(pay_type)) {
                pay_type += payment_method2;
            } else {
                pay_type = pay_type + "," + payment_method2;
            }
        }
        if (StringUtils.isNotEmpty(payment_method3) && !pay_type.contains(payment_method3)) {
            if ("".equals(pay_type)) {
                pay_type += payment_method3;
            } else {
                pay_type = pay_type + "," + payment_method3;
            }
        }
        String error = this.verif(c2c_user_code, direction, pay_type, currency, symbol, coin_amount, symbol_value,
                investment_min, investment_max, on_sale, sort_index, expire_time, login_safeword);
        if (!StringUtils.isNullOrEmpty(error)) {
            throw new YamiShopBindException(error);
        }
        if (StringUtils.isEmptyString(coin_amount) || !StringUtils.isDouble(coin_amount) || Double.valueOf(coin_amount).doubleValue() <= 0) {
            throw new YamiShopBindException("交易币种数量未填或格式不正确");
        }
        if (StringUtils.isEmptyString(symbol_value) || !StringUtils.isDouble(symbol_value) || Double.valueOf(symbol_value).doubleValue() <= 0) {
            throw new YamiShopBindException("币种单价未填或格式不正确");
        }
        DecimalFormat df = new DecimalFormat("#.########");
        double coin_amount_double = Double.valueOf(df.format(Double.valueOf(coin_amount))).doubleValue();
        double symbol_value_double = Double.valueOf(df.format(Double.valueOf(symbol_value))).doubleValue();
        double investment_min_double = Double.valueOf(df.format(Double.valueOf(investment_min))).doubleValue();
        double investment_max_double = Double.valueOf(df.format(Double.valueOf(investment_max))).doubleValue();
        C2cUser c2cUser = c2cUserService.findByUsercode(c2c_user_code);
        if (null == c2cUser) {
            throw new YamiShopBindException("承兑商不存在");
        }
        // C2C承兑商添加广告最大数量
        List<C2cAdvert> advertList = this.c2cAdvertService.getByC2cUserId(c2cUser.getUuid().toString());
        Object obj = this.sysparaService.find("c2c_advert_count_max");
        if (null != obj) {
            if (advertList.size() >= Integer.valueOf(this.sysparaService.find("c2c_advert_count_max").getSvalue()).intValue()) {
                throw new YamiShopBindException("广告数量已达上限");
            }
        }
        // 计算广告参数
        Map<String, Object> computeValue = this.c2cAdvertService.getComputeValue(c2cUser.getDeposit(), currency, symbol, coin_amount_double, symbol_value_double);
        if (coin_amount_double > Double.valueOf(computeValue.get("coin_amount_max").toString()).doubleValue()) {
            throw new YamiShopBindException("交易币种数量不能大于最大可交易数量");
        }
        if (investment_min_double <= 0 || investment_min_double < Double.valueOf(computeValue.get("investment_min_limit").toString()).doubleValue()) {
            throw new YamiShopBindException("单笔订单支付金额下限错误");
        }
        if (investment_max_double > Double.valueOf(computeValue.get("investment_max_limit").toString()).doubleValue()) {
            throw new YamiShopBindException("单笔订单支付金额上限错误");
        }
        double pay_rate_double = Double.valueOf(computeValue.get("pay_rate").toString()).doubleValue();
        double symbol_close_double = Double.valueOf(computeValue.get("symbol_close").toString()).doubleValue();
        double deposit_open_double = Double.valueOf(computeValue.get("deposit_open").toString()).doubleValue();
        if (deposit_open_double < 0) {
            throw new YamiShopBindException("广告保证金不能小于0");
        }
        C2cAdvert c2cAdvert = new C2cAdvert();
        c2cAdvert.setC2cUserId(c2cUser.getUuid());
        c2cAdvert.setDirection(direction);
        c2cAdvert.setCurrency(currency);
        c2cAdvert.setSymbol(symbol);
        c2cAdvert.setSymbolClose(symbol_close_double);
        c2cAdvert.setPayRate((int) pay_rate_double);
        c2cAdvert.setPayType(pay_type);
        c2cAdvert.setSymbolValue(symbol_value_double);
        c2cAdvert.setCoinAmount(coin_amount_double);
        c2cAdvert.setInvestmentMin(investment_min_double);
        c2cAdvert.setInvestmentMax(investment_max_double);
        c2cAdvert.setDeposit(deposit_open_double);
        c2cAdvert.setDepositOpen(deposit_open_double);
        c2cAdvert.setOnSale(Integer.valueOf(on_sale).intValue());
        c2cAdvert.setClosed(0);
        c2cAdvert.setSortIndex(StringUtils.isNotEmpty(sort_index) ? Integer.valueOf(sort_index).intValue() : 0);
        c2cAdvert.setExpireTime(Integer.valueOf(expire_time).intValue());
        c2cAdvert.setTransactionTerms(transaction_terms);
        c2cAdvert.setOrderMsg(order_msg);
        c2cAdvert.setRemark(remark);
        c2cAdvert.setCreateTime(new Date());
        c2cAdvert.setUpdateTime(new Date());
        this.c2cAdvertService.save(c2cAdvert);
        double oldC2cUserDeposit = c2cUser.getDeposit();
        c2cUser.setDeposit(Arith.sub(c2cUser.getDeposit(), deposit_open_double));
        c2cUserService.updateById(c2cUser);
        String log = MessageFormat.format("ip:" + IPHelper.getIpAddr()
                        + ",管理员新增承兑商广告,id:{0},承兑商ID:{1},买卖方式:{2},支付币种:{3},上架币种:{4},上架币种实时行情价:{5},支付比率:{6},支付方式:{7},币种单价:{8},"
                        + "币种数量:{9},单笔订单最低限额:{10},单笔订单最高限额:{11},剩余派单金额:{12},派单金额:{13},是否上架:{14},是否关闭:{15},"
                        + "排序索引:{16},支付时效:{17},交易条款:{18},订单自动消息:{19},备注:{20},创建时间:{21},更新时间:{22}#####原承兑商剩余派单金额:{23},新承兑商剩余派单金额:{24}",
                c2cAdvert.getUuid(), c2cAdvert.getC2cUserId(), c2cAdvert.getDirection(), c2cAdvert.getCurrency(), c2cAdvert.getSymbol(), c2cAdvert.getSymbolClose(), c2cAdvert.getPayRate(), c2cAdvert.getPayType(), c2cAdvert.getSymbolValue(),
                c2cAdvert.getCoinAmount(), c2cAdvert.getInvestmentMin(), c2cAdvert.getInvestmentMax(), c2cAdvert.getDeposit(), c2cAdvert.getDepositOpen(), c2cAdvert.getOnSale(), c2cAdvert.getClosed(),
                c2cAdvert.getSortIndex(), c2cAdvert.getExpireTime(), c2cAdvert.getTransactionTerms(), c2cAdvert.getOrderMsg(), c2cAdvert.getRemark(), c2cAdvert.getCreateTime(), c2cAdvert.getUpdateTime(),
                oldC2cUserDeposit, c2cUser.getDeposit());
        User user = secUserService.getById(c2cUser.getC2cUserPartyId());
        this.saveLog(user, SecurityUtils.getSysUser().getUsername(), log, Constants.LOG_CATEGORY_C2C);
        return Result.succeed();
    }

    /**
     * 修改 C2C广告
     */
    @PostMapping(  "update")
    @ApiOperation(" 修改 C2C广告")
    public Result update(@RequestBody @Valid C2cAdvertAddModel  model) {

        String id = model.getId();
        String payment_method1 =model.getPayment_method1();
        String payment_method2 = model.getPayment_method2();
        String payment_method3 = model.getPayment_method3();
        String c2c_user_code = model.getC2c_user_code();
        String direction = model.getDirection();
        String coin_amount = model.getCoin_amount();
        String symbol_value = model.getSymbol_value();
        String currency = model.getCurrency();
        String symbol = model.getSymbol();
        String investment_min = model.getInvestment_min();
        String investment_max =model.getInvestment_max();
        String on_sale =model.getOn_sale();
        String sort_index = model.getSort_index();
        String expire_time = model.getExpire_time();
        String transaction_terms =model.getTransaction_terms();
        String order_msg = model.getOrder_msg();
        String remark = model.getRemark();
        String login_safeword = model.getLogin_safeword();
        // 支付方式拼接
        String pay_type = "";
        if (StringUtils.isNotEmpty(payment_method1)) {
            if ("".equals(pay_type)) {
                pay_type += payment_method1;
            } else {
                pay_type = pay_type + "," + payment_method1;
            }
        }
        if (StringUtils.isNotEmpty(payment_method2) && !pay_type.contains(payment_method2)) {
            if ("".equals(pay_type)) {
                pay_type += payment_method2;
            } else {
                pay_type = pay_type + "," + payment_method2;
            }
        }
        if (StringUtils.isNotEmpty(payment_method3) && !pay_type.contains(payment_method3)) {
            if ("".equals(pay_type)) {
                pay_type += payment_method3;
            } else {
                pay_type = pay_type + "," + payment_method3;
            }
        }
        String error = this.verif(c2c_user_code, direction, pay_type, currency, symbol, coin_amount, symbol_value,
                investment_min, investment_max, on_sale, sort_index, expire_time, login_safeword);
        if (!StringUtils.isNullOrEmpty(error)) {
            throw new YamiShopBindException(error);
        }
        if (StringUtils.isEmptyString(coin_amount) || !StringUtils.isDouble(coin_amount) || Double.valueOf(coin_amount).doubleValue() <= 0) {
            throw new YamiShopBindException("交易币种数量未填或格式不正确");
        }
        if (StringUtils.isEmptyString(symbol_value) || !StringUtils.isDouble(symbol_value) || Double.valueOf(symbol_value).doubleValue() <= 0) {
            throw new YamiShopBindException("币种单价未填或格式不正确");
        }
        DecimalFormat df = new DecimalFormat("#.########");
        double coin_amount_double = Double.valueOf(df.format(Double.valueOf(coin_amount))).doubleValue();
        double symbol_value_double = Double.valueOf(df.format(Double.valueOf(symbol_value))).doubleValue();
        double investment_min_double = Double.valueOf(df.format(Double.valueOf(investment_min))).doubleValue();
        double investment_max_double = Double.valueOf(df.format(Double.valueOf(investment_max))).doubleValue();
        C2cUser c2cUser = this.c2cUserService.findByUsercode(c2c_user_code);
        if (null == c2cUser) {
            throw new YamiShopBindException("承兑商不存在");
        }
        C2cAdvert c2cAdvert = this.c2cAdvertService.getById(id);
        if (null == c2cAdvert || !c2cAdvert.getC2cUserId().equals(c2cUser.getUuid().toString())) {
            throw new YamiShopBindException("广告不存在");
        }
        String userNameLogin = SecurityUtils.getSysUser().getUsername();
        // 验证资金密码
        sysUserService.checkSafeWord(login_safeword);
        Long orderCount = this.c2cOrderService.findNoEndingOrdersCountByAdvertId(id);
        if (orderCount > 0) {
            throw new YamiShopBindException("广告还有未完结订单，不能修改");
        }
        Long appealCount = this.c2cAppealService.findNoHandleAppealsCountByAdvertId(id);
        if (null == appealCount) {
            throw new YamiShopBindException("广告还有未处理的订单申诉，不能修改");
        }
        // 计算广告参数
        Map<String, Object> computeValue = this.c2cAdvertService.getComputeValue(Arith.add(c2cUser.getDeposit(), c2cAdvert.getDeposit()), currency, symbol,
                coin_amount_double, symbol_value_double);
        if (coin_amount_double > Double.valueOf(computeValue.get("coin_amount_max").toString()).doubleValue()) {
            throw new YamiShopBindException("交易币种数量不能大于最大可交易数量");
        }
        if (investment_min_double <= 0 || investment_min_double < Double.valueOf(computeValue.get("investment_min_limit").toString()).doubleValue()) {
            throw new YamiShopBindException("单笔订单支付金额下限错误");
        }
        if (investment_max_double > Double.valueOf(computeValue.get("investment_max_limit").toString()).doubleValue()) {
            throw new YamiShopBindException("单笔订单支付金额上限错误");
        }
//			modelAndView.addObject("all_deposit", c2cUser.getDeposit());
        double pay_rate_double = Double.valueOf(computeValue.get("pay_rate").toString()).doubleValue();
        double symbol_close_double = Double.valueOf(computeValue.get("symbol_close").toString()).doubleValue();
        double deposit_old_double = c2cAdvert.getDeposit();
        double deposit_double = Double.valueOf(computeValue.get("deposit_open").toString()).doubleValue();
        double change = deposit_double - deposit_old_double;
        if (deposit_double < 0) {
            throw new YamiShopBindException("广告保证金不能小于0");
        }
        if (change > 0) {
            if (Double.valueOf(change).doubleValue() > c2cUser.getDeposit()) {
                throw new YamiShopBindException("广告保证金增加差值不能大于承兑商剩余总保证金");
            }
        }
        String log = MessageFormat.format("ip:" + IPHelper.getIpAddr()
                        + ",管理员修改承兑商广告,id:{0},原承兑商ID:{1},原买卖方式:{2},原支付币种:{3},原上架币种:{4},原上架币种实时行情价:{5},原支付比率:{6},原支付方式:{7},原币种单价:{8},"
                        + "原币种数量:{9},原单笔订单最低限额:{10},原单笔订单最高限额:{11},原剩余保证金:{12},原保证金:{13},原是否上架:{14},原是否关闭:{15},"
                        + "原排序索引:{16},原支付时效:{17},原交易条款:{18},原订单自动消息:{19},原备注:{20},原创建时间:{21},原更新时间:{22}",
                c2cAdvert.getUuid(), c2cAdvert.getC2cUserId(), c2cAdvert.getDirection(), c2cAdvert.getCurrency(), c2cAdvert.getSymbol(), c2cAdvert.getSymbolClose(), c2cAdvert.getPayRate(), c2cAdvert.getPayType(), c2cAdvert.getSymbolValue(),
                c2cAdvert.getCoinAmount(), c2cAdvert.getInvestmentMin(), c2cAdvert.getInvestmentMax(), c2cAdvert.getDeposit(), c2cAdvert.getDepositOpen(), c2cAdvert.getOnSale(), c2cAdvert.getClosed(),
                c2cAdvert.getSortIndex(), c2cAdvert.getExpireTime(), c2cAdvert.getTransactionTerms(), c2cAdvert.getOrderMsg(), c2cAdvert.getRemark(), c2cAdvert.getCreateTime(), c2cAdvert.getUpdateTime());
        c2cAdvert.setC2cUserId(c2cUser.getUuid().toString());
        c2cAdvert.setDirection(direction);
        c2cAdvert.setCurrency(currency);
        c2cAdvert.setSymbol(symbol);
        c2cAdvert.setSymbolClose(symbol_close_double);
        c2cAdvert.setPayRate((int) pay_rate_double);
        c2cAdvert.setPayType(pay_type);
        c2cAdvert.setSymbolValue(symbol_value_double);
        c2cAdvert.setCoinAmount(coin_amount_double);
        c2cAdvert.setInvestmentMin(investment_min_double);
        c2cAdvert.setInvestmentMax(investment_max_double);
        c2cAdvert.setDeposit(deposit_double);
        c2cAdvert.setOnSale(Integer.valueOf(on_sale).intValue());
        c2cAdvert.setSortIndex(StringUtils.isNotEmpty(sort_index) ? Integer.valueOf(sort_index).intValue() : 0);
        c2cAdvert.setExpireTime(Integer.valueOf(expire_time).intValue());
        c2cAdvert.setTransactionTerms(transaction_terms);
        c2cAdvert.setOrderMsg(order_msg);
        c2cAdvert.setRemark(remark);
        c2cAdvert.setUpdateTime(new Date());
        this.c2cAdvertService.updateById(c2cAdvert);
        double oldC2cUserDeposit = c2cUser.getDeposit();
        c2cUser.setDeposit(Arith.sub(c2cUser.getDeposit(), change));
        this.c2cUserService.updateById(c2cUser);
        log += MessageFormat.format(",id:{0},新承兑商ID:{1},新买卖方式:{2},新支付币种:{3},新上架币种:{4},新上架币种实时行情价:{5},新支付比率:{6},新支付方式:{7},新币种单价:{8},"
                        + "新币种数量:{9},新单笔订单最低限额:{10},新单笔订单最高限额:{11},新剩余保证金:{12},新保证金:{13},新是否上架:{14},新是否关闭:{15},"
                        + "新排序索引:{16},新支付时效:{17},新交易条款:{18},新订单自动消息:{19},新备注:{20},新创建时间:{21},新更新时间:{22}#####原承兑商剩余保证金:{23},新承兑商剩余保证金:{24}",
                c2cAdvert.getUuid(), c2cAdvert.getC2cUserId(), c2cAdvert.getDirection(), c2cAdvert.getCurrency(), c2cAdvert.getSymbol(), c2cAdvert.getSymbolClose(), c2cAdvert.getPayRate(), c2cAdvert.getPayType(), c2cAdvert.getSymbolValue(),
                c2cAdvert.getCoinAmount(), c2cAdvert.getInvestmentMin(), c2cAdvert.getInvestmentMax(), c2cAdvert.getDeposit(), c2cAdvert.getDepositOpen(), c2cAdvert.getOnSale(), c2cAdvert.getClosed(),
                c2cAdvert.getSortIndex(), c2cAdvert.getExpireTime(), c2cAdvert.getTransactionTerms(), c2cAdvert.getOrderMsg(), c2cAdvert.getRemark(), c2cAdvert.getCreateTime(), c2cAdvert.getUpdateTime(),
                oldC2cUserDeposit, c2cUser.getDeposit());
        User user = secUserService.getById(c2cUser.getC2cUserPartyId());
        this.saveLog(user, userNameLogin, log, Constants.LOG_CATEGORY_C2C);
        return Result.succeed();
    }

    /**
     * 关闭 C2C广告
     */
    @PostMapping("close")
    @ApiOperation("关闭 C2C广告")
    public Result close(@RequestBody @Valid C2cAdverCloseModel model) {

        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        String id =model.getId();
        String login_safeword =model.getLogin_safeword();
        if (StringUtils.isEmptyString(id)) {
            throw new YamiShopBindException("广告id不正确");
        }
        if (StringUtils.isEmptyString(login_safeword)) {
            throw new YamiShopBindException("资金密码错误");
        }
        String userNameLogin = SecurityUtils.getSysUser().getUsername();
        sysUserService.checkSafeWord(model.getLogin_safeword());
        C2cAdvert c2cAdvert = this.c2cAdvertService.getById(id);
        if (null == c2cAdvert) {
            throw new YamiShopBindException("广告不存在");
        }
        C2cUser c2cUser = this.c2cUserService.getById(c2cAdvert.getC2cUserId());
        if (null == c2cUser) {
            throw new YamiShopBindException("承兑商不存在");
        }
        Long orderCount = this.c2cOrderService.findNoEndingOrdersCountByAdvertId(id);
        if (orderCount > 0) {
            throw new YamiShopBindException("广告还有未完结订单，不能关闭");
        }
        Long appealCount = this.c2cAppealService.findNoHandleAppealsCountByAdvertId(id);
        if (null == appealCount) {
            throw new YamiShopBindException("广告还有未处理的订单申诉，不能关闭");
        }
        double oldC2cUserDeposit = c2cUser.getDeposit();
        if (c2cAdvert.getDeposit() > 0) {
            // 退还保证金
            c2cUser.setDeposit(Arith.add(c2cUser.getDeposit(), c2cAdvert.getDeposit()));
            this.c2cUserService.updateById(c2cUser);
            c2cAdvert.setDeposit(0);
        }
        c2cAdvert.setOnSale(0);
        c2cAdvert.setClosed(1);
        this.c2cAdvertService.updateById(c2cAdvert);
        String log = MessageFormat.format("ip:" + IPHelper.getIpAddr()
                        + ",管理员关闭承兑商广告,id:{0},承兑商ID:{1},买卖方式:{2},支付币种:{3},上架币种:{4},上架币种实时行情价:{5},支付比率:{6},支付方式:{7},币种单价:{8},"
                        + "币种数量:{9},单笔订单最低限额:{10},单笔订单最高限额:{11},剩余保证金:{12},保证金:{13},是否上架:{14},是否关闭:{15},"
                        + "排序索引:{16},支付时效:{17},交易条款:{18},订单自动消息:{19},备注:{20},创建时间:{21},更新时间:{22}#####原承兑商剩余保证金:{23},新承兑商剩余保证金:{24}",
                c2cAdvert.getUuid(), c2cAdvert.getC2cUserId(), c2cAdvert.getDirection(), c2cAdvert.getCurrency(), c2cAdvert.getSymbol(), c2cAdvert.getSymbolClose(), c2cAdvert.getPayRate(), c2cAdvert.getPayType(), c2cAdvert.getSymbolValue(),
                c2cAdvert.getCoinAmount(), c2cAdvert.getInvestmentMin(), c2cAdvert.getInvestmentMax(), c2cAdvert.getDeposit(), c2cAdvert.getDepositOpen(), c2cAdvert.getOnSale(), c2cAdvert.getClosed(),
                c2cAdvert.getSortIndex(), c2cAdvert.getExpireTime(), c2cAdvert.getTransactionTerms(), c2cAdvert.getOrderMsg(), c2cAdvert.getRemark(), c2cAdvert.getCreateTime(), c2cAdvert.getUpdateTime(),
                oldC2cUserDeposit, c2cUser.getDeposit());
        User user = secUserService.getById(c2cUser.getC2cUserPartyId());
        this.saveLog(user, userNameLogin, log, Constants.LOG_CATEGORY_C2C);
        return Result.succeed();
    }

    /**
     * 退还 C2C广告 所有保证金
     */
    @PostMapping( "backAllDeposit")
    @ApiOperation("退还 C2C广告 所有保证金")
    public Result backAllDeposit(HttpServletRequest request) {

        String id = request.getParameter("id");
        String login_safeword = request.getParameter("login_safeword");
        if (StringUtils.isEmptyString(id)) {
            throw new YamiShopBindException("广告id不正确");
        }
        if (StringUtils.isEmptyString(login_safeword)) {
            throw new YamiShopBindException("资金密码错误");
        }
        String userNameLogin = SecurityUtils.getSysUser().getUsername();
        sysUserService.checkSafeWord(login_safeword);
        C2cAdvert c2cAdvert = this.c2cAdvertService.getById(id);
        if (null == c2cAdvert) {
            throw new YamiShopBindException("广告不存在");
        }
        C2cUser c2cUser = this.c2cUserService.getById(c2cAdvert.getC2cUserId());
        if (null == c2cUser) {
            throw new YamiShopBindException("承兑商不存在");
        }
        Long orderCount = this.c2cOrderService.findNoEndingOrdersCountByAdvertId(id);
        if (orderCount > 0) {
            throw new YamiShopBindException("广告还有未完结订单，不能退还");
        }
        Long appealCount = this.c2cAppealService.findNoHandleAppealsCountByAdvertId(id);
        if (null == appealCount) {
            throw new YamiShopBindException("广告还有未处理的订单申诉，不能退还");
        }
        DecimalFormat df = new DecimalFormat("#.########");
        double oldC2cUserDeposit = c2cUser.getDeposit();
        if (c2cAdvert.getDeposit() > 0) {
            // 退还保证金
            double depositTotal = Arith.add(c2cUser.getDeposit(), c2cAdvert.getDeposit());
            c2cUser.setDeposit(Double.valueOf(df.format(depositTotal)).doubleValue());
            this.c2cUserService.updateById(c2cUser);
            c2cAdvert.setCoinAmount(0);
            c2cAdvert.setDeposit(0);
        }
        // 广告下架
        c2cAdvert.setOnSale(0);
        this.c2cAdvertService.updateById(c2cAdvert);
        String log = MessageFormat.format("ip:" + IPHelper.getIpAddr()
                        + ",管理员退还承兑商广告所有保证金,id:{0},承兑商ID:{1},买卖方式:{2},支付币种:{3},上架币种:{4},上架币种实时行情价:{5},支付比率:{6},支付方式:{7},币种单价:{8},"
                        + "币种数量:{9},单笔订单最低限额:{10},单笔订单最高限额:{11},剩余保证金:{12},保证金:{13},是否上架:{14},是否关闭:{15},"
                        + "排序索引:{16},支付时效:{17},交易条款:{18},订单自动消息:{19},备注:{20},创建时间:{21},更新时间:{22}#####原承兑商剩余保证金:{23},新承兑商剩余保证金:{24}",
                c2cAdvert.getUuid(), c2cAdvert.getC2cUserId(), c2cAdvert.getDirection(), c2cAdvert.getCurrency(), c2cAdvert.getSymbol(), c2cAdvert.getSymbolClose(), c2cAdvert.getPayRate(), c2cAdvert.getPayType(), c2cAdvert.getSymbolValue(),
                c2cAdvert.getCoinAmount(), c2cAdvert.getInvestmentMin(), c2cAdvert.getInvestmentMax(), c2cAdvert.getDeposit(), c2cAdvert.getDepositOpen(), c2cAdvert.getOnSale(), c2cAdvert.getClosed(),
                c2cAdvert.getSortIndex(), c2cAdvert.getExpireTime(), c2cAdvert.getTransactionTerms(), c2cAdvert.getOrderMsg(), c2cAdvert.getRemark(), c2cAdvert.getCreateTime(), c2cAdvert.getUpdateTime(),
                oldC2cUserDeposit, c2cUser.getDeposit());
        User user = secUserService.getById(c2cUser.getC2cUserPartyId());
        this.saveLog(user, userNameLogin, log, Constants.LOG_CATEGORY_C2C);
        return Result.succeed();
    }

    /**
     * 获取 承兑商 剩余保证金
     */
    @GetMapping("getC2cUserDeposit")
    @ApiOperation("获取 承兑商 剩余保证金")
    public Result getC2cUserDeposit( @Valid C2cUserDepositModel model) {

        String c2c_user_code = model.getC2cUserCode();
        String advert_id =model.getAdvertId();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        C2cUser c2cUser = this.c2cUserService.findByUsercode(c2c_user_code);
        if (null == c2cUser) {
            throw new YamiShopBindException("承兑商不存在");
        }
        double advertDeposit = 0;
        if (StringUtils.isNotEmpty(advert_id)) {
            C2cAdvert c2cAdvert = this.c2cAdvertService.getById(advert_id);
            if (null != c2cAdvert) {
                advertDeposit = c2cAdvert.getDeposit();
            }
        }
        List<C2cPaymentMethod> methodList = this.c2cPaymentMethodService.getMethodConfigListByPartyId(c2cUser.getC2cUserPartyId());
        resultMap.put("code", 200);
        resultMap.put("all_deposit", c2cUser.getDeposit());
        resultMap.put("deposit_total", Arith.add(c2cUser.getDeposit(), advertDeposit));
        resultMap.put("paymentMethodList", methodList);
        return Result.succeed(resultMap);
    }

    /**
     * 获取 交易币种数量，币种单价
     */
    @GetMapping("compute")
    @ApiOperation("获取 交易币种数量，币种单价")
    public Result compute( @Valid C2ccComputeModel model) {

        String deposit_total =model.getDepositTotal();
        String currency = model.getCurrency();
        String symbol =model.getSymbol();
        String coin_amount =model.getCoinAmount();
        String symbol_value = model.getSymbolValue();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> computeValue = new HashMap<String, Object>();
        String error = verifCompute(currency, symbol, coin_amount, symbol_value);
        if (!StringUtils.isNullOrEmpty(error)) {
            throw new YamiShopBindException(error);
        }
        if (StringUtils.isEmptyString(deposit_total) || !StringUtils.isDouble(deposit_total) || Double.valueOf(deposit_total).doubleValue() <= 0) {
            computeValue.put("pay_rate", 100);
            computeValue.put("deposit_open", 0);
            computeValue.put("all_deposit", deposit_total);
            computeValue.put("symbol_close", 0);
            computeValue.put("price", 0);
            computeValue.put("coin_amount_max", 0);
            computeValue.put("investment_min_limit", 0);
            computeValue.put("investment_max_limit", 0);
            resultMap.put("code", 200);
            resultMap.putAll(computeValue);
            return Result.succeed(resultMap);
        }
        double deposit_total_double = Double.valueOf(deposit_total).doubleValue();
        if (StringUtils.isEmptyString(coin_amount) || !StringUtils.isDouble(coin_amount) || Double.valueOf(coin_amount).doubleValue() <= 0) {
            computeValue = c2cAdvertService.getComputeValue(deposit_total_double, currency, symbol, 0, 1);
            computeValue.put("pay_rate", 100);
            computeValue.put("deposit_open", 0);
            computeValue.put("all_deposit", deposit_total);
            computeValue.put("investment_min_limit", 0);
            computeValue.put("investment_max_limit", 0);
            resultMap.put("code", 200);
            resultMap.putAll(computeValue);
            return Result.succeed(resultMap);
        }
        double coin_amount_double = Double.valueOf(coin_amount).doubleValue();
        if (StringUtils.isEmptyString(symbol_value) || !StringUtils.isDouble(symbol_value) || Double.valueOf(symbol_value).doubleValue() <= 0) {
            computeValue = this.c2cAdvertService.getComputeValue(deposit_total_double, currency, symbol, coin_amount_double, 1);
            computeValue.put("pay_rate", 100);
            computeValue.put("deposit_open", 0);
            computeValue.put("all_deposit", deposit_total);
            computeValue.put("investment_min_limit", 0);
            computeValue.put("investment_max_limit", 0);
            resultMap.put("code", 200);
            resultMap.putAll(computeValue);
            return Result.succeed(resultMap);
        }
        double symbol_value_double = Double.valueOf(symbol_value).doubleValue();
        computeValue = this.c2cAdvertService.getComputeValue(deposit_total_double, currency, symbol, coin_amount_double, symbol_value_double);
        resultMap.put("code", 200);
        resultMap.putAll(computeValue);
        return Result.succeed(resultMap);
    }

    private String verifCompute(String currency, String symbol, String coin_amount, String symbol_value) {

        Map<String, String> currencyMap = this.c2cAdvertService.getCurrencyMap();
        Map<String, String> symbolMap = this.c2cAdvertService.getSymbolMap();
        if (null == currencyMap || !currencyMap.containsKey(currency)) {
            return "支付币种不正确";
        }
        if (null == symbolMap || !symbolMap.containsKey(symbol)) {
            return "上架币种不正确";
        }
//		if (StringUtils.isEmptyString(coin_amount) || !StringUtils.isDouble(coin_amount) || Double.valueOf(coin_amount).doubleValue() <= 0) {
//			return "交易币种数量未填或格式不正确";
//		}
//		if (StringUtils.isEmptyString(symbol_value) || !StringUtils.isDouble(symbol_value) || Double.valueOf(symbol_value).doubleValue() <= 0) {
//			return "币种单价未填或格式不正确";
//		}
        return null;
    }

    private String verif(String c2c_user_code, String direction, String pay_type, String currency, String symbol, String coin_amount, String symbol_value,
                         String investment_min, String investment_max, String on_sale, String sort_index, String expire_time, String login_safeword) {

        this.verifCompute(currency, symbol, coin_amount, symbol_value);
        if (StringUtils.isEmptyString(c2c_user_code)) {
            return "承兑商UID为空";
        }
        if (StringUtils.isEmptyString(direction) || !Arrays.asList("buy", "sell").contains(direction)) {
            return "买卖方式不正确";
        }
        if (StringUtils.isEmptyString(pay_type)) {
            return "请选择支付方式";
        }
        if (StringUtils.isEmptyString(investment_min) || !StringUtils.isDouble(investment_min) || Double.valueOf(investment_min).doubleValue() < 0) {
            return "单笔订单最低限额未填或格式不正确";
        }
        if (StringUtils.isEmptyString(investment_max) || !StringUtils.isDouble(investment_max) || Double.valueOf(investment_max).doubleValue() < 0) {
            return "单笔订单最高限额未填或格式不正确";
        }
        if (Double.valueOf(investment_max).doubleValue() < Double.valueOf(investment_min).doubleValue()) {
            return "单笔订单上限金额不能小于下限金额";
        }
        if (StringUtils.isEmptyString(on_sale) || !Arrays.asList("0", "1").contains(on_sale)) {
            return "是否上架未填或格式不正确";
        }
//		if (StringUtils.isEmptyString(sort_index) || !StringUtils.isInteger(sort_index) || Integer.valueOf(sort_index).intValue() < 0) {
//			return "排序索引未填或格式不正确";
//		}
        if (StringUtils.isEmptyString(expire_time) || !StringUtils.isInteger(expire_time) || Integer.valueOf(expire_time).intValue() < 0) {
            return "支付时效未填或格式不正确";
        }
        if (StringUtils.isEmptyString(login_safeword)) {
            return "资金密码错误";
        }
        return null;
    }

    /**
     * 验证登录人资金密码
     */
//    protected void checkLoginSafeword(SecUser secUser, String operatorUsername, String loginSafeword) {
////		SecUser sec = this.secUserService.findUserByLoginName(operatorUsername);
//        String sysSafeword = secUser.getSafeword();
//        String safeword_md5 = this.passwordEncoder.encodePassword(loginSafeword, operatorUsername);
//        if (!safeword_md5.equals(sysSafeword)) {
//            throw new BusinessException("登录人资金密码错误");
//        }
//    }
    public void saveLog(User secUser, String operator, String context, String category) {

        Log log = new Log();
        log.setCategory(category);
        log.setOperator(operator);
        log.setUsername(secUser.getUserName());
        log.setUserId(secUser.getUserId());
        log.setLog(context);
        log.setCreateTime(new Date());
        logService.save(log);
    }

}
