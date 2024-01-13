package com.yami.trading.service.item;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.item.domain.ItemUserOptionalItem;
import com.yami.trading.bean.item.domain.ItemUserOptionalList;
import com.yami.trading.dao.item.ItemUserOptionalItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 自选组产品Service
 *
 * @author lucas
 * @version 2023-03-10
 */
@Service
@Transactional
public class ItemUserOptionalItemService extends ServiceImpl<ItemUserOptionalItemMapper, ItemUserOptionalItem> {

        @Autowired
    private ItemUserOptionalService ItemUserOptionalService;
    /**
     * 查询分组下所有的币对
     *
     * @param partyId
     * @return
     */
    public List<String> findListByPartyId(String partyId, String listId) {
        QueryWrapper<ItemUserOptionalItem> queryWrapper = new QueryWrapper();
        queryWrapper.eq("LIST_ID", listId);
        queryWrapper.eq("PARTY_ID", partyId);
        queryWrapper.select("SYMBOL");
        List<ItemUserOptionalItem> list = list(queryWrapper);
        return list.stream().map(ItemUserOptionalItem::getSymbol).collect(Collectors.toList());
    }

    public ItemUserOptionalItem findOne(String partyId,  String listId, String symbol){
        QueryWrapper<ItemUserOptionalItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("PARTY_ID", partyId);
        queryWrapper.eq("LIST_ID", listId);
        queryWrapper.eq("SYMBOL", symbol);
        return  baseMapper.selectOne(queryWrapper);
    }

    public boolean findOne(String partyId,  String symbol){
        QueryWrapper<ItemUserOptionalItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("PARTY_ID", partyId);
        queryWrapper.eq("SYMBOL", symbol);
        boolean isFind = count(queryWrapper) >=1;
       if(isFind){
           return true;
       }else{
           return ItemUserOptionalService.getOptionalSymbols(partyId).contains(symbol);
       }


    }
}
