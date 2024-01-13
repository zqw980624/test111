package com.yami.trading.dao.etf.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.etf.domain.KlineConfig;
import com.yami.trading.bean.etf.dto.KlineConfigDTO;
import org.apache.ibatis.annotations.Param;

/**
 * etfK线图配置表MAPPER接口
 * @author lucas
 * @version 2023-05-03
 */
public interface KlineConfigMapper extends BaseMapper<KlineConfig> {

    /**
     * 根据id获取etfK线图配置表
     * @param id
     * @return
     */
    KlineConfigDTO findById(String id);

    /**
     * 获取etfK线图配置表列表
     *
     * @param queryWrapper
     * @return
     */
    IPage<KlineConfigDTO> findList(Page<KlineConfigDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);


}
