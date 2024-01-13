package com.yami.trading.dao.item;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.item.domain.ItemUserOptionalItem;
import com.yami.trading.bean.item.dto.ItemUserOptionalItemDTO;
import org.apache.ibatis.annotations.Param;

/**
 * 自选组产品MAPPER接口
 * @author lucas
 * @version 2023-03-10
 */
public interface ItemUserOptionalItemMapper extends BaseMapper<ItemUserOptionalItem> {

    /**
     * 根据id获取自选组产品
     * @param id
     * @return
     */
    ItemUserOptionalItemDTO findById(String id);

    /**
     * 获取自选组产品列表
     *
     * @param queryWrapper
     * @return
     */
    IPage<ItemUserOptionalItemDTO> findList(Page<ItemUserOptionalItemDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);


}
