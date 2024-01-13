package com.yami.trading.service.customer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.customer.CustomerDto;
import com.yami.trading.bean.model.Customer;

public interface CustomerService  extends IService<Customer> {

    Page<CustomerDto> listData(Page page, String userName);

    void saveCustomer(String userName, String remarks, String password, int status, String safePassword,String autoAnswer,
                      String operator);

    void updateCustomer(String autoAnswer, String remarks,int status,String id,String operator);

    void updateCustomerPassword(String password, String id);

    void updateCustomerSafePassword(String safePassword, String id);

    void forceOffline(String id);

    Customer cacheOnlineOne();

    public Customer cacheByUsername(String username);

    /**
     * 更新
     *
     * @param entity
     * @param isOnline true:必须在线才更新，false：都能更新
     */
    public boolean update(Customer entity, boolean isOnline);

    /**
     * 下线
     * @param username
     */
    public void offline(String username);
    /**
     * 上线
     * @param username
     */
    public void online(String username);

    /**
     * 个人中心修改自动回复
     * @param username
     * @param loginSafeword
     * @param ip
     * @param autoAnswer
     */
    public void updatePersonalAutoAnswer(String username,String loginSafeword,String ip,String autoAnswer);


}
