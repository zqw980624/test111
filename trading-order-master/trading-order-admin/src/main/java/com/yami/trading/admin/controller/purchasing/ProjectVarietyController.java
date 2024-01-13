package com.yami.trading.admin.controller.purchasing;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.controller.purchasing.dto.ProjectBreedDto;
import com.yami.trading.admin.controller.purchasing.dto.RealtimeDto;
import com.yami.trading.admin.controller.purchasing.model.*;
import com.yami.trading.admin.model.IdModel;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.log.dto.LogDto;
import com.yami.trading.bean.purchasing.ProjectVariety;
import com.yami.trading.bean.purchasing.dto.RelatedStockDto;
import com.yami.trading.common.domain.PageRequest;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.huobi.data.DataCache;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.purchasing.ProjectVarietyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("projectVariety")
@Api(tags = "ETF品种管理")
public class ProjectVarietyController {
    @Autowired
    ProjectVarietyService projectVarietyService;

    @Autowired
    DataService dataService;

    @Autowired
    ItemService itemService;

    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page<ProjectVariety>> list(@RequestBody @Valid PageRequest request) {
        Page<ProjectVariety> page = new Page(request.getCurrent(), request.getSize());
        projectVarietyService.page(page);
        for (ProjectVariety projectVariety : page.getRecords()) {
            Realtime realtime = DataCache.getRealtime(projectVariety.getRelatedStockSymbol());
            projectVariety.setRealtime(realtime);
            Item item= itemService.findBySymbol(projectVariety.getRelatedStockSymbol());
            if (item!=null){
                projectVariety.setDataType(item.getFake().equals("1")?1:2);
                projectVariety.setRelatedStockName(item.getName());
            }
        }
        return Result.ok(page);
    }

    @ApiOperation(value = "新增")
    @PostMapping("add")
    public Result add(@RequestBody @Valid ProjectVarietyAddModel addModel) {
        ProjectVariety projectBreed = new ProjectVariety();
        BeanUtils.copyProperties(addModel, projectBreed);
        List<RelatedStockDto> relatedStockDtos = new ArrayList<>();
        StringBuffer sb=new StringBuffer();
//        for (String str : addModel.getRelatedStockVarieties()) {
//            List<Realtime> list = dataService.realtime(str);
//            Item item= itemService.findBySymbol(str);
//            if (!CollectionUtil.isEmpty(list)) {
//                Realtime realtime = list.get(0);
//                RelatedStockDto dto = new RelatedStockDto();
//                dto.setSymbol(realtime.getSymbol());
//                dto.setName(realtime.getName());
//                relatedStockDtos.add(dto);
//                sb.append(realtime.getName()+",");
//            }
//        }
//        if (sb.length()==0){
//            projectBreed.setRelatedStockVarieties("");
//        }else {
//            projectBreed.setRelatedStockVarieties(sb.substring(0,sb.length()-1));
//        }
//        projectVarietyService.saveProjectBreed(projectBreed, relatedStockDtos);
        projectVarietyService.save(projectBreed);
        return Result.ok(null);
    }

    @ApiOperation(value = "更新")
    @PostMapping("update")
    public Result<Page<LogDto>> update(@RequestBody @Valid ProjectVarietyUpdateModel updateModel) {
        ProjectVariety projectBreed = projectVarietyService.getById(updateModel.getId());
        if (projectBreed == null) {
            throw new YamiShopBindException("参数错误!");
        }
        BeanUtils.copyProperties(updateModel, projectBreed);
        projectVarietyService.updateById(projectBreed);
        return Result.succeed();
    }

    @ApiOperation(value = "获取详情")
    @PostMapping("getDesc")
    public Result<ProjectBreedDto> getDesc(@RequestBody @Valid IdModel idModel) {
        ProjectVariety projectBreed = projectVarietyService.getById(idModel.getId());
        if (projectBreed == null) {
            throw new YamiShopBindException("参数错误!");
        }
        List<Realtime> list = dataService.realtime(projectBreed.getTransactionPairsSymbol());
        ProjectBreedDto dto = new ProjectBreedDto();
        BeanUtils.copyProperties(projectBreed, dto);
        if (!CollectionUtil.isEmpty(list)) {
            Realtime realtime = list.get(0);
            RealtimeDto realtimeDto = new RealtimeDto();
            realtimeDto.setAmount(realtime.getAmount());
            realtimeDto.setClose(realtime.getClose());
            realtimeDto.setLow(realtime.getLow());
            realtimeDto.setHigh(realtime.getHigh());
            realtimeDto.setOpenInterest(realtime.getVolume());
            realtimeDto.setDailyIncrement(realtime.getAmount());
            dto.setRealtimeDto(realtimeDto);
        }
        return Result.ok(dto);
    }

    @ApiOperation(value = "删除")
    @PostMapping("delete")
    public Result delete(@RequestBody @Valid IdModel idModel) {
//        projectVarietyService.deleteProjectBreed(idModel.getId());
        projectVarietyService.deleteByProjectBreedId(idModel.getId());
        return Result.succeed();
    }

}
