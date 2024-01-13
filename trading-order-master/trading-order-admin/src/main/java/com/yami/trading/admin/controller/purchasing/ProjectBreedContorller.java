package com.yami.trading.admin.controller.purchasing;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Splitter;
import com.yami.trading.admin.controller.purchasing.dto.ProjectBreedDto;
import com.yami.trading.admin.controller.purchasing.dto.RealtimeDto;
import com.yami.trading.admin.controller.purchasing.model.ProjectBreedAddModel;
import com.yami.trading.admin.controller.purchasing.model.ProjectBreedListModel;
import com.yami.trading.admin.controller.purchasing.model.ProjectBreedUpdateModel;
import com.yami.trading.admin.model.IdModel;
import com.yami.trading.admin.model.purchasing.PurchasingUpdateModel;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.item.dto.RealtimeDTO;
import com.yami.trading.bean.item.dto.RealtimeDTOS;
import com.yami.trading.bean.log.dto.LogDto;
import com.yami.trading.bean.purchasing.ProjectBreed;
import com.yami.trading.bean.purchasing.dto.RelatedStockDto;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.purchasing.ProjectBreedService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("projectBreed")
@Api(tags = "ETF总类管理")
public class ProjectBreedContorller {
    @Autowired
    ProjectBreedService projectBreedService;
    @Autowired
    DataService dataService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    ItemService itemService;
    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page<ProjectBreedDto>> list(@RequestBody @Valid ProjectBreedListModel request) {
        Page<ProjectBreed> page = new Page(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<ProjectBreed> lambdaQueryWrapper = Wrappers.<ProjectBreed>query().lambda();
        if (StringUtils.isNotEmpty(request.getProjectName())) {
            lambdaQueryWrapper.eq(ProjectBreed::getProjectName, request.getProjectName());
        }
        lambdaQueryWrapper.orderByDesc(ProjectBreed::getCreateTime);
        projectBreedService.page(page, lambdaQueryWrapper);
        List<ProjectBreedDto> breedDtos = new ArrayList<>();
        for (ProjectBreed projectBreed : page.getRecords()) {
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
                dto.setRealtimeDto(realtimeDto);
            }
            List<String> relatedStockVarieties = Splitter.on(",").omitEmptyStrings().splitToList(projectBreed.getRelatedStockVarieties());
            dto.setRelatedStockVarieties(projectBreed.getRelatedStockVarieties());
            String collect = relatedStockVarieties.stream().map(r -> itemService.findBySymbol(r)).filter(Objects::nonNull).map(Item::getName).collect(Collectors.joining(","));
            dto.setRelatedStockVarietiesNames(collect);
            breedDtos.add(dto);
        }
        Page<ProjectBreedDto> result = new Page<>();
        result.setRecords(breedDtos);
        result.setTotal(page.getTotal());
        result.setSize(page.getSize());
        result.setCurrent(page.getCurrent());
        return Result.ok(result);
    }

    @ApiOperation(value = "新增")
    @PostMapping("add")
    public Result add(@RequestBody @Valid ProjectBreedAddModel addModel) {
        ProjectBreed projectBreed = new ProjectBreed();
        BeanUtils.copyProperties(addModel, projectBreed);
        List<RelatedStockDto> relatedStockDtos = new ArrayList<>();
        StringBuffer sb=new StringBuffer();
        if(addModel.getRelatedStockVarieties() != null){
            for (String str : addModel.getRelatedStockVarieties()) {
                List<Realtime> list = dataService.realtime(str);
                if (!CollectionUtil.isEmpty(list)) {
                    Realtime realtime = list.get(0);
                    RelatedStockDto dto = new RelatedStockDto();
                    dto.setSymbol(realtime.getSymbol());
                    dto.setName(realtime.getName());
                    relatedStockDtos.add(dto);
                    sb.append(realtime.getName()+",");
                }
            }
        }
        if (sb.length()==0){
            projectBreed.setRelatedStockVarieties("");
        }else {
            projectBreed.setRelatedStockVarieties(sb.substring(0,sb.length()-1));
        }
        projectBreedService.saveProjectBreed(projectBreed, relatedStockDtos);
        return Result.ok(null);
    }

    @ApiOperation(value = "更新")
    @PostMapping("update")
    public Result<Page<LogDto>> update(@RequestBody @Valid ProjectBreedUpdateModel updateModel) {
        ProjectBreed projectBreed = projectBreedService.getById(updateModel.getId());
        if (projectBreed == null) {
            throw new YamiShopBindException("参数错误!");
        }
        List<RelatedStockDto> relatedStockDtos = new ArrayList<>();
        StringBuffer sb=new StringBuffer();
        for (String str : updateModel.getRelatedStockVarieties()) {
            List<Realtime> list = dataService.realtime(str);
            if (!CollectionUtil.isEmpty(list)) {
                Realtime realtime = list.get(0);
                RelatedStockDto dto = new RelatedStockDto();
                dto.setSymbol(realtime.getSymbol());
                dto.setName(realtime.getName());
                relatedStockDtos.add(dto);
                sb.append(realtime.getSymbol()+",");
            }
        }
        BeanUtils.copyProperties(updateModel, projectBreed);
        if (sb.length()==0){
            projectBreed.setRelatedStockVarieties("");
        }else {
            projectBreed.setRelatedStockVarieties(sb.substring(0,sb.length()-1));
        }
        projectBreedService.updateProjectBreed(projectBreed, relatedStockDtos);
        return Result.ok(null);
    }

    @ApiOperation(value = "获取详情")
    @PostMapping("getDesc")
    public Result<ProjectBreedDto> getDesc(@RequestBody @Valid IdModel idModel) {
        ProjectBreed projectBreed = projectBreedService.getById(idModel.getId());
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
        projectBreedService.deleteProjectBreed(idModel.getId());
        return Result.succeed();
    }
}
