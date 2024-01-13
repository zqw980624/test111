package com.yami.trading.service.item;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.item.domain.ItemUserOptional;
import com.yami.trading.bean.item.domain.ItemUserOptionalItem;
import com.yami.trading.bean.item.dto.ItemUserOptionalDTO;
import com.yami.trading.bean.item.dto.ItemUserOptionalItemDTO;
import com.yami.trading.service.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.item.domain.ItemUserOptionalList;
import com.yami.trading.dao.item.ItemUserOptionalListMapper;

import javax.naming.ldap.HasControls;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 自选分组Service
 * @author lucas
 * @version 2023-03-10
 */
@Service
@Transactional
public class ItemUserOptionalListService extends ServiceImpl<ItemUserOptionalListMapper, ItemUserOptionalList> {
    @Autowired
    @Qualifier("dataService")
    private DataService dataService;

    @Autowired
    private ItemUserOptionalItemService itemUserOptionalItemService;

    @Autowired
    private ItemUserOptionalService itemUserOptionalService;
    @Autowired
    private ItemService itemService;
    /**
     * 查询用户拥有的分组
     * @param partyId
     * @return
     */
    public List<ItemUserOptionalList> findListByPartyId(String partyId){
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("PARTY_ID", partyId);
        List<ItemUserOptionalList> list = list(queryWrapper);
        return list;
    }

    /**
     * 查询用户拥有的分组
     * @param partyId
     * @return
     */
    public List<ItemUserOptionalItemDTO> findListItemsByPartyId(String partyId, String listId){
        QueryWrapper<ItemUserOptionalItem> queryWrapper = new QueryWrapper();
        queryWrapper.eq("PARTY_ID", partyId);
        queryWrapper.eq("LIST_ID", listId);
        List<ItemUserOptionalItem> list = itemUserOptionalItemService.list(queryWrapper);
        List<ItemUserOptionalItemDTO> models = BeanUtil.copyToList(list, ItemUserOptionalItemDTO.class);

        for(ItemUserOptionalItemDTO dto: models){
            dto.setName(itemService.findBySymbol(dto.getSymbol()).getName());
        }
        return models;

    }

    /**
     * 查询用户拥有的分组
     * @param partyId
     * @return
     */
    public List<ItemUserOptionalItemDTO> findListItemsByPartyIdAndType(String partyId, String type){
        List<String> byType = itemService.findByType(type).stream().map(Item::getSymbol).collect(Collectors.toList());
        byType.add("-1");


        QueryWrapper<ItemUserOptionalItem> queryWrapper = new QueryWrapper();
        queryWrapper.select("distinct SYMBOL,PARTY_ID");
        queryWrapper.eq("PARTY_ID", partyId);
        queryWrapper.in("SYMBOL", byType);
        List<ItemUserOptionalItem> list = itemUserOptionalItemService.list(queryWrapper);
        List<String> optionalSymbols = itemUserOptionalService.getOptionalSymbols(partyId);

        List<ItemUserOptionalItemDTO> models = BeanUtil.copyToList(list, ItemUserOptionalItemDTO.class);
        Set<String> allSymbos = new HashSet<>();

        for(ItemUserOptionalItemDTO dto: models){
            dto.setName(itemService.findBySymbol(dto.getSymbol()).getName());
            Realtime realtime = dataService.realtime(dto.getSymbol()).get(0);
            dto.setClose(realtime.getClose());
            dto.setChangeRatio(realtime.getChangeRatio());
            dto.setTurnoverRate(realtime.getTurnoverRate());
            dto.setVolumeRatio(realtime.getVolumeRatio());
            allSymbos.add(dto.getSymbol());
        }

        for(String symbol:  optionalSymbols ){
            if(allSymbos.contains(symbol)){
                continue;
            }
            ItemUserOptionalItemDTO dto = new ItemUserOptionalItemDTO();
            dto.setSymbol(symbol);
            Item bySymbol = itemService.findBySymbol(dto.getSymbol());
            if(!bySymbol.getType().equalsIgnoreCase(type)){
                continue;
            }
            dto.setName(bySymbol.getName());
            Realtime realtime = dataService.realtime(dto.getSymbol()).get(0);
            dto.setClose(realtime.getClose());
            dto.setChangeRatio(realtime.getChangeRatio());
            dto.setTurnoverRate(realtime.getTurnoverRate());
            dto.setVolumeRatio(realtime.getVolumeRatio());
            allSymbos.add(dto.getSymbol());
            models.add(dto);
        }
        return models;

    }
    /**
     *
     * @param partyId
     * @param name
     * @return
     */
    public ItemUserOptionalList findOne(String partyId, String name){
        QueryWrapper<ItemUserOptionalList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("PARTY_ID", partyId);
        queryWrapper.eq("NAME", name);
        return  baseMapper.selectOne(queryWrapper);
    }


}
