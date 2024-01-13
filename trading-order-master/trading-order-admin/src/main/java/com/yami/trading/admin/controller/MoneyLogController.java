package com.yami.trading.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.model.MoneylogListModel;
import com.yami.trading.bean.user.dto.MoneyLogDto;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.service.MoneyLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("moneylog")
@Api(tags = "账号资金变动记录")
public class MoneyLogController {

    @Autowired
    MoneyLogService moneyLogService;

    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page<MoneyLogDto>> list(@RequestBody @Valid MoneylogListModel model){
        Page<MoneyLogDto> page=new Page(model.getCurrent(),model.getSize());
        moneyLogService.pageMoneyLog(model.getUserCode(),page,model.getRolename(),model.getStartTime(),model.getEndTime(),model.getUserName(),model.getLog(),model.getCategory());
        page.getRecords().forEach(moneyLogDto -> {
            moneyLogDto.setCategoryText(Constants.MONEYLOG_CATEGORY.get(moneyLogDto.getCategory()));
            moneyLogDto.setRoleNameText(Constants.ROLE_MAP.get(moneyLogDto.getRoleName()));
        });
        return  Result.ok(page);
    }

}
