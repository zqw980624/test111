package com.yami.trading.dao.etf.mapper;

import com.yami.trading.bean.etf.domain.EtfSecKLine;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * Etf秒级k线图 Mapper 接口
 * </p>
 *
 * @author HT
 * @since 2023-05-18 17:27:13
 */
@Mapper
public interface EtfSecKLineMapper extends BaseMapper<EtfSecKLine> {

}
