package com.yami.trading.dao.customer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.customer.CustomerDto;
import com.yami.trading.bean.model.Customer;
import org.apache.ibatis.annotations.Param;
public interface CustomerMapper  extends BaseMapper<Customer> {

    Page<CustomerDto> listPage(@Param("userName") String u,Page page);

}
