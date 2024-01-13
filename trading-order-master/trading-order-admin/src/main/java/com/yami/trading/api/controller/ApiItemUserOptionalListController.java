package com.yami.trading.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yami.trading.api.dto.OptionalListCount;
import com.yami.trading.api.dto.UserOptionalListDto;
import com.yami.trading.bean.item.domain.ItemUserOptionalItem;
import com.yami.trading.bean.item.domain.ItemUserOptionalList;
import com.yami.trading.bean.item.dto.ItemUserOptionalDTO;
import com.yami.trading.bean.item.dto.ItemUserOptionalItemDTO;
import com.yami.trading.bean.item.dto.ItemUserOptionalListDTO;
import com.yami.trading.bean.item.dto.ItemUserOptionalListUpdateDTO;
import com.yami.trading.bean.item.mapstruct.ItemUserOptionalListWrapper;
import com.yami.trading.bean.item.query.ItemUserOptionalListQuery;
import com.yami.trading.bean.rate.domain.ExchangeRate;
import com.yami.trading.bean.rate.dto.ExchangeRateDTO;
import com.yami.trading.bean.rate.mapstruct.ExchangeRateWrapper;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.item.ItemUserOptionalItemService;
import com.yami.trading.service.item.ItemUserOptionalListService;
import com.yami.trading.service.item.ItemUserOptionalService;
import com.yami.trading.service.rate.ExchangeRateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自选分组Controller
 *
 * @author lucas
 * @version 2023-03-10
 */

@Api(tags = "自选分组")
@RestController
@RequestMapping(value = "api/item/itemUserOptionalList")
public class ApiItemUserOptionalListController {

    @Autowired
    private ItemUserOptionalListService itemUserOptionalListService;

    @Autowired
    private ItemUserOptionalListWrapper itemUserOptionalListWrapper;
    @Autowired
    private ItemUserOptionalService itemUserOptionalService;
    @Autowired
    private ItemUserOptionalItemService itemUserOptionalItemService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private ExchangeRateService exchangeRateService;
    @Autowired
    private ExchangeRateWrapper exchangeRateWrapper;

    /**
     * 自选分组列表数据
     */
    @ApiOperation(value = "我的自选，分组列表,默认会显示聚合信息")
    @GetMapping("list")
    public Result<UserOptionalListDto> list(ItemUserOptionalListQuery itemUserOptionalListQuery, Page<ItemUserOptionalList> page) throws Exception {
        Map<String, String> maps = Maps.newHashMap();
        maps.put("zh-CN","我的自选");
        maps.put("CN","我的自選");
        maps.put("en","My Choice");
        maps.put("Japanese","私の選択");
        maps.put("Korean","내 선택\n");
        maps.put("de","meine Wahl");
        maps.put("fr","mon choix");
        maps.put("vi","lựa chọn của tôi");
        maps.put("Italy","la mia scelta");
        maps.put("th","ตัวเลือกของฉัน");
        String lang = itemUserOptionalListQuery.getLanguage();
        if(StringUtils.isEmptyString(lang)){
            lang = "en";
        }
        String partyId = SecurityUtils.getCurrentUserId();
        if(StringUtils.isEmptyString(partyId)){
            UserOptionalListDto model = new UserOptionalListDto();
            model.setList(Lists.newArrayList());
            model.setCount(Maps.newHashMap());
            return Result.succeed(model);
        }
        UserOptionalListDto dto = new UserOptionalListDto();
        List<OptionalListCount> optionalListCountList = new ArrayList<>();
        Set<String> symbols = new HashSet<>();
        // 先查询我的自选币对列表
        List<String> optionalSymbols = itemUserOptionalService.getOptionalSymbols(partyId);
        symbols.addAll(optionalSymbols);
        optionalListCountList.add(new OptionalListCount("0", maps.getOrDefault(lang, "my choice"), "", optionalSymbols.size()));

        // 查询我的自选分组，查询每个分组，有的币对。可以统计出每个分组的数量
        List<ItemUserOptionalList> list = itemUserOptionalListService.findListByPartyId(partyId);
        for (ItemUserOptionalList itemUserOptionalList : list) {
            List<String> listSymbols = itemUserOptionalItemService.findListByPartyId(partyId, itemUserOptionalList.getUuid());
            optionalListCountList.add(new OptionalListCount(itemUserOptionalList.getUuid(), itemUserOptionalList.getName(), itemUserOptionalList.getCurrency(), listSymbols.size()));
            symbols.addAll(listSymbols);
        }
        dto.setList(optionalListCountList);
        dto.setCount(itemService.typeCountGroupByType(symbols));

        return Result.succeed(dto);
    }

    @ApiOperation(value = "获取新增自选组时候的币种列表")
    @GetMapping("listExchanges")
    public Result<List<ExchangeRateDTO>> listExchanges() {
        QueryWrapper<ExchangeRate> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT CURRENCY,NAME");
        List<ExchangeRate> list = exchangeRateService.list(queryWrapper);
        return Result.ok(exchangeRateWrapper.toDTO(list));
    }

    /**
     * 根据分组id获取分组下的币对列表
     */
    @ApiOperation(value = "根据Id获取自选分组数据")
    @GetMapping("listItemsById")
    public Result<List<ItemUserOptionalDTO>> queryById(String id) {
        String partyId = SecurityUtils.getCurrentUserId();
        List<ItemUserOptionalDTO> itemUserOptionals = itemUserOptionalService.getItemUserOptionalsPTX(partyId);
        return Result.succeed(itemUserOptionals);
    }

