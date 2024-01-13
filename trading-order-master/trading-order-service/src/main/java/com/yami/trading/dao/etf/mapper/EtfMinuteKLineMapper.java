package com.yami.trading.dao.etf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yami.trading.bean.etf.domain.EtfMinuteKLine;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * Etf分钟级k线图 Mapper 接口
 * </p>
 *
 * @author lucas
 * @since 2023-06-17 20:18:56
 */
@Mapper
public interface EtfMinuteKLineMapper extends BaseMapper<EtfMinuteKLine> {

}
