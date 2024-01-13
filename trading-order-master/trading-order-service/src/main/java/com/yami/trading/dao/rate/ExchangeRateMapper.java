package com.yami.trading.dao.rate;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.rate.domain.ExchangeRate;
import com.yami.trading.bean.rate.dto.ExchangeRateDTO;
import org.apache.ibatis.annotations.Param;

/**
 * 汇率管理MAPPER接口
 * @author lucas
 * @version 2023-03-28
 */
public interface ExchangeRateMapper extends BaseMapper<ExchangeRate> {

    /**
     * 根据id获取汇率管理
     * @param id
     * @return
     */
    ExchangeRateDTO findById(String id);

    /**
     * 获取汇率管理列表
     *
     * @param queryWrapper
     * @return
     */
    IPage<ExchangeRateDTO> findList(Page<ExchangeRateDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);


}
