package com.yami.trading.dao.data;

import cn.hutool.db.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.yami.trading.bean.data.domain.Realtime;
import com.yami.trading.bean.data.dto.RealtimeDTO;
import org.apache.ibatis.annotations.Param;

/**
 * 实时价格MAPPER接口
 * @author lucas
 * @version 2023-03-16
 */
public interface RealtimeMapper extends BaseMapper<Realtime> {




}
