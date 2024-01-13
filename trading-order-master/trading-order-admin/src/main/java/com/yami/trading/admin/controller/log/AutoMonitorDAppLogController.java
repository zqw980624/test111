package com.yami.trading.admin.controller.log;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.model.log.DAppLogListModel;
import com.yami.trading.bean.log.dto.LogDto;
import com.yami.trading.common.domain.Result;
import com.yami.trading.service.system.AutoMonitorDAppLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("dAppLog")
@Api(tags = "转账转换日志 前端日志")
public class AutoMonitorDAppLogController {

    @Autowired
    AutoMonitorDAppLogService  autoMonitorDAppLogService;

    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page<LogDto>> list(@RequestBody @Valid DAppLogListModel model){
        Page<LogDto> page=new Page(model.getCurrent(),model.getSize());
        autoMonitorDAppLogService.listPage(page,model.getRolename(),model.getUserName(),model.getAction(),model.getStartTime(), model.getEndTime());
        return  Result.ok(page);
    }
}
