package com.yami.trading.admin.controller.user;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.facade.PermissionFacade;
import com.yami.trading.admin.model.*;
import com.yami.trading.bean.agent.AgentDto;
import com.yami.trading.bean.c2c.dto.C2cPaymentMethodDto;
import com.yami.trading.bean.exchange.dto.SumEtfDto;
import com.yami.trading.bean.model.Log;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.model.UserRecom;
import com.yami.trading.bean.model.Wallet;
import com.yami.trading.bean.user.dto.UserBasicDto;
import com.yami.trading.bean.user.dto.UserDto;
import com.yami.trading.common.annotation.SysLog;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.IPHelper;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.security.common.enums.SysTypeEnum;
import com.yami.trading.security.common.manager.TokenStore;
import com.yami.trading.service.WalletService;
import com.yami.trading.service.agent.AgentService;
import com.yami.trading.service.exchange.ExchangeApplyOrderService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.service.user.UserRecomService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.service.user.UserStatisticsService;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("user")
@Api(tags = "用户管理")
public class UserController {
    @Autowired
    private TokenStore tokenStore;

    @Autowired
    UserService userService;

    @Autowired
    SysUserService sysUserService;

    @Autowired
    LogService logService;

    @Autowired
    WalletService walletService;

    @Autowired
    UserRecomService userRecomService;
    @Autowired
    AgentService agentService;
    @Autowired
    UserStatisticsService userStatisticsService;

    @Autowired
    PermissionFacade permissionFacade;
    @Autowired
    private ExchangeApplyOrderService exchangeApplyOrderService;

    @PostMapping("list")
    @ApiOperation("列表")
    public Result<Page<UserDto>> list(@Valid @RequestBody UserListModel model) {
        List<String> roleNames = new ArrayList<>();
        if (StrUtil.isEmpty(model.getRolename())) {
            roleNames.add(Constants.SECURITY_ROLE_GUEST);
            roleNames.add(Constants.SECURITY_ROLE_MEMBER);
            roleNames.add(Constants.SECURITY_ROLE_TEST);
        } else {
            roleNames.add(model.getRolename());
        }
        SysUser sysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());

        Page<AgentDto> pages = new Page(1, 5);
        pages = agentService.listTotal(pages, sysUser.getUsername());
        List<AgentDto> lists = pages.getRecords();

