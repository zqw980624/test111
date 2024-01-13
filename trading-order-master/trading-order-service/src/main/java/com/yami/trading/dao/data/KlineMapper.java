package com.yami.trading.dao.data;

import cn.hutool.db.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.yami.trading.bean.data.domain.Kline;
import com.yami.trading.bean.data.dto.KlineDTO;
import org.apache.ibatis.annotations.Param;

/**
 * k线图数据MAPPER接口
 * @author lucas
 * @version 2023-03-16
 */
public interface KlineMapper extends BaseMapper<Kline> {




}
