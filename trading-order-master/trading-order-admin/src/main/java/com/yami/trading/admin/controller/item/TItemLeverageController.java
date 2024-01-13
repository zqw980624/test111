package com.yami.trading.admin.controller.item;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.yami.trading.bean.item.dto.ItemLeverageDTO;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.query.QueryWrapperGenerator;

import com.yami.trading.common.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.yami.trading.bean.item.mapstruct.TItemLeverageWrapper;
import com.yami.trading.service.item.ItemLeverageService;
import com.yami.trading.bean.item.query.TItemLeverageQuery;

import java.math.BigDecimal;


/**
 * 产品杠杠倍数Controller
 *
 * @author lucas
 * @version 2023-03-10
 */

@Api(tags = "产品杠杠倍数")
@RestController
@RequestMapping(value = "normal/adminItemLeverageAction!")
public class TItemLeverageController {

    @Autowired
    private ItemLeverageService itemLeverageService;

    @Autowired
    private TItemLeverageWrapper tItemLeverageWrapper;

    /**
     * 产品杠杠倍数列表数据
     */
    @ApiOperation(value = "查询产品杠杠倍数列表数据")
    @GetMapping("list")
    public Result<IPage<ItemLeverageDTO>> list(TItemLeverageQuery tItemLeverageQuery, Page<ItemLeverageDTO> page) throws Exception {
        QueryWrapper queryWrapper = QueryWrapperGenerator.buildQueryCondition(tItemLeverageQuery, TItemLeverageQuery.class);

        String itemId = tItemLeverageQuery.getItemId();
        if(StringUtils.isNotEmpty(itemId)){
            queryWrapper.eq("item_id",itemId);
        }
        queryWrapper.orderByAsc("lever_rate");
        IPage<ItemLeverageDTO> result = itemLeverageService.findPage(page, queryWrapper);
        return Result.ok(result);
    }


    /**
     * 根据Id获取产品杠杠倍数数据
     */
    @ApiOperation(value = "根据Id获取产品杠杠倍数数据")
    @GetMapping("queryById")
    public Result<ItemLeverageDTO> queryById(String id) {
        return Result.ok(itemLeverageService.findById(id));
    }

    /**
     * 保存产品杠杠倍数
     */
    @ApiOperation(value = "保存产品杠杠倍数")
    @PostMapping("add.action")
    public Result<String> save(@Valid @RequestBody ItemLeverageDTO itemLeverageDTO) {
        String leverRate = itemLeverageDTO.getLeverRate();
        if (StrUtil.isEmpty(leverRate)) {
            throw new YamiShopBindException("杠杆倍数不能为空");
        }
        BigDecimal bigDecimal;
        try {
            bigDecimal = new BigDecimal(leverRate);
        } catch (Exception e) {
            throw new YamiShopBindException("杠杆倍数必须是数字");
        }
        if (bigDecimal.compareTo(BigDecimal.ONE) <= 0) {
            throw new YamiShopBindException("杠杆倍数必须大于1");
        }
        //新增或编辑表单保存
        itemLeverageService.saveOrUpdate(tItemLeverageWrapper.toEntity(itemLeverageDTO));
        return Result.ok("保存产品杠杠倍数成功");
    }


    /**
     * 删除产品杠杠倍数
     */
    @ApiOperation(value = "删除产品杠杠倍数")
    @GetMapping("delete")
    public Result<String> delete(String ids) {
        String idArray[] = ids.split(",");
        itemLeverageService.removeByIds(Lists.newArrayList(idArray));
        return Result.ok("删除产品杠杠倍数成功");
    }

}
