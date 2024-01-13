package com.yami.trading.dao;

import com.yami.trading.model.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author HT
 * @since 2023-02-25 18:59:24
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}
