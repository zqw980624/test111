package com.yami.trading.api.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Splitter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.yami.trading.api.dto.RelatedStocksDto;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.data.dto.StocksDto;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.item.dto.ItemDTO;
import com.yami.trading.bean.item.dto.SymbolDTO;
import com.yami.trading.bean.item.mapstruct.ItemWrapper;
import com.yami.trading.bean.item.query.ItemQuery;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.lang.LangUtils;
import com.yami.trading.common.util.MarketOpenChecker;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.service.data.DataService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.syspara.LocalSysparaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 产品Controller
 *
 * @author lucas
 * @version 2023-03-10
 */

@Api(tags = "产品")
@RestController
@ApiOperation("api/")
public class ApiItemController {

    public static final String ITEM = "/api/item!";
    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemWrapper itemWrapper;
    @Autowired
    private LocalSysparaService localSysparaService;
    @Autowired
    @Qualifier("dataService")
    private DataService dataService;
    @Autowired
    RedisTemplate redisTemplate;
    private static final Cache<String, Result<List<SymbolDTO>>> cache = CacheBuilder.newBuilder()
            .maximumSize(1024)
            .expireAfterWrite(3, TimeUnit.SECONDS)
            .build();

    /**
     * 产品列表数据
     */
    @ApiOperation(value = "获取板块指数成分股")
    @GetMapping(ITEM + "relateStocks.action")
    public Result<RelatedStocksDto> relateStocks(String symbol) throws Exception {
        Item bySymbol = itemService.findBySymbol(symbol);
        if (LangUtils.isEnItem()) {
            bySymbol.transName();
        }
        if (bySymbol == null) {
            throw new YamiShopBindException(symbol + " 不存在");
        }
        List<StocksDto> stocks = findRealTimeByBoard(symbol);
        RelatedStocksDto relatedStocksDto = new RelatedStocksDto(symbol, stocks);
        return Result.ok(relatedStocksDto);

    }

    /**
     * 获取一个板块相关的股票价格
     *
     * @param symbol
     * @return
     */
    public List<StocksDto> findRealTimeByBoard(String symbol) {
        QueryWrapper<Item> wrapper = new QueryWrapper();
        wrapper.like("board", symbol);
        wrapper.eq("show_status", "1");
        List<Item> list = itemService.list(wrapper);
        String symbols = list.stream().map(Item::getSymbol).collect(Collectors.joining(","));
        List<Realtime> realtime = dataService.realtime(symbols);
        List<StocksDto> stocksDtos = BeanUtil.copyToList(realtime, StocksDto.class);
        for (StocksDto stocksDto : stocksDtos) {
            Item bySymbol = itemService.findBySymbol(stocksDto.getSymbol());
            if (LangUtils.isEnItem()) {
                bySymbol.transName();
            }
            stocksDto.setName(bySymbol.getName());
        }
        return stocksDtos;
    }

