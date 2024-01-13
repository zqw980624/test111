package com.yami.trading.dao.item;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.item.dto.ItemSummaryDTO;
import com.yami.trading.bean.item.domain.ItemSummary;

/**
 * 简况MAPPER接口
 * @author lucas
 * @version 2023-05-01
 */
public interface ItemSummaryMapper extends BaseMapper<ItemSummary> {

    /**
     * 根据id获取简况
     * @param id
     * @return
     */
    ItemSummaryDTO findById(String id);

    /**
     * 获取简况列表
     *
     * @param queryWrapper
     * @return
     */
    IPage <ItemSummaryDTO> findList(Page<ItemSummaryDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);


}
