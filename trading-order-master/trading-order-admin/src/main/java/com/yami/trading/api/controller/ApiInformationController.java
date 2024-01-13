package com.yami.trading.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yami.trading.bean.cms.Infomation;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.service.cms.InfomationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lucas
 * @since 2023-06-19 23:44:55
 */
@RestController
@CrossOrigin
@Slf4j
@Api(tags = "实时行情数据")
public class ApiInformationController {
    public static final String action = "/api/information!";
    @Autowired
    private InfomationService infomationService;
    /**
     * 返回自选币种的行情
     */
    @ApiOperation("查看实时咨询数据")
    @GetMapping(action + "list.action")
    public Result<List<Infomation>> list(@RequestParam(required =false ) Integer limit, @RequestParam(required =false ) String language) {
        if(limit == null){
            limit = 50;
        }
        if(limit>=200){
            limit = 200;
        }
        QueryWrapper<Infomation> infomationQueryWrapper = new QueryWrapper<>();
        infomationQueryWrapper.orderByDesc("create_time");
        infomationQueryWrapper.last("LIMIT "+limit);
        return  Result.ok(infomationService.list(infomationQueryWrapper));

    }

}
