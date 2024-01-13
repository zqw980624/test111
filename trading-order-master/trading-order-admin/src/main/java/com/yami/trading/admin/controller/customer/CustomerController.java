package com.yami.trading.admin.controller.customer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.controller.customer.model.BindGooleAuthCodeModel;
import com.yami.trading.admin.controller.customer.model.ForceOfflineModel;
import com.yami.trading.admin.controller.customer.model.UnbindGoogleAuthCodeModel;
import com.yami.trading.admin.model.CustomerListModel;
import com.yami.trading.admin.model.customer.CustomerAddModel;
import com.yami.trading.admin.model.customer.CustomerPasswordModel;
import com.yami.trading.admin.model.customer.CustomerSafePasswordModel;
import com.yami.trading.admin.model.customer.CustomerUpdateModel;
import com.yami.trading.bean.customer.CustomerDto;
import com.yami.trading.bean.model.Log;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.GoogleAuthenticator;
import com.yami.trading.common.util.IPHelper;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.security.common.manager.PasswordManager;
import com.yami.trading.service.customer.CustomerService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Date;

@RestController
@RequestMapping("customer")
@Api(tags = "客服管理")
public class CustomerController {


    @Autowired
    CustomerService customerService;

    @Autowired
    PasswordManager passwordManager;

    @Autowired
    SysUserService sysUserService;

    @Autowired
    UserService userService;

    @Autowired
    LogService logService;

    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page<CustomerDto>> list(@RequestBody @Valid CustomerListModel model) {
        Page<CustomerDto> page = new Page(model.getCurrent(), model.getSize());
        customerService.listData(page,model.getUserName());
        return Result.ok(page);
    }


    @ApiOperation(value = "新增客服")
    @PostMapping("add")
    public Result<?> add(@RequestBody @Valid CustomerAddModel model) {
        sysUserService.checkSafeWord(model.getLoginSafeword());
        sysUserService.checkSuperGoogleAuthCode(String.valueOf(model.getSuperGoogleAuthCode()));
        customerService.saveCustomer(model.getUserName(),model.getRemarks(),
                passwordManager.decryptPassword(model.getPassword())
                ,model.isEnabled()?1:0,
                passwordManager.decryptPassword(model.getSafePassword()),model.getAutoAnswer()
                ,SecurityUtils.getSysUser().getUsername());
        return Result.ok(null);
    }



    @ApiOperation(value = "修改客服")
    @PostMapping("update")
    public Result<?> update(@RequestBody @Valid CustomerUpdateModel model) {
        sysUserService.checkSafeWord(model.getLoginSafeword());
        customerService.updateCustomer(model.getAutoAnswer(),model.getRemarks()
                ,model.isEnabled()?1:0,model.getId(),SecurityUtils.getSysUser().getUsername());
        return Result.ok(null);
    }


    @ApiOperation(value = "修改客服密码")
    @PostMapping("updatePassword")
    public Result<?> updatePassword(@RequestBody @Valid CustomerPasswordModel model) {
        sysUserService.checkSafeWord(model.getLoginSafeword());
        sysUserService.checkSuperGoogleAuthCode(String.valueOf(model.getSuperGoogleAuthCode()));
        model.setPassword( passwordManager.decryptPassword(model.getPassword()));
        customerService.updateCustomerPassword(model.getPassword(),model.getId());
        return Result.ok(null);
    }


    @ApiOperation(value = "修改资金密码")
    @PostMapping("updateSafePassword")
    public Result<?> updateSafePassword(@RequestBody @Valid CustomerSafePasswordModel model) {
        model.setSafePassword( passwordManager.decryptPassword(model.getSafePassword()));
        customerService.updateCustomerSafePassword(model.getSafePassword(),model.getId());
        return Result.ok(null);
    }


    @ApiOperation(value = "强制下线")
    @PostMapping("forceOffline")
    public Result<?> forceOffline(@RequestBody @Valid ForceOfflineModel model) {
        sysUserService.checkSafeWord(model.getLoginSafeword());
        customerService.forceOffline(model.getId());
        return Result.ok(null);
    }



    @ApiOperation(value = "解绑谷歌验证码")
    @PostMapping("unbindGoogleAuthCode")
    public Result<?> unbindGoogleAuthCode(@RequestBody @Valid UnbindGoogleAuthCodeModel model) {
        sysUserService.checkSuperGoogleAuthCode(model.getRootGoogleCode());
        SysUser user= sysUserService.getById(model.getUserId());
        user.setGoogleAuthSecret(null);
        user.setGoogleAuthBind(false);
        sysUserService.updateById(user);
        return Result.ok(null);
    }

    @ApiOperation(value = "绑定谷歌验证码")
    @PostMapping("bindGoogleAuthCode")
    public Result<?> bindGoogleAuthCode(@RequestBody @Valid BindGooleAuthCodeModel model) {
        sysUserService.checkSuperGoogleAuthCode(model.getRootGoogleCode());
        SysUser user= sysUserService.getById(model.getUserId());
        long t = System.currentTimeMillis();
        GoogleAuthenticator ga = new GoogleAuthenticator();
        ga.setWindowSize(5); // should give 5 * 30 seconds of grace...
        boolean checkCode = ga.check_code(model.getGoogleAuthSecret(),
                Long.valueOf(model.getGoogleAuthCode()), t);
        if (!checkCode) {
            throw new YamiShopBindException("谷歌验证码错误");
        }
        user.setGoogleAuthSecret(model.getGoogleAuthSecret());
        user.setGoogleAuthBind(true);
        sysUserService.updateById(user);
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_OPERATION);
        log.setOperator(SecurityUtils.getSysUser().getUsername());
        log.setUsername(user.getUsername());
        log.setUserId(user.getUserId().toString()+"");
        log.setLog("ip:"+ IPHelper.getIpAddr() +"谷歌验证器绑定");
        log.setCreateTime(new Date());
        logService.save(log);
        return Result.ok(null);
    }
}
