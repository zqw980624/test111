package com.yami.trading.service.item;

import com.yami.trading.bean.item.domain.ItemLeverage;
import com.yami.trading.bean.item.dto.ItemLeverageDTO;
import com.yami.trading.bean.item.mapstruct.TItemLeverageWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.dao.item.TItemLeverageMapper;

import java.util.List;

/**
 * 产品杠杠倍数Service
 * @author lucas
 * @version 2023-03-10
 */
@Service
@Transactional
public class ItemLeverageService extends ServiceImpl<TItemLeverageMapper, ItemLeverage> {

	@Autowired
	private TItemLeverageWrapper tItemLeverageWrapper;

	/**
	 * 根据id查询
	 * @param id
	 * @return
	 */
	public ItemLeverageDTO findById(String id) {
		return baseMapper.findById ( id );
	}

	/**
	 * 自定义分页检索
	 * @param page
	 * @param queryWrapper
	 * @return
	 */
	public IPage <ItemLeverageDTO> findPage(Page <ItemLeverageDTO> page, QueryWrapper queryWrapper) {
		queryWrapper.eq ("a.del_flag", 0 ); // 排除已经删除
		return  baseMapper.findList (page, queryWrapper);
	}

	/**
	 * 通过产品找id找配置的杠杆
	 * @param itemId
	 * @return
	 */
	public List<ItemLeverageDTO> findByItemId(String itemId){
		List<ItemLeverage> list = this.lambdaQuery().eq(ItemLeverage::getItemId, itemId).orderByAsc(ItemLeverage::getCreateTime).list();
		return tItemLeverageWrapper.toDTO(list);
	}

}
