package com.yami.trading.admin.controller.exchange;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.controller.exchange.model.CloseModel;
import com.yami.trading.admin.controller.exchange.model.ResetFreezeModel;
import com.yami.trading.admin.controller.exchange.model.ResetLockModel;
import com.yami.trading.admin.controller.exchange.model.SuccessModel;
import com.yami.trading.admin.facade.PermissionFacade;
import com.yami.trading.admin.model.exchange.ExchangeApplyOrderListModel;
import com.yami.trading.bean.agent.AgentDto;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.exchange.ExchangeApplyOrder;
import com.yami.trading.bean.exchange.dto.ExchangeApplyOrderDto;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.item.dto.RealtimeDTO;
import com.yami.trading.bean.model.Log;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.user.dto.UserDataDto;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.IPHelper;
import com.yami.trading.common.util.LockFilter;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.agent.AgentService;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.exchange.ExchangeApplyOrderService;
import com.yami.trading.service.exchange.job.ExchangeApplyOrderHandleJobService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("exchangeApplyOrder")
@Api(tags = "股票交易")
public class ExchangeApplyOrderController {
    @Autowired
    ExchangeApplyOrderService exchangeApplyOrderService;
    @Autowired
    SysUserService sysUserService;
    @Autowired
    ExchangeApplyOrderHandleJobService exchangeApplyOrderHandleJobService;
    @Autowired
    UserService userService;
    @Autowired
    LogService logService;
    @Autowired
    ItemService itemService;
    @Autowired
    private PermissionFacade permissionFacade;
    @Autowired
    AgentService agentService;
    @Autowired
    RedisTemplate redisTemplate;
    /**
     * 股票交易订单 列表查询
     */
    @ApiModelProperty("股票交易订单 列表查询")
    @PostMapping("list")
    public Result<Page<ExchangeApplyOrderDto>> list(@RequestBody @Valid ExchangeApplyOrderListModel model) {
        SysUser sysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        Page<AgentDto> pages = new Page(model.getCurrent(), model.getSize());
        pages = agentService.listTotal(pages, sysUser.getUsername());
        List<AgentDto> lists = pages.getRecords();
        Page<ExchangeApplyOrderDto> page = null;
        if (lists.size() > 0) {//代理商
            String userCode = lists.get(0).getUserCode();
            page = new Page(model.getCurrent(), model.getSize());
            exchangeApplyOrderService.listPages(page, model.getRolename(),
                    model.getUserName(), model.getOrderNo(), model.getState(), model.getOffset(), model.getType(), model.getUserCode(), model.getSymbol(),userCode);
            List<ExchangeApplyOrderDto> list = page.getRecords();
            for (int i = 0; i < list.size(); i++) {
                ExchangeApplyOrderDto dto = list.get(i);
                if (StringUtils.isNotEmpty(dto.getRoleName())) {
                    dto.setRoleName(Constants.ROLE_MAP.containsKey(dto.getRoleName()) ? Constants.ROLE_MAP.get(dto.getRoleName()) : dto.getRoleName());
                    Item bySymbol = itemService.findBySymbol(dto.getSymbol());
                    if (bySymbol != null) {
                        dto.setSymbolName(bySymbol.getName());
                    }
                }
            }
        } else {
            page = new Page(model.getCurrent(), model.getSize());
            exchangeApplyOrderService.listPage(page, model.getRolename(),
                    model.getUserName(), model.getOrderNo(), model.getState(), model.getOffset(), model.getType(), model.getUserCode(), model.getSymbol());
            List<ExchangeApplyOrderDto> list = page.getRecords();
            for (int i = 0; i < list.size(); i++) {
                ExchangeApplyOrderDto dto = list.get(i);
                if (StringUtils.isNotEmpty(dto.getRoleName())) {
                    dto.setRoleName(Constants.ROLE_MAP.containsKey(dto.getRoleName()) ? Constants.ROLE_MAP.get(dto.getRoleName()) : dto.getRoleName());
                    Item bySymbol = itemService.findBySymbol(dto.getSymbol());
                    if (bySymbol != null) {
                        dto.setSymbolName(bySymbol.getName());
                    }
                }
            }
        }
        return Result.succeed(page);
    }

    /**
     * 股票交易订单 详情查询
     */
    @ApiModelProperty("股票交易订单 详情查询")
    @GetMapping("/{id}")
    public Result<ExchangeApplyOrder> getById(@PathVariable String id) {
        ExchangeApplyOrder exchangeApplyOrder = exchangeApplyOrderService.getById(id);
//        Item bySymbol = itemService.findBySymbol(exchangeApplyOrder.getSymbol());
//        if (bySymbol != null) {
//            exchangeApplyOrderDto.setSymbolName(bySymbol.getName());
//        }
        return Result.succeed(exchangeApplyOrder);
    }

    /**
     * 平仓或撤单
     */
    @PostMapping("close")
    @ApiOperation("平仓或撤单")
    public Result close(@RequestBody @Valid CloseModel model) {
        ExchangeApplyOrder order = exchangeApplyOrderService.findByOrderNo(model.getOrderNo());
        if (ExchangeApplyOrder.STATE_CREATED.equals(order.getState())) {
            throw new YamiShopBindException("委托已完成无法撤销");
        }
        exchangeApplyOrderService.saveCancel(order.getPartyId(), model.getOrderNo());
        return Result.succeed();
    }