        Page<UserDto> page =null;
        if (lists.size() > 0) {//代理商
            page = new Page(model.getCurrent(), model.getSize());
            Page<UserDto> userDtoPage = userService.listUser(page, roleNames, model.getUserCode(), model.getUserName(), permissionFacade.getOwnerUserIds());
            for (UserDto dto : userDtoPage.getRecords()) {
                //SumEtfDto sumEtfDto = exchangeApplyOrderService.getProfitLossByUserId(dto.getUserId(), "YD-stocks");
               // dto.setProfitLoss(sumEtfDto.getProfitLoss());
                dto.setDeflag("2");
            }
        } else {
            page = new Page(model.getCurrent(), model.getSize());
            Page<UserDto> userDtoPage = userService.listUser(page, roleNames, model.getUserCode(), model.getUserName(), permissionFacade.getOwnerUserIds());
            for (UserDto dto : userDtoPage.getRecords()) {
               // SumEtfDto sumEtfDto = exchangeApplyOrderService.getProfitLossByUserId(dto.getUserId(), "YD-stocks");
               // dto.setProfitLoss(sumEtfDto.getProfitLoss());
                dto.setDeflag("1");
            }
        }
        return Result.ok(page);
    }

    @GetMapping("asset/{id}")
    @ApiOperation("用户资产")
    public Result<?> asset(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getById(id);
        Wallet wallet = walletService.getOne(Wrappers.<Wallet>lambdaQuery().eq(Wallet::getUserId, id));
        result.put("walletMone", wallet.getMoney());
        return Result.ok(result);
    }



    @ApiOperation(value = "修改账户余额")
    @PostMapping("updateWallt")
    @SysLog("修改账户余额")
    public Result updateWallt(@Valid @RequestBody UpdateWalltModel model) {
        sysUserService.checkSafeWord(model.getSafePassword());
        userService.updateWallt(model.getUserId(), model.getMoneyRevise(),
                model.getAccountType(), model.getCoinType());
        return Result.ok(null);
    }

    @ApiOperation(value = "修改提现限制流水")
    @PostMapping("updateWithdrawalLimitFlow")
    @SysLog("修改提现限制流水")
    public Result updateWithdrawalLimitFlow(@Valid @RequestBody WithdrawalLimitFlowModel model) {

        userService.updateWithdrawalLimitFlow(model.getUserId(), model.getMoneyWithdraw());
        return Result.ok(null);
    }

    @ApiOperation(value = "重置登录密码")
    @PostMapping("restLoginPasswrod")
    @SysLog("重置登录密码")
    public Result restLoginPasswrod(@Valid @RequestBody RestLoginPasswrodModel model) {
       // sysUserService.checkGooleAuthCode(Long.valueOf(model.getGoogleAuthCode()));
       // sysUserService.checkSafeWord(model.getLoginSafeword());
        userService.restLoginPasswrod(model.getUserId(),  model.getPassword());
        return Result.ok(null);
    }

    @ApiOperation(value = "解绑用户谷歌验证器")
    @PostMapping("deleteGooleAuthCode")
    @SysLog("解绑用户谷歌验证器")
    public Result deleteGooleAuthCode(@Valid @RequestBody DeleteGooleAuthCodeModel model) {

        userService.deleteGooleAuthCode(model.getUserId(), model.getGoogleAuthCode(), model.getLoginSafeword());
        return Result.ok(null);
    }

    @ApiOperation(value = "重置资金密码")
    @PostMapping("restSafePassword")
    @SysLog("重置资金密码")
    public Result restSafePassword(@Valid @RequestBody RestSafePasswordModel model) {
        sysUserService.checkGooleAuthCode(Long.valueOf(model.getGoogleAuthCode()));
        sysUserService.checkSafeWord(model.getLoginSafeword());
        userService.restSafePassword(model.getUserId(), model.getNewSafeword());
        return Result.ok(null);
    }

    /**
     * 退出用户登录状态
     */
    @PostMapping(value = "resetUserLoginState")
    @ApiOperation(value = "强制用户退出登录状态")
    public Result resetUserLoginState(@Valid @RequestBody ResetUserLoginStateModel model) {
        try {
            sysUserService.checkGooleAuthCode(Long.valueOf(model.getGoogleAuthCode()));
            sysUserService.checkSafeWord(model.getLoginSafeword());
            tokenStore.deleteAllToken(String.valueOf(SysTypeEnum.ORDINARY.value()), String.valueOf(model.getUserId()));
            Log log = new Log();
            log.setCategory(Constants.LOG_CATEGORY_OPERATION);
            log.setUsername(SecurityUtils.getSysUser().getUsername());
            log.setOperator(SecurityUtils.getSysUser().getUsername());
            log.setLog("管理员手动退出用户登录状态,ip:[" + IPHelper.getIpAddr() + "]");
            logService.save(log);
        } catch (Exception e) {
            //throw  new YamiShopBindException("操作失败!");
        }
        return Result.ok(null);
    }

    /**
     * 查询用户基本信息
     * @param uid
     * @return
     */
    @GetMapping("/{uid}")
    @ApiOperation("详情")
    public Result<List<UserBasicDto>> detail(@PathVariable String uid) {
        Set<String> ids = new HashSet<>();
        User eUser = userService.findUserByUserCode(uid);
        if(null == eUser) {
            return Result.failed("用户不存在");
        }
        List<String> childrenIds = userRecomService.findChildren(eUser.getUserId());
        List<UserRecom> parents = userRecomService.getParents(eUser.getUserId());
        ids.add(eUser.getUserId());
        if(childrenIds.size() > 0) ids.addAll(childrenIds);
        if(parents.size() > 0) ids.addAll(parents.stream().map(UserRecom::getUserId).collect(Collectors.toList()));

        List<User> users = userService.list(Wrappers.<User>lambdaQuery().in(User::getUserId, ids).orderByAsc(User::getUserLevel));
        List<UserBasicDto> userBasicDtos = users.stream().map(user -> {
            UserBasicDto userBasicDto = new UserBasicDto();
            userBasicDto.setUserId(user.getUserId());
            userBasicDto.setUserLevel(user.getUserLevel());
            userBasicDto.setUserName(user.getUserName());
            userBasicDto.setUid(user.getUserCode());
            userBasicDto.setAccountType(Constants.ROLE_MAP.get(user.getRoleName()));
            userBasicDto.setRealNameAuthority(user.isRealNameAuthority());
            return userBasicDto;
        }).collect(Collectors.toList());

        return Result.ok(userBasicDtos);
    }



    @GetMapping("/assetsAll")
    @ApiOperation("用户资产 登录者只能看自己下面的用户资产")
    public  Result assetsAll(@RequestParam  String userId){
     User user=   userService.findUserByUserCode(userId);
        List<Map<String, Object>> asset_data =   userStatisticsService.getAssetsAll(SecurityUtils.getCurrentSysUserId(),user.getUserId());
        return  Result.succeed(asset_data);
    }


}





