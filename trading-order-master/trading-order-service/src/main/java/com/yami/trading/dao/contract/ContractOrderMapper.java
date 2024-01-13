package com.yami.trading.dao.contract;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.contract.domain.ContractOrder;
import com.yami.trading.bean.contract.dto.ContractApplyOrderDTO;
import com.yami.trading.bean.contract.dto.ContractOrderDTO;
import com.yami.trading.bean.contract.query.ContractApplyOrderQuery;
import com.yami.trading.bean.contract.query.ContractOrderQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 非按金额订单MAPPER接口
 * @author lucas
 * @version 2023-03-29
 */
public interface ContractOrderMapper extends BaseMapper<ContractOrder> {

    /**
     * 根据id获取非按金额订单
     * @param id
     * @return
     */
    ContractOrderDTO findById(String id);

    /**
     * 获取非按金额订单列表
     *
     * @param queryWrapper
     * @return
     */
    IPage<ContractOrderDTO> findList(Page<ContractOrderDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);


    IPage<ContractOrderDTO> listRecord(Page page,@Param("query")  ContractOrderQuery query);

    void batchUpdateBuffer(List<ContractOrder> list);
}
