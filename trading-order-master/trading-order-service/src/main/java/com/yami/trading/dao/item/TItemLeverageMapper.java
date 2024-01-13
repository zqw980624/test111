package com.yami.trading.dao.item;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.item.dto.ItemLeverageDTO;
import com.yami.trading.bean.item.domain.ItemLeverage;

/**
 * 产品杠杠倍数MAPPER接口
 * @author lucas
 * @version 2023-03-10
 */
public interface TItemLeverageMapper extends BaseMapper<ItemLeverage> {

    /**
     * 根据id获取产品杠杠倍数
     * @param id
     * @return
     */
    ItemLeverageDTO findById(String id);

    /**
     * 获取产品杠杠倍数列表
     *
     * @param queryWrapper
     * @return
     */
    IPage <ItemLeverageDTO> findList(Page<ItemLeverageDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);


}