    /**
     * 限价成交
     */
    @PostMapping("success")
    @ApiOperation("限价成交")
    public Result success(@RequestBody SuccessModel successModel) {
        sysUserService.checkSafeWord(successModel.getLoginSafeword());
        ExchangeApplyOrder order = exchangeApplyOrderService.findByOrderNo(successModel.getOrderNo());
        if (order == null) {
            throw new YamiShopBindException("委托单不存在或请稍后再试");
        }
        if (ExchangeApplyOrder.STATE_CREATED.equals(order.getState()))
            throw new YamiShopBindException("委托已完成无法操作");
        if (!"limit".equals(order.getOrderPriceType())) {
            throw new YamiShopBindException("委托并非限价单，无法限价成交");
        }
        RealtimeDTO realtime = new RealtimeDTO();
        realtime.setLast(String.valueOf(order.getPrice()));
        exchangeApplyOrderHandleJobService.handles(order, realtime);
        User party = userService.getById(order.getPartyId());
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_OPERATION);
        log.setExtra(order.getOrderNo());
        log.setOperator(SecurityUtils.getSysUser().getUsername());
        log.setUsername(party.getUserName());
        log.setUserId(order.getPartyId());
        log.setLog("股票限价单，操作限价成交。订单号[" + order.getOrderNo() + "]。");
        logService.save(log);
        return Result.succeed();
    }


    private String verificationResetLock(String money_revise, String reset_type, String coin_type, String login_safeword) {
        if (StringUtils.isNullOrEmpty(money_revise)) {
            throw new YamiShopBindException("账变金额必填");
        }
        if (!StringUtils.isDouble(money_revise)) {
            throw new YamiShopBindException("账变金额输入错误，请输入浮点数");
        }
        if (Double.valueOf(money_revise).doubleValue() <= 0) {
            throw new YamiShopBindException("账变金额不能小于等于0");
        }
        if (StringUtils.isNullOrEmpty(login_safeword)) {
            throw new YamiShopBindException("请输入资金密码");
        }
        if (StringUtils.isNullOrEmpty(reset_type)) {
            throw new YamiShopBindException("请选择转移方向");
        }
        if (StringUtils.isNullOrEmpty(coin_type)) {
            throw new YamiShopBindException("请选择转移币种");
        }
        return null;
    }

    /**
     * 交易所 转移账户冻结金额
     */
    @PostMapping(value = "resetFreeze")
    @ApiOperation(("转移账户冻结金额"))
    public Result<Object> resetFreeze(@RequestBody ResetFreezeModel model) {

        boolean lock = false;
        try {

            String error = this.verificationResetLock(model.getMoneyRevise(), model.getResetType(), model.getCoinType(), model.getLoginSafeword());
            if (!StringUtils.isNullOrEmpty(error)) {
                throw new YamiShopBindException(error);
            }
            sysUserService.checkSafeWord(model.getLoginSafeword());
            if (!LockFilter.add(model.getId())) {
                throw new YamiShopBindException("请稍后再试");
            }
            lock = true;
            double money_revise = Double.valueOf(model.getMoneyRevise()).doubleValue();
            userService.saveResetLock(model.getId(), money_revise, model.getLoginSafeword(),
                    SecurityUtils.getSysUser().getUsername(), model.getResetType(), IPHelper.getIpAddr(), model.getCoinType());
            ThreadUtils.sleep(300);
        } catch (YamiShopBindException e) {
            throw new YamiShopBindException(e.getMessage());
        } catch (Throwable t) {
            throw new YamiShopBindException("程序错误");
        } finally {
            if (lock) {
                LockFilter.remove(model.getId());
            }
        }
        return Result.succeed();

    }


    /**
     * 交易所 减少账户锁定金额(root)
     */
    @RequestMapping("resetLock")
    @ApiOperation("减少账户锁定金额(root)")
    public Result<Object> resetLock(ResetLockModel lockModel) {
        boolean lock = false;
        try {
            String error = this.verificationResetLock(lockModel.getMoneyRevise(),
                    lockModel.getResetType(), lockModel.getCoinType(), lockModel.getLoginSafeword());
            if (!StringUtils.isNullOrEmpty(error)) {
                throw new YamiShopBindException(error);
            }
            sysUserService.checkSafeWord(lockModel.getLoginSafeword());
            if (!LockFilter.add(lockModel.getId())) {
                throw new YamiShopBindException("请稍后再试");
            }
            lock = true;
            double money_revise = Double.valueOf(lockModel.getMoneyRevise()).doubleValue();

            userService.saveResetLock(lockModel.getId(),
                    money_revise, lockModel.getLoginSafeword(), SecurityUtils.getSysUser().getUsername(), "addLock", IPHelper.getIpAddr(), lockModel.getCoinType());

            ThreadUtils.sleep(300);

        } catch (YamiShopBindException e) {
            e.printStackTrace();
            throw new YamiShopBindException(e.getMessage());
        } finally {
            if (lock) {
                LockFilter.remove(lockModel.getId());
            }
        }

        return Result.succeed();

    }
}
