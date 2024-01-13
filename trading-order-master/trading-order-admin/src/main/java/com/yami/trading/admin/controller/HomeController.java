package com.yami.trading.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.dto.HomeViewDto;
import com.yami.trading.admin.dto.WaitCountDto;
import com.yami.trading.bean.agent.AgentDto;
import com.yami.trading.bean.cms.Infomation;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.user.dto.UserBenefitsDto;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.util.DateUtil;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.*;
import com.yami.trading.service.agent.AgentService;
import com.yami.trading.service.user.UserDataService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("home")
@Api(tags = "首页")
@Slf4j
public class HomeController {

    @Autowired
    UserService userService;

    @Autowired
    UserDataService userDataService;

    @Autowired
    WalletService walletService;

    @Autowired
    RealNameAuthRecordService realNameAuthRecordService;

    @Autowired
    HighLevelAuthRecordService highLevelAuthRecordService;

    @Autowired
    RechargeBlockchainOrderService rechargeBlockchainOrderService;
    @Autowired
    SysUserService sysUserService;
    @Autowired
    WithdrawService withdrawService;

    @Autowired
    AgentService agentService;
    @ApiOperation(value = "今日数据统计")
    @PostMapping("view")
    public Result<HomeViewDto> view(){
        HomeViewDto homeViewDto=new HomeViewDto();

        Date date=new Date();
        log.info(DateUtil.formatDate(date,"yyyy-MM-dd HH:mm:ss"));

        log.info(DateUtil.formatDate(DateUtil.minDate(date),"yyyy-MM-dd HH:mm:ss"));
        log.info(DateUtil.formatDate(DateUtil.maxDate(date),"yyyy-MM-dd HH:mm:ss"));
        UserBenefitsDto userBenefitsDto= null;
        SysUser sysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        Page<AgentDto> pages = new Page(1, 5);
        pages = agentService.listTotal(pages, sysUser.getUsername());
        List<AgentDto> lists = pages.getRecords();
        if (lists.size()>0) {//代理商
            String userCode = lists.get(0).getUserCode();
            homeViewDto.setTodayUserCount(userService.countToDays(userCode));
            QueryWrapper<User> itemWrapper = new QueryWrapper<>();
            itemWrapper.eq("recom_code", userCode);
            homeViewDto.setAllUserCount( userService.count(itemWrapper));
            homeViewDto.setSumUsdtAmount(walletService.sumMoneyAgent(userCode));//钱包
            userBenefitsDto= userDataService.daySumDatas(DateUtil.minDate(date),DateUtil.maxDate(date),null,userCode);
        }
        else{
             userBenefitsDto= userDataService.daySumData(DateUtil.minDate(date),DateUtil.maxDate(date),null);
            homeViewDto.setTodayUserCount(userService.countToDay());
            homeViewDto.setAllUserCount(userService.count());
            homeViewDto.setSumUsdtAmount(walletService.sumMoney());//钱包
        }
        homeViewDto.setTodayRechargeUserCount(userDataService.countTodayRechargeUser());
        if (userBenefitsDto!=null){
            homeViewDto.setRecharge(userBenefitsDto.getRechargeUsdt());
            homeViewDto.setWithdraw(userBenefitsDto.getWithdraw());
            homeViewDto.setBalanceAmount(userBenefitsDto.getWithdraw().subtract(userBenefitsDto.getRechargeUsdt()));
            homeViewDto.setTotleIncome(userBenefitsDto.getTotleIncome());
        }
        return  Result.ok(homeViewDto);
    }

    @ApiOperation(value = "待处理统计数据")
    @PostMapping("waitCount")
    public Result<WaitCountDto> waitCount(){
        SysUser sysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        if(sysUser.getUsername().equals("admin")){
            WaitCountDto waitCountDto=new WaitCountDto();
            waitCountDto.setHighLevelAuthCount(highLevelAuthRecordService.waitCount());
            waitCountDto.setRealNameAuthCount(realNameAuthRecordService.waitCount());
            waitCountDto.setWithdrawCount(withdrawService.waitCount());
            waitCountDto.setRechargeCount(rechargeBlockchainOrderService.waitCount());
            return Result.ok(waitCountDto);
        }else{
            WaitCountDto waitCountDto=new WaitCountDto();
            waitCountDto.setHighLevelAuthCount(0);
            waitCountDto.setRealNameAuthCount(0);
            waitCountDto.setWithdrawCount(0);
            waitCountDto.setRechargeCount(0);
            return  Result.ok(waitCountDto);
        }
    }

}
