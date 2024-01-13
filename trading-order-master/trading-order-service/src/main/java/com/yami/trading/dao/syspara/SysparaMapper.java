package com.yami.trading.dao.syspara;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.yami.trading.bean.syspara.domain.Syspara;
import com.yami.trading.bean.syspara.dto.SysparaDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 配置参数MAPPER接口
 * @author lucas
 * @version 2023-03-17
 */
public interface SysparaMapper extends BaseMapper<Syspara> {

    /**
     * 根据id获取配置参数
     * @param id
     * @return
     */
    SysparaDTO findById(String id);

    void updateBatch(@Param("sysparaList") List<Syspara> sysparaList);
    /**
     * 获取配置参数列表
     *
     * @param queryWrapper
     * @return
     */
    IPage<SysparaDTO> findList(Page<SysparaDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);


}
