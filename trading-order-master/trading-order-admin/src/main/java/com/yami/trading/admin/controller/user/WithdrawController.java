package com.yami.trading.admin.controller.user;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.controller.user.model.WithdrawListModel;
import com.yami.trading.admin.controller.user.model.WithdrawRemarksModel;
import com.yami.trading.admin.model.*;
import com.yami.trading.bean.agent.AgentDto;
import com.yami.trading.bean.user.dto.WithdrawDto;
import com.yami.trading.common.annotation.SysLog;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.security.common.manager.PasswordManager;
import com.yami.trading.service.RealNameAuthRecordService;
import com.yami.trading.service.WithdrawService;
import com.yami.trading.service.agent.AgentService;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("withdraw")
@Api(tags = "提现订单")
public class WithdrawController {
    @Autowired
    WithdrawService withdrawService;
    @Autowired
    RealNameAuthRecordService realNameAuthRecordService;
    @Autowired
    SysUserService sysUserService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    PasswordManager passwordManager;
    @Autowired
    AgentService agentService;
    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page<WithdrawDto>> list(@RequestBody @Valid WithdrawListModel request) {
        SysUser sysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        Page<AgentDto> pages = new Page(1, 5);
        pages = agentService.listTotal(pages, sysUser.getUsername());
        List<AgentDto> lists = pages.getRecords();
        if (lists.size()>0) {//代理商
            Page<WithdrawDto> page = new Page(request.getCurrent(), request.getSize());
            withdrawService.listRecords(page, request.getStatus(),
                    request.getRolename(), request.getUserName(), request.getOrderNo(), lists.get(0).getUserCode());
            QrConfig config = new QrConfig(345, 345);
            config.setMargin(3);
            return Result.ok(page);
        }else{
            Page<WithdrawDto> page = new Page(request.getCurrent(), request.getSize());
            withdrawService.listRecord(page, request.getStatus(),
                    request.getRolename(), request.getUserName(), request.getOrderNo());
            QrConfig config = new QrConfig(345, 345);
            config.setMargin(3);
            return Result.ok(page);
        }
    }

    /*@ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page<WithdrawDto>> list(@RequestBody @Valid WithdrawListModel request) {
        Page<WithdrawDto> page = new Page(request.getCurrent(), request.getSize());
        withdrawService.listRecord(page, request.getStatus(),
                request.getRolename(), request.getUserName(), request.getOrderNo());
        QrConfig config = new QrConfig(345, 345);
        config.setMargin(3);
        for (WithdrawDto withdrawDto: page.getRecords()){
            String base64 = QrCodeUtil.generateAsBase64(withdrawDto.getAddress(), config, "png");
            withdrawDto.setQdcode(base64);
        }
        return Result.ok(page);
    }*/

    @ApiOperation(value = "通过审核手动打款")
    @PostMapping("examineOk")
    @SysLog("UDST提现订单-通过审核手动打款")
    public Result<?> examineOk(@RequestBody @Valid WithdrawExamineModel model) {
        Long adminUserId = SecurityUtils.getSysUser().getUserId();
        withdrawService.examineOk(model.getId(), adminUserId);
        return Result.ok(null);
    }

    @ApiOperation(value = "驳回")
    @PostMapping("reject")
    @SysLog("UDST提现订单-驳回")
    public Result<?> reject(@RequestBody @Valid WithdrawRejectModel model) {
        String adminUserName = SecurityUtils.getSysUser().getUsername();
        withdrawService.reject(model.getId(), model.getContent(), adminUserName);
        return Result.ok(null);
    }

    @ApiOperation(value = "备注")
    @PostMapping("remarks")
    @SysLog("UDST提现订单-驳回")
    public Result<?> remarks(@RequestBody @Valid WithdrawRemarksModel model) {
        withdrawService.remarks(model.getId(),model.getRemarks());
        return Result.ok(null);
    }

    @ApiOperation(value = "修改用户提现订单收款地址")
    @PostMapping("changeAddress")
    @SysLog("UDST提现订单-修改用户提现订单收款地址")
    public Result<?> changeAddress(@RequestBody @Valid ChangeAddressModel model) {
        String adminUserName = SecurityUtils.getSysUser().getUsername();
        withdrawService.updateAddress(model.getId(), adminUserName,model.getAddress(), model.getAccount(), model.getNames(), model.getBank());
        return Result.ok(null);
    }
}
