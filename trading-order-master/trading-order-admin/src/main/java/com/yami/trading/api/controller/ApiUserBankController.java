package com.yami.trading.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yami.trading.api.model.AddUserBankModel;
import com.yami.trading.bean.model.*;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.dao.c2c.UserBankMapper;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.WalletService;
import com.yami.trading.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

@RestController
@RequestMapping("api/userBank")
@Api(tags = "用户提现银行卡信息")
public class ApiUserBankController {
    @Autowired
    WalletService walletService;
    @Autowired
    UserService userService;
    @Autowired
    UserBankMapper userBankMapper;

    @GetMapping("/getBank")
    @ApiOperation(value = "获取余额")
    public Result getUsdt() {
        //  余额
        Map<String, Object> data = new HashMap<String, Object>();
        String partyId = SecurityUtils.getUser().getUserId();
        DecimalFormat df2 = new DecimalFormat("#.##");
        // 向下取整
        df2.setRoundingMode(RoundingMode.FLOOR);
        Wallet wallet = new Wallet();
        if (!"".equals(partyId) && partyId != null) {
            wallet = walletService.saveWalletByPartyId(partyId);
        }
        double money = wallet.getMoney().doubleValue();
        // 账户剩余资金
        data.put("money", df2.format(money));
        return Result.succeed(data);
    }

    /**
     * 获取 支付方式
     */
    @RequestMapping("myBank")
    @ApiOperation("获取我的 支付方式 列表")
    public Result<List<UserBank>> myBankUser() {
        String partyId = SecurityUtils.getUser().getUserId();
        User user = userService.getById(partyId);
        List<UserBank> dbBank =this.userBankMapper.selectList(new QueryWrapper<UserBank>().eq("user_id", user.getUserId()));
        return Result.succeed(dbBank);
    }


    @ApiOperation(value = "新增收款方式")
    @RequestMapping("add")
    public Result<?> add(@Valid AddUserBankModel model) {
        String partyId = SecurityUtils.getUser().getUserId();
        User user = userService.getById(partyId);
        LambdaQueryWrapper<UserBank> queryWrapper = new LambdaQueryWrapper<UserBank>()
                .eq(UserBank::getUserId,  user.getUserId());
        UserBank userBanks = userBankMapper.selectOne(queryWrapper);
        if(userBanks!=null){
            throw  new YamiShopBindException("The user's bank card number already exists");
        }
        UserBank userBank = new UserBank();
        userBank.setUserId(user.getUserId());
        userBank.setBankName(model.getBankName());
        userBank.setBankNo(model.getBankNo());
        userBank.setBankAddress(model.getBankAddress());
        userBank.setBankImg(model.getBankImg());
        userBank.setBankPhone(model.getBankPhone());
        userBank.setUserName(model.getUserName());
        userBank.setMethodName(model.getMethodName());
        userBank.setCreateTime(new Date());
        int insertCount = this.userBankMapper.insert(userBank);
        if (insertCount > 0) {
            return Result.succeed("添加成功");
        }else{
            throw  new YamiShopBindException("添加失败");
        }
    }

    @ApiOperation(value = "修改收款方式")
    @RequestMapping("update")
    public Result<?> update(@Valid AddUserBankModel model) {
        UserBank userBank = new UserBank();
        userBank.setUuid(model.getUUID());
        userBank.setBankName(model.getBankName());
        userBank.setBankNo(model.getBankNo());
        userBank.setBankAddress(model.getBankAddress());
        userBank.setBankImg(model.getBankImg());
        userBank.setBankPhone(model.getBankPhone());
        userBank.setUserName(model.getUserName());
        userBank.setMethodName(model.getMethodName());
        userBank.setCreateTime(new Date());
        int insertCount = this.userBankMapper.updateById(userBank);
        if (insertCount > 0) {
            return Result.succeed("更新成功");
        }else{
            throw  new YamiShopBindException("更新失败");
        }
    }
}
