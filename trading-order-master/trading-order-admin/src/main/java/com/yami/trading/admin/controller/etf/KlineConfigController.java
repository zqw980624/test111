package com.yami.trading.admin.controller.etf;

import cn.hutool.core.lang.Tuple;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yami.trading.bean.data.domain.Depth;
import com.yami.trading.bean.data.domain.Kline;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.data.domain.RobotModel;
import com.yami.trading.bean.etf.domain.EtfKLine;
import com.yami.trading.bean.etf.domain.EtfSecKLine;
import com.yami.trading.bean.etf.mapstruct.EtfSecKLineWrapper;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.item.dto.ItemDTO;
import com.yami.trading.bean.item.query.ItemQuery;
import com.yami.trading.common.domain.Result;

import javax.validation.Valid;

import com.google.common.collect.Lists;
import com.yami.trading.common.query.QueryWrapperGenerator;

import com.yami.trading.dao.etf.mapper.EtfSecKLineMapper;
import com.yami.trading.service.etf.EtfSecKLineService;
import com.yami.trading.service.etf.MarketService;
import com.yami.trading.service.item.ItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.etf.domain.KlineConfig;

import com.yami.trading.bean.etf.dto.KlineConfigDTO;
import com.yami.trading.bean.etf.mapstruct.KlineConfigWrapper;
import com.yami.trading.service.etf.KlineConfigService;
import com.yami.trading.bean.etf.query.KlineConfigQuery;

import java.math.BigDecimal;
import java.util.*;


/**
 * etfK线图配置表Controller
 *
 * @author lucas
 * @version 2023-05-03
 */

@Api(tags = "债劵ETF_币对管理")
@RestController
@RequestMapping(value = "/etf/klineConfig")
public class KlineConfigController {

    @Autowired
    private KlineConfigService klineConfigService;

    @Autowired
    private KlineConfigWrapper klineConfigWrapper;

    @Autowired
    private EtfSecKLineWrapper etfSecKLineWrapper;

    @Autowired
    private ItemService itemService;

    @Autowired
    MarketService marketService;

    @Autowired
    EtfSecKLineService etfSecKLineService;

    /**
     * etfK线图配置表列表数据
     */
    @ApiOperation(value = "查询etfK线图配置表列表数据")
    @GetMapping("list")
    public Result<IPage<ItemDTO>> list(ItemQuery itemQuery, Page<ItemDTO> page) throws Exception {
        QueryWrapper queryWrapper = QueryWrapperGenerator.buildQueryCondition(itemQuery, ItemQuery.class);
        IPage<ItemDTO> result = itemService.findPage(page, queryWrapper);
        return Result.ok(result);
    }

    /**
     * 添加交易对
     */
    @ApiOperation(value = "添加交易对")
    @PostMapping("addItem")
    public Result<String> addItem(@Valid @RequestBody Item item) throws Exception {
        //新增或编辑表单保存
        itemService.saveOrUpdate(item);
        return Result.ok("添加交易对成功");
    }


    /**
     * 根据Id获取etfK线图配置表数据
     */
    @ApiOperation(value = "根据Id获取etfK线图配置表数据")
    @GetMapping("queryById")
    public Result<KlineConfigDTO> queryById(String id) {
        return Result.ok(klineConfigWrapper.toDTO(klineConfigService.getById(id)));
    }

    /**
     * 根据Id获取etfK线图配置表数据
     */
    @ApiOperation(value = "查询etfK线图配置列表")
    @GetMapping("pageList")
    public Result<IPage<KlineConfigDTO>> pageList(KlineConfigQuery query, Page<KlineConfig> page) throws Exception {
        IPage<KlineConfigDTO> list;
        if (Objects.isNull(query.getSymbol())) {
            list = klineConfigService.page(page, new LambdaQueryWrapper<KlineConfig>().orderBy(true, false, KlineConfig::getOpenTimeTs)).convert(klineConfigWrapper::toDTO);
        } else {
            list = klineConfigService.page(page, new LambdaQueryWrapper<>(new KlineConfig()).eq(KlineConfig::getSymbol, query.getSymbol()).orderBy(true, false, KlineConfig::getOpenTimeTs)).convert(klineConfigWrapper::toDTO);
        }
        list.getRecords().forEach(r -> {
            Item bySymbol = itemService.findBySymbol(r.getSymbol());
            if (bySymbol != null) {
                r.setSymbolName(bySymbol.getName());
            }
        });
        return Result.ok(list);
    }

