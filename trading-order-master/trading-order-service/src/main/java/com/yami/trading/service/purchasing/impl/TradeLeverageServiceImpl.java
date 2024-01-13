package com.yami.trading.service.purchasing.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.purchasing.TradeLeverage;
import com.yami.trading.dao.TradeLeverageMapper;
import com.yami.trading.service.purchasing.TradeLeverageService;
import org.springframework.stereotype.Service;

@Service
public class TradeLeverageServiceImpl  extends ServiceImpl<TradeLeverageMapper,TradeLeverage> implements TradeLeverageService {
}
