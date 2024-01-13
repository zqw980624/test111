package com.yami.trading.service.item;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.item.dto.ItemUserOptionalDTO;
import com.yami.trading.bean.item.dto.RealtimeDTO;
import com.yami.trading.bean.item.mapstruct.ItemUserOptionalWrapper;
import com.yami.trading.common.lang.LangUtils;
import com.yami.trading.service.data.DataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.item.domain.ItemUserOptional;
import com.yami.trading.dao.item.ItemUserOptionalMapper;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户自选Service
 * @author lucas
 * @version 2023-03-10
 */
@Service
@Transactional
@Slf4j
public class ItemUserOptionalService extends ServiceImpl<ItemUserOptionalMapper, ItemUserOptional> {
    @Autowired
    private ItemUserOptionalWrapper wrapper;
    @Autowired
    private ItemService itemService;
    @Autowired
    @Qualifier("dataService")
    private DataService dataService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    private ItemUserOptionalService itemUserOptionalService;
    @Autowired
    ItemUserOptionalMapper itemUserOptionalMapper;
    /**
     * 查询我的自选的币对列表
     * @param partyId
     * @return
     */
    public List<String> getOptionalSymbols(String partyId){
        QueryWrapper<ItemUserOptional> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("PARTY_ID", partyId);
        queryWrapper.select("SYMBOL");
        return list(queryWrapper).stream().map(ItemUserOptional::getSymbol).collect(Collectors.toList());
    }

    public List<ItemUserOptionalDTO> getItemUserOptionalsPTX(String partyId) {
        QueryWrapper<ItemUserOptional> queryWrappers = new QueryWrapper<>();
        queryWrappers.eq("PARTY_ID", partyId);
        List<ItemUserOptional> models = itemUserOptionalService.list(queryWrappers);
        List<ItemUserOptionalDTO> model =new ArrayList<>();
        for(ItemUserOptional dto: models){
            Item bySymbol = itemService.findByPid(dto.getPid());
            if(bySymbol==null){
                return model;
            }
            if(LangUtils.isEnItem()){
                bySymbol.transName();
            }
            ItemUserOptionalDTO dtos = new ItemUserOptionalDTO();
            dtos.setName(bySymbol.getName());
            Object results = redisTemplate.opsForValue().get("ydTask" + dto.getPid());
            if(results!=null){
                JSONObject msgObject = JSONUtil.parseObj(results);
                dtos.setClose(new BigDecimal(msgObject.getStr("last")));
                dtos.setChangeRatio(new BigDecimal(msgObject.getStr("pc")));
                dtos.setTurnoverRates(msgObject.getStr("pcp"));
                dtos.setPartyId(partyId);
                dtos.setName(bySymbol.getName());
                dtos.setSymbol(bySymbol.getSymbol());
                dtos.setPid(dto.getPid());
            }else{
                log.error("{} 实时价格是空", bySymbol.getSymbol());
            }
            model.add(dtos);
        }
        return model;
    }

    public List<ItemUserOptionalDTO> getItemUserOptionals(@RequestParam(required = false) String symbol, String partyId) {
        QueryWrapper<ItemUserOptional> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("PARTY_ID", partyId);
        queryWrapper.eq(StrUtil.isNotBlank(symbol),"symbol", symbol);
        List<ItemUserOptionalDTO> models = wrapper.toDTO(list(queryWrapper));
        for(ItemUserOptionalDTO dto: models){
            Item bySymbol = itemService.findBySymbol(dto.getSymbol());
            if(LangUtils.isEnItem()){
                bySymbol.transName();
            }
            dto.setName(bySymbol.getName());
            List<Realtime> realtimes = dataService.realtime(dto.getSymbol());
            if(!CollectionUtil.isEmpty(realtimes)){
                Realtime realtime = realtimes.get(0);
                dto.setClose(realtime.getClose());
                dto.setChangeRatio(realtime.getChangeRatio());
                dto.setTurnoverRate(realtime.getTurnoverRate());
                dto.setVolumeRatio(realtime.getVolumeRatio());
            }else{
                log.error("{} 实时价格是空", bySymbol.getSymbol());
            }
        }
        return models;
    }

}