    /**
     * 产品列表数据
     */
    @ApiOperation(value = "列表查询")
    @GetMapping(ITEM + "list.action")
    public Result<List<SymbolDTO>> list(ItemQuery itemQuery) throws Exception {
        QueryWrapper<Item> queryWrapper = new QueryWrapper<Item>();
        String symbol = itemQuery.getSymbol();
        String type = itemQuery.getType();
        String category = itemQuery.getCategory();
        queryWrapper.eq("show_status", "1");

        // 如果type为etf，需要排除大盘的
        List<String> symbolsNotCotnains = Lists.newArrayList(".DJI", ".IXIC", ".INX");
        if (type != null && type.equalsIgnoreCase("indices") && StringUtils.isEmptyString(symbol)) {
            queryWrapper.notIn("symbol", symbolsNotCotnains);
        }

        List<String> symbols = Lists.newArrayList();
        if (StrUtil.isNotEmpty(symbol)) {
            symbols = Splitter.on(",").splitToList(symbol);
        }

        if ("1".equalsIgnoreCase(itemQuery.getBoardType())) {
            queryWrapper.eq("category", "global");
        } else if ("2".equalsIgnoreCase(itemQuery.getBoardType())) {
            queryWrapper.ne("category", "global");
        }
        queryWrapper.in(CollectionUtil.isNotEmpty(symbols), "symbol", symbols);
        String name = itemQuery.getName();
        queryWrapper.and(StringUtils.isNotEmpty(name), itemWrapper -> itemWrapper.like("name", name).or().like("symbol", name));
        queryWrapper.eq(StrUtil.isNotBlank(type), "type", type);
        queryWrapper.like(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.orderByDesc("sorted");
        Page<Item> page = new Page<>(1, 1000);
        IPage<Item> result = itemService.page(page, queryWrapper);
        List<Item> records = result.getRecords();
        if (LangUtils.isEnItem()) {
            records.forEach(Item::transName);
        }
        List<SymbolDTO> dtos = new ArrayList<>();
        Map<String, Object> topPara = localSysparaService.find("index_top_symbols");
        String indexTopSymbols = topPara.get("index_top_symbols").toString();
        List<String> symbolList = Arrays.asList(indexTopSymbols.split(","));
        for (Item record : records) {
            String isTop = "0";
            if (symbolList.contains(record.getSymbol())) {
                isTop = "1";
            }
            SymbolDTO symbolDTO = BeanUtil.copyProperties(record, SymbolDTO.class);
            symbolDTO.setIsTop(isTop);
            List<Realtime> realtimes = dataService.realtime(record.getSymbol());
            if (CollectionUtil.isNotEmpty(realtimes)) {
                Realtime realtime = realtimes.get(0);
                symbolDTO.setChangeRatio(realtime.getChangeRatio());
                symbolDTO.setAmount(realtime.getAmount());
                symbolDTO.setVolume(realtime.getVolume());
                symbolDTO.setClose(realtime.getClose());
                symbolDTO.setTs(realtime.getTs());
                symbolDTO.setCurrent_time(realtime.getTs());
                symbolDTO.setChg(realtime.getClose().subtract(realtime.getOpen()));
            }
            dtos.add(symbolDTO);
        }
        return Result.ok(dtos);
    }

    /**
     * 产品列表数据  印度
     */
    @ApiOperation(value = "列表查询")
    @GetMapping(ITEM + "listYd.action")
    public Result<List<SymbolDTO>> listYd(ItemQuery itemQuery) {
        String cacheKey = String.format("%s:%s:%s:%s", itemQuery.getType(), itemQuery.getName(),
                itemQuery.getPageNum(), itemQuery.getPageSize());
        Result<List<SymbolDTO>> resp = cache.getIfPresent(cacheKey);
        if (resp != null) {
            return resp;
        }
        QueryWrapper<Item> queryWrapper = new QueryWrapper<Item>();
        String type = itemQuery.getType();
        List<SymbolDTO> dtos = new ArrayList<>();
        String name = itemQuery.getName();
        queryWrapper.eq("show_status", "1");
        queryWrapper.and(StringUtils.isNotEmpty(name), itemWrapper -> itemWrapper.like("name", name).or().like("symbol", name));
        queryWrapper.eq(StrUtil.isNotBlank(type), "type", type);
        queryWrapper.orderByDesc("sorted");
        List<Item> records =null;
        String items = (String)redisTemplate.opsForValue().get("stock_list");
        if (StringUtils.isNotEmpty(name) || itemQuery.getPageNum() != 1) {
            Page<Item> page = new Page<>(itemQuery.getPageNum(), itemQuery.getPageSize());
            IPage<Item> result = itemService.page(page, queryWrapper);
            records = result.getRecords();
        }else if (StrUtil.isEmpty(items)){
            Page<Item> page = new Page<>(itemQuery.getPageNum(), itemQuery.getPageSize());
            IPage<Item> result = itemService.page(page, queryWrapper);
            records = result.getRecords();
            items = JSONUtil.toJsonStr(records);
            redisTemplate.opsForValue().set("stock_list", items);
        }else {
            records = JSONUtil.toList(items, Item.class);
        }
        List<Object> cachedValues = redisTemplate.opsForValue().multiGet(records.stream().map(it -> "ydTask" + it.getPid())
                .collect(Collectors.toList()));
        for (int i = 0; i < records.size(); i++) {
            Item record = records.get(i);
            SymbolDTO symbolDTO = BeanUtil.copyProperties(record, SymbolDTO.class);
            //印度股
            Object results = cachedValues == null ? null : cachedValues.get(i);
            if (results == null) {
                continue;
            }
            JSONObject msgObject = JSONUtil.parseObj(results);
            symbolDTO.setClose(new BigDecimal(msgObject.getStr("last")));//最新价格
            symbolDTO.setChangeRatio(StringUtils.isNotEmpty(msgObject.getStr("pc")) ? new BigDecimal(msgObject.getStr("pc")) : new BigDecimal(0));//涨幅
            symbolDTO.setChgs(msgObject.getStr("pcp"));//涨幅百分比
            symbolDTO.setTss(msgObject.getStr("time"));//时间
            symbolDTO.setHigh(new BigDecimal(msgObject.getStr("high")));//最高
            symbolDTO.setLow(new BigDecimal(msgObject.getStr("low")));//最低
            symbolDTO.setId(record.getPid());//股票ID
            symbolDTO.setSymbol(record.getSymbol());//股票代码
            symbolDTO.setType(type);//股票类型
            symbolDTO.setSymbolData(record.getSymbolData());
            dtos.add(symbolDTO);
        }
        resp = Result.ok(dtos);
        cache.put(cacheKey, resp);
        return resp;
    }

    /**
     * 根据Id获取产品数据
     */
    @ApiOperation(value = "根据Id获取产品数据")
    @GetMapping(ITEM + "queryById")
    public Result<ItemDTO> queryById(String id) {
        ItemDTO byId = itemService.findById(id);
        if (LangUtils.isEnItem()) {
            byId.transName();
        }
        return Result.succeed(byId);
    }


    /**
     * 根据Id获取产品数据  zxz
     */
    @ApiOperation(value = "根据symbol获取币对详情")
    @GetMapping(ITEM + "queryBySymbol.action")
    public Result<Item> queryBySymbol(String symbol) {
        Item item = itemService.findBySymbol(symbol);
        if (LangUtils.isEnItem()) {
            item.transName();
        }
        item.setOpen(MarketOpenChecker.isMarketOpenByItemCloseType(item.getOpenCloseType()));
        return Result.succeed(item);
    }

    /**
     * 根据Id获取产品数据  zxz
     */
    @ApiOperation(value = "根据symbol获取币对详情")
    @GetMapping(ITEM + "queryBySymbolyd.action")
    public Result<Item> queryBySymbolyd(String pid) {
        Item item = itemService.findByPid(pid);
        if (LangUtils.isEnItem()) {
            item.transName();
        }
        return Result.succeed(item);
    }
}
