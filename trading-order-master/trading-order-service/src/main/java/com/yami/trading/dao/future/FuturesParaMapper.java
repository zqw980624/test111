package com.yami.trading.dao.future;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.future.domain.FuturesPara;
import com.yami.trading.bean.future.dto.FuturesParaDTO;
import com.yami.trading.bean.future.query.FuturesParaQuery;
import org.apache.ibatis.annotations.Param;

/**
 * 交割合约管理MAPPER接口
 * @author lucas
 * @version 2023-04-08
 */
public interface FuturesParaMapper extends BaseMapper<FuturesPara> {

    /**
     * 根据id获取交割合约管理
     * @param id
     * @return
     */
    FuturesParaDTO findById(String id);

    /**
     * 获取交割合约管理列表
     *
     * @param queryWrapper
     * @return
     */
    IPage<FuturesParaDTO> findList(Page<FuturesParaDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);



}
