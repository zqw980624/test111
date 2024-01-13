package com.yami.trading.dao.item;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yami.trading.bean.item.domain.ItemUserOptionalList;
import com.yami.trading.bean.item.dto.ItemUserOptionalListDTO;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
/**
 * 自选分组MAPPER接口
 * @author lucas
 * @version 2023-03-10
 */
public interface ItemUserOptionalListMapper extends BaseMapper<ItemUserOptionalList> {

    /**
     * 根据id获取自选分组
     * @param id
     * @return
     */
    ItemUserOptionalListDTO findById(String id);

    /**
     * 获取自选分组列表
     *
     * @param queryWrapper
     * @return
     */
    IPage <ItemUserOptionalListDTO> findList(Page<ItemUserOptionalListDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);


}
