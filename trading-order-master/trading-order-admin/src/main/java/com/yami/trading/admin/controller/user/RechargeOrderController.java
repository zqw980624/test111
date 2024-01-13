package com.yami.trading.admin.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.model.*;
import com.yami.trading.bean.agent.AgentDto;
import com.yami.trading.bean.user.dto.RechargeBlockchainOrderDto;
import com.yami.trading.common.annotation.SysLog;
import com.yami.trading.common.domain.Result;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.security.common.manager.PasswordManager;
import com.yami.trading.service.AwsS3OSSFileService;
import com.yami.trading.service.RechargeBlockchainOrderService;
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
@RequestMapping("rechargeOrder")
@Api(tags = "USDT充值订单")
public class RechargeOrderController {
    @Autowired
    RechargeBlockchainOrderService rechargeBlockchainOrderService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private PasswordManager passwordManager;
    @Autowired
    SysUserService sysUserService;
    @Autowired
    AgentService agentService;

    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page<RechargeBlockchainOrderDto>> list(@RequestBody @Valid RechargeOrderModel request){
        Page<RechargeBlockchainOrderDto> page=null;
        SysUser sysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        Page<AgentDto> pages = new Page(1, 5);
        pages = agentService.listTotal(pages, sysUser.getUsername());
        List<AgentDto> lists = pages.getRecords();
        if (lists.size()>0) {//代理商
            page=new Page(request.getCurrent(),request.getSize());
            rechargeBlockchainOrderService.pageRecords(page,request.getRolename(),
                    request.getOrderNo(), request.getUserName(),request.getStartTime()
                    ,request.getEndTime(),request.getStatus(),lists.get(0).getUserCode());
            for (RechargeBlockchainOrderDto withdrawDto: page.getRecords()){
                withdrawDto.setDeflag("2");
            }
        }else{// 超级管理员  财务
            page=new Page(request.getCurrent(),request.getSize());
            rechargeBlockchainOrderService.pageRecord(page,request.getRolename(),
                    request.getOrderNo(), request.getUserName(),request.getStartTime()
                    ,request.getEndTime(),request.getStatus());
            for (RechargeBlockchainOrderDto withdrawDto: page.getRecords()){
                withdrawDto.setDeflag("1");
            }
        }
        return  Result.ok(page);
    }

    @ApiOperation(value = "手动到账")
    @PostMapping("manualReceipt")
    @SysLog("USDT充值订单-手动到账")
    public Result<?> manualReceipt(@RequestBody @Valid ManualReceiptModel model){
         SysUser user = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        rechargeBlockchainOrderService.manualReceipt(model.getId(),model.getAmount(),user.getUsername());
        return  Result.ok(null);
    }


    @ApiOperation(value = "驳回充值申请")
    @PostMapping("refusalApply")
    @SysLog("USDT充值订单-驳回充值申请")
    public Result<?> refusalApply(@RequestBody @Valid RefusalApplyModel model){
        SysUser user = sysUserService.getSysUserById( SecurityUtils.getSysUser().getUserId());
        rechargeBlockchainOrderService.refusalApply(model.getId(),model.getContent(),user.getUsername());
        return  Result.ok(null);
    }
}
