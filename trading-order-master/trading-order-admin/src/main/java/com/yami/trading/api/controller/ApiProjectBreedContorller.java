package com.yami.trading.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.controller.purchasing.model.ProjectBreedListModel;
import com.yami.trading.api.model.GetConstituentStockListModel;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.purchasing.ProjectBreed;
import com.yami.trading.bean.purchasing.ProjectVariety;
import com.yami.trading.common.domain.PageRequest;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.huobi.data.DataCache;
import com.yami.trading.service.purchasing.ProjectBreedService;
import com.yami.trading.service.purchasing.ProjectVarietyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("api/projectBreed")
@Slf4j
@Api(tags = "ETF总类-api")
public class ApiProjectBreedContorller {


    @Autowired
    ProjectBreedService projectBreedService;

    @Autowired
    ProjectVarietyService projectVarietyService;

    @ApiOperation(value = "列表")
    @GetMapping("getAll")
    public Result<Page<ProjectBreed>> list(PageRequest request) {
        Page page = new Page(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<ProjectBreed> lambdaQueryWrapper = Wrappers.<ProjectBreed>query().lambda();
        projectBreedService.page(page, lambdaQueryWrapper);
        return Result.ok(page);
    }



    @ApiOperation(value = "获取成份股列表")
    @GetMapping("getConstituentStockList")
    public Result<List<ProjectVariety>> getConstituentStockList(GetConstituentStockListModel  model) {
        List<ProjectVariety> list= projectVarietyService.list(Wrappers.<ProjectVariety>query().lambda().eq(ProjectVariety::getTransactionPairsSymbol,model.getSymbol()));
         for (ProjectVariety projectVariety:list){
             Realtime realtime = DataCache.getRealtime(projectVariety.getRelatedStockSymbol());
             log.info(realtime+"======");
             projectVariety.setRealtime(realtime);
         }
        return Result.ok(list);
    }
}
