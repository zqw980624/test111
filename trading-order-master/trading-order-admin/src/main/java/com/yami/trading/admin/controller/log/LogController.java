package com.yami.trading.admin.controller.log;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.dto.NameLableDto;
import com.yami.trading.admin.model.log.LogListModel;
import com.yami.trading.bean.log.dto.LogDto;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.service.system.LogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("log")
@Api(tags = "操作日志")
public class LogController {


    @Autowired
    LogService logService;

    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page<LogDto>> list(@RequestBody @Valid LogListModel model){
        Page<LogDto> page=new Page(model.getCurrent(),model.getSize());
        logService.listPage(page,model.getRoleName(),model.getUserName(),model.getLog(),model.getCategory(), model.getOperator());
        page.getRecords().forEach(logDto -> {
            logDto.setRoleNameText(Constants.ROLE_MAP.get(logDto.getRolename()));
        });
        return  Result.ok(page);
    }


    @ApiOperation(value = "获取操作类型")
    @GetMapping("getCategory")
    public Result< List<NameLableDto>> getCategory(){
        List<NameLableDto> list=new ArrayList<>();
         for (String key: Constants.LOG_CATEGORY.keySet()){
             NameLableDto nameLableDto=new NameLableDto();
             nameLableDto.setValue(key);
             nameLableDto.setLable(Constants.LOG_CATEGORY.get(key));
             list.add(nameLableDto);
         }
        return  Result.ok(list);
    }
}
