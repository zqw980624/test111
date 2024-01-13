package com.yami.trading.dao.data;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.data.domain.Symbols;
import com.yami.trading.bean.data.dto.SymbolsDTO;
import org.apache.ibatis.annotations.Param;

/**
 * 币对MAPPER接口
 * @author lucas
 * @version 2023-03-17
 */
public interface SymbolsMapper extends BaseMapper<Symbols> {

    /**
     * 根据id获取币对
     * @param id
     * @return
     */
    SymbolsDTO findById(String id);

    /**
     * 获取币对列表
     *
     * @param queryWrapper
     * @return
     */
    IPage<SymbolsDTO> findList(Page<SymbolsDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);


}
