
package com.yami.trading.admin.controller.user;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.controller.service.SysUserOperService;
import com.yami.trading.admin.controller.user.model.UserDataAddModel;
import com.yami.trading.admin.facade.PermissionFacade;
import com.yami.trading.admin.model.UpdateUserModel;
import com.yami.trading.admin.model.UserDataListModel;
import com.yami.trading.api.model.AddUserBankModel;
import com.yami.trading.bean.agent.AgentDto;
import com.yami.trading.bean.c2c.dto.C2cPaymentMethodDto;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.model.UserBank;
import com.yami.trading.bean.user.dto.UserDataDto;
import com.yami.trading.common.annotation.SysLog;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.IPHelper;
import com.yami.trading.dao.c2c.UserBankMapper;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.security.common.manager.PasswordManager;
import com.yami.trading.service.agent.AgentService;
import com.yami.trading.service.user.UserRecomService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("userData")
@Api(tags = "用户基础数据")
public class UserDataController {
    @Autowired
    UserService userService;
    @Autowired
    SysUserService sysUserService;
    @Autowired
    PasswordManager passwordManager;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRecomService userRecomService;
    @Autowired
    SysUserOperService sysUserOperService;
    @Autowired
    private PermissionFacade permissionFacade;
    @Autowired
    UserBankMapper userBankMapper;
    @Autowired
    AgentService agentService;
    @PostMapping("list")
    @ApiOperation("列表")
    public Result<Page<UserDataDto>> list(@Valid @RequestBody UserDataListModel model) {
        List<String> roleNames = new ArrayList<>();
        if (StrUtil.isEmpty(model.getRolename())) {
            roleNames.add(Constants.SECURITY_ROLE_GUEST);
            roleNames.add(Constants.SECURITY_ROLE_MEMBER);
            roleNames.add(Constants.SECURITY_ROLE_TEST);
        } else {
            roleNames.add(model.getRolename());
        }

        Page<UserDataDto> page = new Page(model.getCurrent(), model.getSize());
        userService.listUserAndRecom(page, roleNames, model.getUserCode(), model.getUserName(), model.getLastIp(),
                permissionFacade.getOwnerUserIds());
        for (UserDataDto userDataDto : page.getRecords()) {
            userDataDto.setOnline(userService.isOnline(userDataDto.getUserId()));
            userDataDto.setLoginAuthority(userDataDto.getStatus()==1);
            userDataDto.setUserRegip(userDataDto.getUserLastip());
        }
        return Result.ok(page);
    }

    @ApiOperation(value = "修改用户")
    @PostMapping("update")
    @SysLog("修改用户")
    public Result update(@Valid @RequestBody UpdateUserModel model) {
        User user = userService.getById(model.getUserId());
        if (user == null) {
            throw new YamiShopBindException("参数错误!");
        }
        user.setEnabled(model.isEnabled());
        user.setRemarks(model.getRemarks());
        user.setStatus(model.isLoginAuthority()?1:0);
        user.setWithdrawAuthority(model.isWithdrawAuthority());
        user.setStatus(model.isLoginAuthority() ? 1 : 0);
        userService.updateById(user);
        return Result.ok(null);
    }

    /**
     * 获取 客户银行卡
     */
    @RequestMapping("myBank")
    @ApiOperation("获取我的 客户银行卡 列表")
    public Result<List<UserBank>> myBankUser(String userId) {
        List<UserBank> dbBank =this.userBankMapper.selectList(new QueryWrapper<UserBank>().eq("user_id", userId));
        if(dbBank.size()<=0){
            throw new YamiShopBindException("客户未添加银行信息");
        }
        return Result.succeed(dbBank);
    }

    @ApiOperation(value = "修改客户银行卡信息")
    @RequestMapping("updateBank")
    public Result<?> updateBank(@RequestBody @Valid AddUserBankModel model) {
        UserBank userBank = new UserBank();
        userBank.setUuid(model.getUUID());
        userBank.setBankName(model.getBankName());
        userBank.setBankNo(model.getBankNo());
        userBank.setBankAddress(model.getBankAddress());
        userBank.setBankImg(model.getBankImg());
        userBank.setBankPhone(model.getBankPhone());
        userBank.setUserName(model.getUserName());
        userBank.setBankPhone(model.getBankPhone());
        userBank.setMethodName(model.getMethodName());
        userBank.setCreateTime(new Date());
        int insertCount = this.userBankMapper.updateById(userBank);
        if (insertCount > 0) {
            return Result.succeed("更新成功");
        }else{
            throw  new YamiShopBindException("更新失败");
        }
    }
    // 手机号校验
    private boolean isValidPhone(String username) {
        Pattern p = Pattern.compile("[0-9]*");
        return p.matcher(username).matches();
    }

    @ApiOperation(" 新增 演示账号")
    @PostMapping(value = "add")
    public Result add(@RequestBody @Valid UserDataAddModel request) {
        request.setPassword(passwordManager.decryptPassword(request.getPassword()));
        String username = request.getUsername().replace(" ", "");
        String password = request.getPassword().replace(" ", "");
        String userCode = request.getParentsUseCode();
        if (StrUtil.isEmpty(userCode)) {
            throw new YamiShopBindException("请输入代理商推荐码!");
        }
        Page<AgentDto> page = new Page(1, 5);
        page = agentService.listTotal(page, userCode);
        List<AgentDto> list = page.getRecords();
        if (list.size()<=0) {
            throw new YamiShopBindException("代理商推荐码未找到");
        }
        // 手机
        if (!isValidPhone(username)) {
            throw new YamiShopBindException("手机号格式不正常!");
        }
        userService.saveUser(username, password, request.isLoginAuthority(), request.isEnabled(), request.getRemarks(), SecurityUtils.getSysUser().getUserId().toString(), IPHelper.getIpAddr(), userCode);
        return Result.succeed();
    }
}





