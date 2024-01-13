package com.yami.trading.service.etf;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.etf.domain.EtfMinuteKLine;
import com.yami.trading.dao.etf.mapper.EtfMinuteKLineMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Etf分钟级k线图 服务实现类
 * </p>
 *
 * @author lucas
 * @since 2023-06-17 20:18:56
 */
@Service
public class EtfMinuteKLineServiceImpl extends ServiceImpl<EtfMinuteKLineMapper, EtfMinuteKLine> implements EtfMinuteKLineService {

}
