package com.yami.trading.admin.controller.syspara;

import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.domain.Result;

import javax.validation.Valid;

import com.google.common.collect.Lists;
import com.yami.trading.common.query.QueryWrapperGenerator;

import com.yami.trading.service.item.ItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.syspara.domain.OpenClose;

import com.yami.trading.bean.syspara.dto.OpenCloseDTO;
import com.yami.trading.bean.syspara.mapstruct.OpenCloseWrapper;
import com.yami.trading.service.syspara.OpenCloseService;
import com.yami.trading.bean.syspara.query.OpenCloseQuery;

import java.util.List;
import java.util.stream.Stream;


/**
 * 开盘停盘时间设置Controller
 *
 * @author lucas
 * @version 2023-05-20
 */

@Api(tags = "开盘停盘时间设置")
@RestController
@RequestMapping(value = "/syspara/openClose")
public class OpenCloseController {

    @Autowired
    private OpenCloseService openCloseService;

    @Autowired
    private OpenCloseWrapper openCloseWrapper;
    @Autowired
    private ItemService itemService;

    /**
     * 开盘停盘时间设置列表数据
     */
    @ApiOperation(value = "查询开盘停盘时间设置列表数据")
    @GetMapping("list")
    public Result<IPage<OpenClose>> list(OpenCloseQuery openCloseQuery, Page<OpenClose> page) throws Exception {
        QueryWrapper queryWrapper = QueryWrapperGenerator.buildQueryCondition(openCloseQuery, OpenCloseQuery.class);
        IPage<OpenClose> result = openCloseService.page(page, queryWrapper);
        List<OpenClose> records = result.getRecords();
        for(OpenClose record: records){
            Item bySymbol = itemService.findBySymbol(record.getSymbol());
            if(bySymbol != null){
                record.setSymbolName(bySymbol.getName());
            }
        }
        return Result.ok(result);
    }


    /**
     * 根据Id获取开盘停盘时间设置数据
     */
    @ApiOperation(value = "根据Id获取开盘停盘时间设置数据")
    @GetMapping("queryById")
    public Result<OpenCloseDTO> queryById(String id) {
        return Result.ok(openCloseWrapper.toDTO(openCloseService.getById(id)));
    }

    /**
     * 保存开盘停盘时间设置
     */
    @ApiOperation(value = "保存开盘停盘时间设置")
    @PostMapping("save")
    public Result<String> save(@Valid @RequestBody OpenCloseDTO openCloseDTO) {
        //新增或编辑表单保存
        OpenClose entity = openCloseWrapper.toEntity(openCloseDTO);
        entity.setCloseTs( openCloseDTO.getCloseBjDate().getTime());
        entity.setOpenTs(openCloseDTO.getOpenBjDate().getTime());
        try {
            openCloseService.saveOrUpdate(entity);
        }catch (DuplicateKeyException e){
            return Result.failed("当前币对所在时段已经配置过开盘时间了");
        }
        return Result.ok("保存开盘停盘时间设置成功");
    }


    /**
     * 删除开盘停盘时间设置
     */
    @ApiOperation(value = "删除开盘停盘时间设置")
    @DeleteMapping("delete")
    public Result<String> delete(String ids) {
        String idArray[] = ids.split(",");
        openCloseService.removeByIds(Lists.newArrayList(idArray));
        return Result.ok("删除开盘停盘时间设置成功");
    }

}
