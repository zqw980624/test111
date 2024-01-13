package com.yami.trading.service.data;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.data.domain.Kline;
import com.yami.trading.dao.data.KlineMapper;

/**
 * k线图数据Service
 * @author lucas
 * @version 2023-03-16
 */
@Service
@Transactional
public class KlineDBService extends ServiceImpl<KlineMapper, Kline> {



}
