package com.yami.trading.admin.controller.data;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.dto.AdminMarketQuotationsCalculateDto;
import com.yami.trading.admin.dto.AdminMarketQuotationsUpdateDto;
import com.yami.trading.admin.facade.MarketQuotationsFacade;
import com.yami.trading.bean.item.dto.ItemDTO;
import com.yami.trading.bean.item.dto.MarketQuotations;
import com.yami.trading.bean.item.dto.MarketQuotationsAdjust;
import com.yami.trading.bean.item.query.ItemQuery;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.lang.LangUtils;
import com.yami.trading.common.query.QueryWrapperGenerator;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.item.ItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("normal/adminMarketQuotationsManageAction!")
@Api(tags = "行情数据调整")
@Slf4j
public class AdminMarketQuotationsManageController {
    @Autowired
    private MarketQuotationsFacade marketQuotationsFacade;

    @Autowired
    private ItemService itemService;

    @Autowired
    @Qualifier("dataService")
    private DataService dataService;


    /**
     * 列表
     */
    @ApiOperation(value = "列表查询")
    @GetMapping("list.action")
    public Result<IPage<MarketQuotations>> list(ItemQuery itemQuery, Page<ItemDTO> page) throws Exception {
        QueryWrapper queryWrapper = QueryWrapperGenerator.buildQueryCondition(itemQuery, ItemQuery.class);
        IPage<ItemDTO> result = itemService.findPage(page, queryWrapper);
        List<String> symbols = result.getRecords().stream().map(ItemDTO::getSymbol).collect(Collectors.toList());
        IPage<MarketQuotations> marketQuotationsPages = new Page<MarketQuotations>();
        BeanUtil.copyProperties(result, marketQuotationsPages);
        marketQuotationsPages.setRecords(marketQuotationsFacade.marketQuotationsList(symbols));
        return Result.succeed(marketQuotationsPages);

    }

    /**
     * 行情管理-调整弹框显示
     */
    @ApiOperation(value = "查询当前值")
    @GetMapping("showModal.action")
    public Result<MarketQuotationsAdjust> showModal(@NotEmpty @RequestParam(required = true) String symbol) {
        return Result.succeed(marketQuotationsFacade.getDetails(symbol));
    }


    /**
     * 页面计算
     */
    @ApiOperation(value = "预计算值")
    @PostMapping("getValue.action")
    public Result<Map<String, String>> getValue(@RequestBody AdminMarketQuotationsCalculateDto dto) {
        return Result.succeed(marketQuotationsFacade.calculateValue(dto.getSymbol(), dto.getType(), dto.getValue()));
    }

    /**
     * 调整计算
     */
    @ApiOperation(value = "调整值")
    @PostMapping("adjust.action")
    public Result<String> adjust(@RequestBody AdminMarketQuotationsUpdateDto adminMarketQuotationsUpdateDto) {
//        if(!itemService.isOpen(adminMarketQuotationsUpdateDto.getSymbol())){
//            throw new YamiShopBindException("当前休市状态");
//        }
        marketQuotationsFacade.adjust(adminMarketQuotationsUpdateDto.getSymbol(), adminMarketQuotationsUpdateDto.getSecond(), adminMarketQuotationsUpdateDto.getValue());
        return Result.succeed("操作成功");
    }
}
