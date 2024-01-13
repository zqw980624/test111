package com.yami.trading.service.item;

import cn.hutool.core.collection.CollectionUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.item.dto.ItemSummaryDTO;
import com.yami.trading.bean.item.domain.ItemSummary;
import com.yami.trading.dao.item.ItemSummaryMapper;

import java.util.Collection;
import java.util.List;

/**
 * 简况Service
 * @author lucas
 * @version 2023-05-01
 */
@Service
@Transactional
public class ItemSummaryService extends ServiceImpl<ItemSummaryMapper, ItemSummary> {

	/**
	 * 根据id查询
	 * @param id
	 * @return
	 */
	public ItemSummaryDTO findById(String id) {
		return baseMapper.findById ( id );
	}

	/**
	 * 自定义分页检索
	 * @param page
	 * @param queryWrapper
	 * @return
	 */
	public IPage <ItemSummaryDTO> findPage(Page <ItemSummaryDTO> page, QueryWrapper queryWrapper) {
		queryWrapper.eq ("a.del_flag", 0 ); // 排除已经删除
		return  baseMapper.findList (page, queryWrapper);
	}

	public ItemSummary getOrNewOne(String symbol){
		QueryWrapper<ItemSummary> itemSummaryQueryWrapper = new QueryWrapper<>();
		itemSummaryQueryWrapper.eq("symbol", symbol);
		List<ItemSummary> list = list(itemSummaryQueryWrapper);
		if(CollectionUtil.isEmpty(list)){
			ItemSummary summary = new ItemSummary();
			summary.setSymbol(symbol);
			summary.setLang("zh-CN");
			save(summary);
			return summary;
		}
		return list.get(0);
	}
	public ItemSummary getOneByLang(String symbol, String lang){
		QueryWrapper<ItemSummary> itemSummaryQueryWrapper = new QueryWrapper<>();
		itemSummaryQueryWrapper.eq("symbol", symbol);
		itemSummaryQueryWrapper.eq("lang", lang);
		List<ItemSummary> list = list(itemSummaryQueryWrapper);
		if(CollectionUtil.isEmpty(list)){
			return null;
		}
		return list.get(0);
	}
}
