package com.yami.trading.dao.item;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.item.dto.ItemDTO;
import com.yami.trading.bean.item.domain.Item;

/**
 * 产品MAPPER接口
 * @author lucas
 * @version 2023-03-10
 */
public interface ItemMapper extends BaseMapper<Item> {

    /**
     * 根据id获取产品
     * @param id
     * @return
     */
    ItemDTO findById(String id);

    /**
     * 获取产品列表
     *
     * @param queryWrapper
     * @return
     */
    IPage <ItemDTO> findList(Page<ItemDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);


}
