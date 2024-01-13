package com.yami.trading.dao.syspara;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.yami.trading.bean.syspara.domain.OpenClose;
import com.yami.trading.bean.syspara.dto.OpenCloseDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;

/**
 * 开盘停盘时间设置MAPPER接口
 * @author lucas
 * @version 2023-05-20
 */
public interface OpenCloseMapper extends BaseMapper<OpenClose> {

    /**
     * 根据id获取开盘停盘时间设置
     * @param id
     * @return
     */
    OpenCloseDTO findById(String id);

    /**
     * 获取开盘停盘时间设置列表
     *
     * @param queryWrapper
     * @return
     */
    IPage<OpenCloseDTO> findList(Page<OpenCloseDTO> page, @Param(Constants.WRAPPER) QueryWrapper queryWrapper);


}
