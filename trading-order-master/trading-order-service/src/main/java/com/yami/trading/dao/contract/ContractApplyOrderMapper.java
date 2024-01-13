package com.yami.trading.dao.contract;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.contract.domain.ContractApplyOrder;
import com.yami.trading.bean.contract.dto.ContractApplyOrderDTO;
import com.yami.trading.bean.contract.dto.ContractOrderDTO;
import com.yami.trading.bean.contract.query.ContractApplyOrderQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 订单表MAPPER接口
 * @author lucas
 * @version 2023-03-29
 */
public interface ContractApplyOrderMapper extends BaseMapper<ContractApplyOrder> {

    /**
     * 根据id获取订单表
     * @param id
     * @return
     */
    ContractApplyOrderDTO findById(String id);

    /**
     * 获取订单表列表
     *
     * @param queryWrapper
     * @return
     */
    IPage<ContractApplyOrderDTO> findList(Page<ContractApplyOrderDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);

    IPage<ContractApplyOrderDTO> listRecord(Page page, @Param("query") ContractApplyOrderQuery query);
}
