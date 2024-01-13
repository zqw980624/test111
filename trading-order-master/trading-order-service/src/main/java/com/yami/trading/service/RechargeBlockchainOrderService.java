package com.yami.trading.service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.model.RealNameAuthRecord;
import com.yami.trading.bean.model.RechargeBlockchainOrder;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface RechargeBlockchainOrderService  extends IService<RechargeBlockchainOrder> {


    Page pageRecord(Page page, String rolename, String orderNo, String userName, Date startTime, Date endTime,String status);
    Page pageRecords(Page page, String rolename, String orderNo, String userName, Date startTime, Date endTime,String status, String tx);

    /**
     * 手动到账
     * @param id
     */
    void manualReceipt(String id, BigDecimal amount,String operator_username);

    /**
     * 驳回申请
     * @param id
     * @param content
     */
    void refusalApply(String id, String content,String userName);

    long waitCount();

    public List<RechargeBlockchainOrder> findByPartyIdAndSucceeded(Serializable partyId, int succeeded);

    public RechargeBlockchainOrder findByOrderNo(String order_no);

    void saveOrder(RechargeBlockchainOrder recharge);
    //void saveOrders(RechargeBlockchainOrder recharge);

    /**
     * 查找当日内用户的所有充值订单
     *order_no
     * @param
     * @return
     */
    public List<RechargeBlockchainOrder> findByPartyIdAndToday(String partyId);
}
