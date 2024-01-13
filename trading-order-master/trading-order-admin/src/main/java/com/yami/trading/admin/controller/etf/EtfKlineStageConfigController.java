package com.yami.trading.admin.controller.etf;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.etf.domain.EtfKlineStageConfig;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.service.etf.EtfKlineStageConfigService;
import com.yami.trading.service.item.ItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author lucas
 * @since 2023-06-09 20:22:26
 */
@Api(tags = "债劵ETF_币对管理_etfK线图阶段配置")
@RestController
@RequestMapping("/klineStageConfig")
public class EtfKlineStageConfigController {

    @Autowired
    private EtfKlineStageConfigService etfKlineStageConfigService;
    @Autowired
    private ItemService itemService;

    /**
     * etfK线图阶段配置列表数据
     */
    @ApiOperation(value = "etfK线图阶段配置列表数据")
    @GetMapping("list")
    public Result<IPage<EtfKlineStageConfig>> list( @RequestParam(value = "type",required = false) String type, @RequestParam(value = "symbol", required = false) String symbol, Page<EtfKlineStageConfig> page) throws Exception {
        List<String> symbols = itemService.findByType(type).stream().map(Item::getSymbol).collect(Collectors.toList());
        symbols.add("-1");
        QueryWrapper<EtfKlineStageConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(StringUtils.isNotEmpty(type), "symbol", symbols);
        queryWrapper.eq(StringUtils.isNotEmpty(symbol), "symbol", symbol);
        Page<EtfKlineStageConfig> etfKlineStageConfigPage = etfKlineStageConfigService.page(page, queryWrapper);
        return Result.ok(etfKlineStageConfigPage);
    }

    /**
     * 添加etfK线图阶段配置数据
     */
    @ApiOperation(value = "etfK线图阶段配置数据")
    @PostMapping("add")
    public Result<String> addItem(@Valid @RequestBody EtfKlineStageConfig config) {
        //新增或编辑表单保存
        etfKlineStageConfigService.saveOrUpdate(config);
        return Result.ok("添加交易对成功");
    }


    /**
     * 根据Id获取etfK线图阶段配置数据
     */
    @ApiOperation(value = "根据Id获取etfK线图阶段配置数据")
    @GetMapping("queryById")
    public Result<EtfKlineStageConfig> queryById(String id) {
        return Result.ok(etfKlineStageConfigService.getById(id));
    }

    /**
     * 根据Id获取etfK线图阶段配置数据
     */
    @ApiOperation(value = "根据Id删除etfK线图阶段配置数据")
    @GetMapping("delete")
    public Result<String> delete(String id) {
        etfKlineStageConfigService.removeById(id);
        return Result.ok("删除成功");
    }

}