    /**
     * 根据分组id获取分组下的币对列表
     */
    @ApiOperation(value = "根据Id获取自选分组数据")
    @GetMapping("listItemsByIds")
    public Result<List<ItemUserOptionalItemDTO>> queryByIds(String id) {
        List<ItemUserOptionalItemDTO> list = new ArrayList<>();
        String partyId = SecurityUtils.getCurrentUserId();
        if ("0".equalsIgnoreCase(id)) {
            List<ItemUserOptionalDTO> itemUserOptionals = itemUserOptionalService.getItemUserOptionals(null, partyId);
            list = BeanUtil.copyToList(itemUserOptionals, ItemUserOptionalItemDTO.class);
        } else {
            list = itemUserOptionalListService.findListItemsByPartyId(partyId, id);
        }
        return Result.succeed(list);
    }
    /**
     * 根据type获取分组下的币对列表
     */
    @ApiOperation(value = "根据type获取自选分组数据")
    @GetMapping("listItemsByType")
    public Result<List<ItemUserOptionalItemDTO>> listItemsByType(String type) {
        List<ItemUserOptionalItemDTO> list = new ArrayList<>();
        String partyId = SecurityUtils.getUser().getUserId();
        list = itemUserOptionalListService.findListItemsByPartyIdAndType(partyId, type);
        return Result.succeed(list);
    }

    /**
     * 保存自选分组
     */
    @ApiOperation(value = "新增自选分组")
    @PostMapping("save")
    public Result<String> save(@Valid ItemUserOptionalListDTO itemUserOptionalListDTO) {
        String partyId = SecurityUtils.getUser().getUserId();
        if (itemUserOptionalListService.findOne(partyId, itemUserOptionalListDTO.getName()) != null) {
            return Result.failed("该自选组名称已经使用");
        }
        //新增或编辑表单保存
        ItemUserOptionalList entity = itemUserOptionalListWrapper.toEntity(itemUserOptionalListDTO);
        entity.setPartyId(partyId);
        itemUserOptionalListService.save(entity);
        return Result.succeed("保存自选分组成功");
    }

    /**
     * 保存自选到分组
     */
    @ApiOperation(value = "将一个币对加入一个分组")
    @GetMapping("saveItem")
    public Result<String> saveItem(String listId, String symbol) {
        String partyId = SecurityUtils.getUser().getUserId();
        if (itemUserOptionalItemService.findOne(partyId, listId, symbol) != null) {
            return Result.failed("已经添加过该分组了");
        }
        ItemUserOptionalItem itemUserOptionalItem = new ItemUserOptionalItem();
        itemUserOptionalItem.setListId(listId);
        itemUserOptionalItem.setSymbol(symbol);
        itemUserOptionalItem.setPartyId(partyId);
        itemUserOptionalItemService.save(itemUserOptionalItem);
        return Result.succeed("保存自选分组成功");
    }

    /**
     * 保存自选到分组
     */
    @ApiOperation(value = "判断币对是否已经加入分组")
    @GetMapping("isItemHasAdd")
    public Result<Boolean> isItemHasAdd(String listId, String symbol) {
        String partyId = SecurityUtils.getUser().getUserId();
        if (itemUserOptionalItemService.findOne(partyId, listId, symbol) != null) {
            return Result.succeed(true);
        }
        return Result.succeed(false);

    }

    /**
     * 保存自选到分组
     */
    @ApiOperation(value = "判断币对是否已经被全局加入某个分组")
    @GetMapping("isItemHasAddGlobal")
    public Result<Boolean> isItemHasAddGlobal(String symbol) {
        String partyId = SecurityUtils.getCurrentUserId();
        if(StringUtils.isEmptyString(partyId)){
            return Result.succeed(false);
        }
        if (itemUserOptionalItemService.findOne(partyId, symbol)) {
            return Result.succeed(true);
        }
        return Result.succeed(false);

    }

    @ApiOperation(value = "将一个币对从分组移除")
    @GetMapping("removeItem")
    public Result<String> removeItem(String listId, String symbol) {
        String partyId = SecurityUtils.getUser().getUserId();
        ItemUserOptionalItem one = itemUserOptionalItemService.findOne(partyId, listId, symbol);
        if (one == null) {
            return Result.succeed("删除成功");
        }
        itemUserOptionalItemService.removeById(one);
        return Result.succeed("删除成功");
    }

    /**
     * 保存自选分组
     */
    @ApiOperation(value = "更新自选分组")
    @PostMapping("update")
    public Result<String> update(@Valid ItemUserOptionalListUpdateDTO updateDTO) {
        String partyId = SecurityUtils.getUser().getUserId();
        ItemUserOptionalList one = itemUserOptionalListService.findOne(partyId, updateDTO.getName());
        if (one != null && !one.getUuid().equalsIgnoreCase(updateDTO.getUuid())) {
            return Result.failed("该自选组名称已经使用");
        }
        //新增或编辑表单保存
        ItemUserOptionalList entity = BeanUtil.copyProperties(updateDTO, ItemUserOptionalList.class);
        entity.setPartyId(partyId);
        itemUserOptionalListService.updateById(entity);
        return Result.succeed("保存自选分组成功");
    }

    /**
     * 删除自选分组
     */
    @ApiOperation(value = "删除自选分组")
    @GetMapping("delete")
    public Result<String> delete(String ids) {
        String idArray[] = ids.split(",");
        ArrayList<String> list = Lists.newArrayList(idArray);
        if(list.size() == 0){
            return Result.succeed(null, "删除自选分组成功");
        }
        itemUserOptionalListService.removeByIds(list);
        QueryWrapper<ItemUserOptionalItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("list_id", list);
        List<String> collect = itemUserOptionalItemService.list(queryWrapper).stream().map(ItemUserOptionalItem::getUuid).collect(Collectors.toList());
        itemUserOptionalItemService.removeByIds(collect);
        return Result.succeed(null, "删除自选分组成功");
    }

}
