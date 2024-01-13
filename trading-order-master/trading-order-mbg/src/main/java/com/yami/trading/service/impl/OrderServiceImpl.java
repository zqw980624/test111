package com.yami.trading.service.impl;

import com.yami.trading.model.Order;
import com.yami.trading.dao.OrderMapper;
import com.yami.trading.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author HT
 * @since 2023-02-25 18:59:24
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

}
