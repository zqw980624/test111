package com.yami.trading.service.item;

import cn.hutool.core.collection.CollectionUtil;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.template.QuickConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.item.dto.ItemDTO;
import com.yami.trading.bean.item.dto.ItemLeverageDTO;
import com.yami.trading.common.util.ApplicationContextUtils;
import com.yami.trading.common.util.MarketOpenChecker;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.dao.item.ItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 产品Service
 *
 * @author lucas
 * @version 2023-03-10
 */
@Service
@Transactional
@Slf4j
public class ItemService extends ServiceImpl<ItemMapper, Item> {
    public static final String ITEM_CACHE = "itemCache";
    public static final String ALL = "all";

    @Autowired
    private ItemLeverageService itemLeverageService;

    private Map<String, Integer> symbolDecimal = Maps.newHashMap();


    @Autowired
    private CacheManager cacheManager;
    private Cache<String, List<Item>> itemCache;

    @PostConstruct
    public void init() {
        QuickConfig qc = QuickConfig.newBuilder(ITEM_CACHE)
                .expire(Duration.ofSeconds(3600))
                .cacheType(CacheType.REMOTE) // two level cache
                .build();
        itemCache = cacheManager.getOrCreateCache(qc);
        itemCache.put(ALL, list());

    }

    public List<Item> findByType(String type) {
        List<Item> items =  ApplicationContextUtils.getApplicationContext().getBean(ItemService.class).list();
        if(items == null){
            LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<Item>()
                    .eq(Item::getType, type);
            return super.baseMapper.selectList(queryWrapper);

        }
        return items.stream().filter(i -> i.getType().equalsIgnoreCase(type)).collect(Collectors.toList());

    }

    public List<Item> findByItem(String type) {
        QueryWrapper<Item> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("open_close_type", type);

        return super.baseMapper.selectList(itemWrapper);
    }

    public Item findByPid(String pid) {
        QueryWrapper<Item> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("pid", pid);

        return super.baseMapper.selectOne(itemWrapper);
    }

    public  List<Item> findByTypes(String type) {
        QueryWrapper<Item> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("type", type);

        return super.baseMapper.selectList(itemWrapper);
    }

  /*  public List<Item> findByItem(String type) {
        QueryWrapper<Item> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("open_close_type", type);

        return super.baseMapper.selectList(itemWrapper);
    }*/

    /**
     * 获取多个币对，每个类型的数量
     * @param symbols
     * @return
     */
    public Map<String, Integer> typeCountGroupByType(Collection<String> symbols) {
        // 避免为空时候报错
        symbols.add("-1");
        Map<String, Integer> typeCount = new HashMap<>();
        for(String type : Item.types){
            typeCount.put(type, 0);
        }
        if(CollectionUtil.isEmpty(symbols)){
            return typeCount;
        }
        QueryWrapper<Item> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("SYMBOL", symbols);
        queryWrapper.select("SYMBOL", "TYPE", "count(*) as count")
                .groupBy("TYPE");
        List<Map<String, Object>> maps = baseMapper.selectMaps(queryWrapper);
        int sum = 0;
        for(Map<String, Object> data : maps){
            typeCount.put(data.get("TYPE").toString(), Integer.parseInt(data.get("count").toString()));
            sum += Integer.parseInt(data.get("count").toString());
        }
        typeCount.put("all", sum);
        return typeCount;
    }

    /**
     * 通过code 找对象，
     *
     * @param symbol
     * @return
     */
    public Item findBySymbol(String symbol) {
        List<Item> items = ApplicationContextUtils.getApplicationContext().getBean(ItemService.class).list();
        if (CollectionUtil.isNotEmpty(items)) {
            Optional<Item> first = items.stream().filter(i -> symbol.equalsIgnoreCase(i.getSymbol())).findFirst();
            return first.orElse(null);
        }
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<Item>()
                .eq(Item::getSymbol, symbol)
                .last("LIMIT 1");
        return super.baseMapper.selectOne(queryWrapper);
    }

    /**
     * 通过code 找对象，
     *
     * @param symbol
     * @return
     */
    public Item findBySymbols(String symbol) {
        QueryWrapper<Item> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("symbol", symbol);

        return super.baseMapper.selectOne(itemWrapper);
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    public ItemDTO findById(String id) {
        ItemDTO item = baseMapper.findById(id);
        if (item != null) {
            QueryWrapper wrapper = new QueryWrapper();
            List<ItemLeverageDTO> levels = itemLeverageService.findByItemId(id);
            item.setLevels(levels);
        }
        return item;
    }

    /**
     * 自定义分页检索
     *
     * @param page
     * @param queryWrapper
     * @return
     */
    public IPage<ItemDTO> findPage(Page<ItemDTO> page, QueryWrapper queryWrapper) {
        queryWrapper.eq("a.del_flag", 0); // 排除已经删除
        return baseMapper.findList(page, queryWrapper);
    }

    @Cached(name = ITEM_CACHE, key = "'all'", expire = 3600)
    @Override
    public List<Item> list() {
        QueryWrapper<Item> queryWrapper = new QueryWrapper<Item>()
                .eq("type", "YD-stocks");
        List<Item> list = super.list(queryWrapper);
        symbolDecimal = list.stream()
                .collect(Collectors.toMap(Item::getSymbol, Item::getDecimals, (s1, s2) -> s2));
        return list;
    }

    @Override
    @CacheInvalidate(name = ITEM_CACHE)
    public boolean updateById(Item item) {
        return super.updateById(item);
    }

    @Override
    @CacheInvalidate(name = ITEM_CACHE)
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }


    /**
     * 获取品种精度
     *
     * @param symbol
     * @return
     */
    public Integer getDecimal(String symbol) {
        return symbolDecimal.getOrDefault(symbol, 0);
    }

    public List<String> getAllSymbol() {

        List<Item> list = list();
        List<String> result = new ArrayList<>();
        for (Item item : list) {
            result.add(item.getSymbol());
        }

        return result;
    }

    public List<Item> cacheGetAll() {
        return ApplicationContextUtils.getApplicationContext().getBean(ItemService.class).list();
    }


    public List<Item> cacheGetByMarket(String symbol) {
        List<Item> cacheGetAll = cacheGetAll();
        if (StringUtils.isNullOrEmpty(symbol)) {
            return cacheGetAll;
        }
        List<Item> result = new ArrayList<Item>();
        for (Item item : cacheGetAll) {
            if (symbol.equals(item.getSymbol()))
                result.add(item);
        }
        return result;
    }

    /**
     * 当前是否开盘
     * @param symbol
     * @return
     */
    public boolean isOpen(String symbol){
        Item bySymbol = findBySymbol(symbol);
        return MarketOpenChecker.isMarketOpenByItemCloseType(bySymbol.getOpenCloseType());
    }
}
