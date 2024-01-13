package com.yami.trading.service.c2c;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.c2c.C2cOrder;
import com.yami.trading.bean.model.C2cPaymentMethod;
import com.yami.trading.bean.model.User;

import java.util.List;
import java.util.Map;

public interface C2cOrderService  extends IService<C2cOrder> {
    /**
     * 获取当天取消订单数据
     */
    long getTodayCancelOrderCount(String userId);

    public Map<String, Object> detail(C2cOrder crder);

    public void savePass(C2cOrder c2cOrder, String safeword, String operator_username);

    //public void savePassWd(C2cOrder c2cOrder, String safeword, String operator_username);


    public void saveOrderPass(C2cOrder c2cOrder);
    /*
     * 查询未完结订单数量，根据广告ID
     */
    public Long findNoEndingOrdersCountByAdvertId(String ctcAdvertId);

    /**
     * 手动放行
     * @param id
     */
    void manualRelease(C2cOrder c2cOrder,  String operator_username);

    public void saveOrderCancel(C2cOrder c2cOrder, String role);

    /**
     * 取消订单
     * @param id
     * @param reason
     */
    void orderCancel(String id, String reason);


    public Page pagedQuery(int pageNo, int pageSize, String direction, String state, String loginPartyId);

    public Page pagedBankCardOrderQuery(Page page, List<String> direction,String state,String userCode,String roleName,String orderNo);

    public Page pagedQuery(long pageNo, long pageSize, String status_int, String order_no_para, String user_code_para, String rolename_para,
                           String c2c_user_code_para, String c2c_user_type_int, String c2c_user_party_code_para,List<String> direction, String loginPartyId);

    public void saveOpen(C2cOrder c2cOrder,String remark);

    /**
     * 今日C2C订单
     */
    public List<C2cOrder> findByPartyIdAndToday(String partyId, String direction, String state);

    public void saveOpen(C2cOrder c2cOrder, User user);
    public void saveOpenRecharge(C2cOrder c2cOrder, User user);

    public void saveOpenWithdraw(C2cOrder c2cOrder, User user);

    public C2cOrder get(String order_no);

    public void saveOrderPay(String order_no, String safeword, String operator_username, String payment_method_id_order_pay);

    public void saveOrderPayPd(String order_no, String safeword, String operator_username, String payment_method_id_order_pay);

    public List<C2cPaymentMethod> getOrderPayments(String order_no,boolean c2cOrderFlag);

    public Long getNofinishOrderCount(String partyId);

    List<C2cOrder> getByPayId(String id);
}
