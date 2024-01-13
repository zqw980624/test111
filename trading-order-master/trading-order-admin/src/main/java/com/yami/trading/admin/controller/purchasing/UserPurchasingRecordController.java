package com.yami.trading.admin.controller.purchasing;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.controller.purchasing.model.PurchasingListModel;
import com.yami.trading.admin.controller.purchasing.model.UserPurchasingRecordModel;
import com.yami.trading.bean.purchasing.Purchasing;
import com.yami.trading.bean.purchasing.UserPurchasingRecord;
import com.yami.trading.common.domain.Result;
import com.yami.trading.service.purchasing.UserPurchasingRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("purchasingRecord")
@Api(tags = "用户申购记录")
public class UserPurchasingRecordController {

    @Autowired
    UserPurchasingRecordService userPurchasingRecordService;

    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page<UserPurchasingRecord>> list(@RequestBody @Valid UserPurchasingRecordModel model){
        Page page=new Page(model.getCurrent(),model.getSize());
        userPurchasingRecordService.listPage(page,model.getRolename(),model.getUserName());
        return  Result.ok(page);
    }
}
