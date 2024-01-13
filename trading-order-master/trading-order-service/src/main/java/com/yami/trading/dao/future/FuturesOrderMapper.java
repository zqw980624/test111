package com.yami.trading.dao.future;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.future.domain.FuturesOrder;
import com.yami.trading.bean.future.dto.ProfitLossConfigDTO;
import com.yami.trading.bean.future.dto.TFuturesOrderDTO;
import com.yami.trading.bean.future.query.FuturesOrderQuery;
import com.yami.trading.bean.future.query.ProfitLossConfigQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 交割合约订单MAPPER接口
 * @author lucas
 * @version 2023-04-08
 */
public interface FuturesOrderMapper extends BaseMapper<FuturesOrder> {


    IPage<TFuturesOrderDTO> listRecord(Page page, @Param("query") FuturesOrderQuery query);

    List<FuturesOrder> findByHourAndSate(@Param("state")String state, @Param("roleName")String roleName);

    /**
     * 根据id获取交割合约订单
     * @param id
     * @return
     */
    TFuturesOrderDTO findById(String id);

    /**
     * 获取交割合约订单列表
     *
     * @param queryWrapper
     * @return
     */
    IPage<TFuturesOrderDTO> findList(Page<TFuturesOrderDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);


}
