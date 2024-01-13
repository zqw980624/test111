package com.yami.trading.admin.controller.log;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.controller.log.model.CodeLogListModel;
import com.yami.trading.admin.model.IdModel;
import com.yami.trading.bean.log.domain.CodeLog;
import com.yami.trading.bean.model.Log;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.IPHelper;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.system.CodeLogService;
import com.yami.trading.service.system.LogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("codeLog")
@Api(tags = "验证码日志")
public class CodeLogController {


    @Autowired
    CodeLogService  codeLogService;

    @Autowired
    LogService logService;

    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page<CodeLog>> list(@RequestBody @Valid CodeLogListModel model){
        Page<CodeLog> page=new Page(model.getCurrent(),model.getSize());
       LambdaQueryWrapper<CodeLog> logLambdaQueryWrapper= Wrappers.<CodeLog>query().lambda();
       if (!StrUtil.isEmpty(model.getTarget())){
           logLambdaQueryWrapper.like(CodeLog::getTarget,model.getTarget());
       }
        logLambdaQueryWrapper.orderByDesc(CodeLog::getCreateTime);
        codeLogService.page(page,logLambdaQueryWrapper);
        return  Result.ok(page);
    }


    @ApiOperation(value = "查看code")
    @PostMapping("getCode")
    public Result<CodeLog> list(@RequestBody @Valid IdModel model){

        CodeLog codeLog=    codeLogService.getById(model.getId());
        if (codeLog==null){
            throw  new YamiShopBindException("参数错误!");
        }
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_OPERATION);
        log.setUsername("");
        log.setOperator(SecurityUtils.getSysUser().getUsername());
        log.setLog("管理员查看手机号/邮箱号["+codeLog.getTarget()+"]的验证码，管理员ip["+ IPHelper.getIpAddr() +"]");
        logService.save(log);
        return  Result.ok(codeLog);
    }
}
