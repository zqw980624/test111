package com.yami.trading.dao.rate;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.rate.domain.UserRateConfig;
import com.yami.trading.bean.rate.dto.UserRateConfigDTO;
import org.apache.ibatis.annotations.Param;

/**
 * 用户汇率管理MAPPER接口
 * @author lucas
 * @version 2023-03-28
 */
public interface UserRateConfigMapper extends BaseMapper<UserRateConfig> {

    /**
     * 根据id获取用户汇率管理
     * @param id
     * @return
     */
    UserRateConfigDTO findById(String id);

    /**
     * 获取用户汇率管理列表
     *
     * @param queryWrapper
     * @return
     */
    IPage<UserRateConfigDTO> findList(Page<UserRateConfigDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);


}
