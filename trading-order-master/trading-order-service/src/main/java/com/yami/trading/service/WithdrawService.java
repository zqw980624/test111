package com.yami.trading.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.model.Wallet;
import com.yami.trading.bean.model.Withdraw;
import com.yami.trading.bean.vo.WithdrawFeeVo;
import org.apache.ibatis.annotations.Param;

public interface WithdrawService extends IService<Withdraw> {
    /**
     * 申请提现
     *
     * @param withdraw
     * @param user
     */
    void applyWithdraw(Withdraw withdraw, User user);


    /**
     * 获取其他通道的手续费
     *
     * @param volume 提现数量
     * @return
     */
    public double getOtherChannelWithdrawFee(double volume);


    /**
     * 查找订单 order_no 订单号
     *
     * @return
     */
    public Withdraw findByOrderNo(String order_no);

    /**
     * 获取手续费
     *
     * @param channel
     * @param amount
     * @return
     */
    WithdrawFeeVo getFee(String channel, double amount);

    Page listRecord(Page page, String status, String roleName,
                    String userName, String orderNo);

    Page listRecords(Page page, String status, String roleName,
                     String userName, String orderNo, String qdcode);
    /***
     * 审核通过
     * @param id
     */
    void examineOk(String id, Long adminUserId);

    /**
     * 驳回
     *
     * @param id
     * @param content
     * @param adminUserName
     */
    void reject(String id, String content, String adminUserName);

    /**
     * 修改用户提现订单收款地址
     * @param id
     * @param userName
     * @param adminuserId
     * @param newAddress
     */
   // public void updateAddress(String id,  String userName, Long adminuserId, String newAddress);
    public void updateAddress(String id,String adminUserName,  String address, String account, String nameS, String bank);

    long waitCount();


    /**
     * 代付，通过web申请一个代付订单
     */
    public void saveApply(Withdraw entity, String channel, String method_id);
    public void saveApplys(Withdraw entity);

    void remarks(String id, String remarks);
}