    /**
     * 保存etfK线图配置表
     */
    @ApiOperation(value = "保存etfK线图配置表")
    @PostMapping("save")
    public Result<String> save(@RequestBody KlineConfigDTO klineConfigDTO) {
        Tuple startAndEnd = klineConfigService.getStartAndEnd(klineConfigDTO.getOpenTimeTs());
        klineConfigDTO.setOpenTimeTs(startAndEnd.get(0));
        klineConfigDTO.setCloseTimeTs(startAndEnd.get(1));

        if (etfSecKLineService.count(new LambdaQueryWrapper<>(new EtfSecKLine()).eq(EtfSecKLine::getSymbol, klineConfigDTO.getSymbol()).between(EtfSecKLine::getTs, klineConfigDTO.getOpenTimeTs(), klineConfigDTO.getCloseTimeTs())) > 0) {
            return Result.failed("今天数据已存在，无法添加");
        }

        //新增或编辑表单保存
        KlineConfig klineConfig = klineConfigWrapper.toEntity(klineConfigDTO);
        klineConfig.setCreateTime(new Date());

        List<Kline> klines = klineConfigService.generateSecKLine(klineConfigDTO);
        if (klines != null) {
            List<EtfSecKLine> etfSecKLines = etfSecKLineWrapper.toDTO(klines);
            etfSecKLineService.saveBatch(etfSecKLines, 10000);
        }

        klineConfigService.saveOrUpdate(klineConfig);
        marketService.clear();
        return Result.ok("保存etfK线图配置表成功");
    }


    /**
     * 删除etfK线图配置表
     */
    @ApiOperation(value = "删除etfK线图配置表")
    @DeleteMapping("delete")
    public Result<String> delete(String ids) {
        List<String> list = Arrays.asList(ids.split(","));

        klineConfigService.deleteByRobotModelId(list);
        return Result.ok("删除etfK线图配置表成功");
    }


    /**
     * 删除etfK线图配置表
     */
    @ApiOperation(value = "初始化ETF当天K线图,分钟级别")
    @PostMapping("init")
    public Result<EtfKLine> init(@RequestBody KlineConfigDTO klineConfigDTO) {
        Item symbol = itemService.findBySymbol(klineConfigDTO.getSymbol());
        EtfKLine model = klineConfigService.queryKLine(klineConfigDTO);
        List<Kline> klineList = model.getKlineList();
        List<Kline> result = new ArrayList<>();
        // 抽象成15分钟图，方便前端显示
        List<List<Kline>> partition = Lists.partition(klineList, 15);
        for (List<Kline> list1Min : partition) {
            Double high = list1Min.get(0).getHigh().doubleValue();
            Double low = list1Min.get(0).getLow().doubleValue();
            for (Kline kline : list1Min) {
                if (high <= kline.getHigh().doubleValue()) {
                    high = kline.getHigh().doubleValue();
                }
                if (low >= kline.getLow().doubleValue()) {
                    low = kline.getLow().doubleValue();
                }
            }
            int lastIndex = list1Min.size() - 1;
            Kline kline = new Kline();
            kline.setSymbol(klineConfigDTO.getSymbol());
            kline.setTs(list1Min.get(lastIndex).getTs());
            kline.setSymbol(klineConfigDTO.getSymbol());
            kline.setOpen(list1Min.get(0).getOpen().setScale(symbol.getDecimals(), BigDecimal.ROUND_DOWN));
            kline.setHigh(new BigDecimal(high).setScale(symbol.getDecimals(), BigDecimal.ROUND_DOWN));
            kline.setLow(new BigDecimal(low).setScale(symbol.getDecimals(), BigDecimal.ROUND_DOWN));
            kline.setClose(list1Min.get(lastIndex).getClose().setScale(symbol.getDecimals(), BigDecimal.ROUND_DOWN));
            kline.setPeriod(Kline.PERIOD_15MIN);
            // 格式化小数点位
            // klineOneTop.formatPoint(kline);
            BigDecimal sumAmount = klineList.stream()
                    .map(Kline::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sumVolume = klineList.stream()
                    .map(Kline::getVolume)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            kline.setAmount(sumAmount);
            kline.setVolume(sumVolume);
            result.add(kline);
        }
        model.setKlineList(result);
        return Result.ok(model);
    }

    /**
     * 根据Id获取etfK线图配置表数据
     */
    @ApiOperation(value = "获取每秒k线图")
    @GetMapping("secKline")
    public Result<Realtime> secKline(String symbol) {
        return Result.ok(marketService.queryRealtime(symbol));
    }


    /**
     * 根据Id获取etfK线图配置表数据
     */
    @ApiOperation(value = "获取每秒深度")
    @GetMapping("secDepth")
    public Result<Depth> secDepth(String symbol) {
        return Result.ok(marketService.queryDepth(symbol));
    }


    /**
     * 根据Id获取etfK线图配置表数据
     */
    @ApiOperation(value = "加倍深度")
    @GetMapping("accelerate")
    public Result accelerate(@RequestParam(value = "symbol") String symbol, @RequestParam(value = "enlarge") Double
            enlarge, @RequestParam(value = "enable") Boolean enable) {
        Map<String, Double> accelerate = marketService.getAccelerate();
        if (enable) {
            accelerate.put(symbol, enlarge);
        } else {
            accelerate.remove(symbol);
        }
        return Result.succeed();
    }


}
