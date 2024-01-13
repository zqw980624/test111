package com.yami.trading.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.purchasing.Purchasing;
import com.yami.trading.dao.PurchasingMapper;
import com.yami.trading.service.PurchasingService;
import org.springframework.stereotype.Service;

@Service
public class PurchasingServiceImpl   extends ServiceImpl<PurchasingMapper, Purchasing> implements PurchasingService {
}
