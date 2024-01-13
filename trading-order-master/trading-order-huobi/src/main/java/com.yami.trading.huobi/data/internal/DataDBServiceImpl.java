package com.yami.trading.huobi.data.internal;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.common.config.RequestDataHelper;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.huobi.data.DataCache;
import com.yami.trading.huobi.data.job.RealtimeQueue;
import com.yami.trading.service.data.RealtimeService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.syspara.SysparaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataDBServiceImpl implements DataDBService {
    @Autowired
    private NamedParameterJdbcOperations namedParameterJdbcTemplate;
    @Autowired
    private SysparaService sysparaService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private RealtimeService realtimeService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void saveAsyn(Realtime realtime) {

        String symbol = realtime.getSymbol();
        Realtime latestRealtime = DataCache.getLatestRealTime(symbol);
        if (null != latestRealtime && latestRealtime.getTs() >= realtime.getTs()) {
            log.debug("时间没有变化");
            return;
        }
        DataCache.putLatestRealTime(symbol, realtime);

        // 最近60s内实时价格集合
        List<Realtime> list = DataCache.latestRealTimeMap_60s.get(symbol);
        if (list.size() >= KlineConstant.LATEST_REALTIME_LIST_MAX) {
            list.remove(0);
        }
        list.add(realtime);
        DataCache.latestRealTimeMap_60s.put(symbol, list);
        RealtimeQueue.add(realtime);
    }

    @Override
    public void saveBatch(List<Realtime> entities) {
        Map<String, List<Realtime>> collect = entities.stream().collect(Collectors.groupingBy(Realtime::getSymbol));
        for (String symbol : collect.keySet()) {
            RequestDataHelper.set("symbol", symbol);
            realtimeService.saveOrUpdateBatch(collect.get(symbol));
            RequestDataHelper.clear();
        }

    }

    @Override
    public Realtime get(String symbol) {
        RequestDataHelper.set("symbol", symbol);
        LambdaQueryWrapper<Realtime> queryWrapper = new LambdaQueryWrapper<Realtime>()
                .eq(Realtime::getSymbol, symbol)
                .orderByDesc(Realtime::getTs)
                .last("LIMIT 1");
        Realtime realtime = realtimeService.getBaseMapper().selectOne(queryWrapper);
        RequestDataHelper.clear();
        return realtime;

    }

    public void deleteRealtime(int days) {
        for (int i = 0; i <=  Constants.TABLE_PARTITIONS - 1; i++) {
            Map<String, Object> parameters = new HashMap();
            Long ts = DateUtils.addDate(new Date(), days).getTime();
            parameters.put("ts", ts);
            this.namedParameterJdbcTemplate.update("DELETE FROM t_realtime_" + i + " WHERE ts < :ts", parameters);
        }


    }

    @Override
    public void updateOptimize(String table) {
        for (int i = 0; i <=  Constants.TABLE_PARTITIONS - 1; i++) {
            this.jdbcTemplate.execute("optimize table " + table + "_" + i);

        }

    }

    @Override
    public List<Realtime> findRealtimeOneDay(String symbol) {
        int interval = this.sysparaService.find("data_interval").getInteger() / 1000;
        int num = (24 * 60 * 60) / interval;
        RequestDataHelper.set("symbol", symbol);

        LambdaQueryWrapper<Realtime> queryWrapper = new LambdaQueryWrapper<Realtime>()
                .eq(Realtime::getSymbol, symbol)
                .orderByDesc(Realtime::getTs)
                .last("LIMIT " + num);
        List<Realtime> realtimes = realtimeService.getBaseMapper().selectList(queryWrapper);
        RequestDataHelper.clear();
        return realtimes;

    }

    /**
     * 获取最新60s实时价格数据
     */
    @Override
    public List<Realtime> listRealTime60s(String symbol) {
        RequestDataHelper.set("symbol", symbol);
        int data_interval = sysparaService.find("data_interval").getInteger().intValue();
        // 取数据条数为
        int limit = 60*1000/data_interval;
        LambdaQueryWrapper<Realtime> queryWrapper = new LambdaQueryWrapper<Realtime>()
                .eq(Realtime::getSymbol, symbol)
                .orderByDesc(Realtime::getTs)
                .last("LIMIT " + limit);
        List<Realtime> realtimes = realtimeService.getBaseMapper().selectList(queryWrapper);
        RequestDataHelper.clear();
        return realtimes;
    }


}
