package com.yami.trading.dao.future;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.future.domain.ProfitLossConfig;
import com.yami.trading.bean.future.dto.FuturesParaDTO;
import com.yami.trading.bean.future.dto.ProfitLossConfigDTO;
import com.yami.trading.bean.future.query.FuturesParaQuery;
import com.yami.trading.bean.future.query.ProfitLossConfigQuery;
import org.apache.ibatis.annotations.Param;

/**
 * 交割合约MAPPER接口
 * @author lucas
 * @version 2023-04-08
 */
public interface ProfitLossConfigMapper extends BaseMapper<ProfitLossConfig> {

    /**
     * 根据id获取交割合约
     * @param id
     * @return
     */
    ProfitLossConfigDTO findById(String id);

    /**
     * 获取交割合约列表
     *
     * @param queryWrapper
     * @return
     */
    IPage<ProfitLossConfigDTO> findList(Page<ProfitLossConfigDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);

    IPage<ProfitLossConfigDTO> listRecord(Page page, @Param("query") ProfitLossConfigQuery query);

}
