package com.yami.trading.dao.item;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yami.trading.bean.item.domain.ItemUserOptional;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yami.trading.bean.item.dto.ItemUserOptionalDTO;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
/**
 * 用户自选MAPPER接口
 * @author lucas
 * @version 2023-03-10
 */
public interface ItemUserOptionalMapper extends BaseMapper<ItemUserOptional> {

    /**
     * 根据id获取用户自选
     * @param id
     * @return
     */
    ItemUserOptionalDTO findById(String id);

    /**
     * 获取用户自选列表
     *
     * @param queryWrapper
     * @return
     */
    IPage <ItemUserOptionalDTO> findList(Page<ItemUserOptionalDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);


}
