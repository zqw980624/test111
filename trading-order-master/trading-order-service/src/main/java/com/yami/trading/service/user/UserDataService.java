package com.yami.trading.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.contract.domain.ContractOrder;
import com.yami.trading.bean.exchange.ExchangeApplyOrder;
import com.yami.trading.bean.future.domain.FuturesOrder;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.model.UserData;
import com.yami.trading.bean.user.dto.ChildrenLever;
import com.yami.trading.bean.user.dto.UserBenefitsDto;
import com.yami.trading.common.constants.Constants;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface UserDataService  extends IService<UserData> {
    public void saveUserData(UserData entity);
    public Page userAll(Page page,Date startTime,Date endTime);

    Map sumAll(Date startTime,Date endTime);

    Page listUserGenefits(Page page,  Date startTime,Date endTime, String userName,List<String> children);

    long countTodayRechargeUser();
    public void saveGiftMoneyHandle(String partyId, double amount);

    UserBenefitsDto daySumData( Date startTime,
                                Date endTime,List<String> userIds);


    UserBenefitsDto daySumDatas( Date startTime,
                                Date endTime,List<String> userIds,String userCodes);

    public List<Map<String, UserData>> cacheByPartyIds(List<String> partyIds);

    /**
     * 1、api注册 2、推荐关系更改
     */
    public void saveRegister(String userId);


    public Map<String, UserData> cacheByPartyId(String partyId);

    void saveRechargeHandle(String partyId, double amount, String symbol);
    /**
     * 合约平仓
     *
     * @param partyId
     * @param amount
     */
    public void saveClose(ContractOrder order) ;
    /**
     * 交割平仓
     *
     * @param partyId
     * @param amount
     */
    public void saveFuturesClose(FuturesOrder order);
    /**
     * 卖股票
     */
    public void saveSell(ExchangeApplyOrder order);

    /**
     * 卖股票
     */
    public void saveSellYd(ExchangeApplyOrder order,String pid);
    /**
     * 买股票
     */
    public void saveBuyYd(ExchangeApplyOrder order);

    /**
     * 买股票
     */
    public void saveBuy(ExchangeApplyOrder order);

    /**
     * 提现
     *
     * @param partyId
     * @param amount
     */
    public void saveWithdrawHandle(String partyId, double amount, double amount_fee, String symbol);



    public ChildrenLever cacheChildrenLever4(String partyId);

    /**
     * 资金盘
     */
    public List<Map<String, Object>> getChildrenLevelPagedForGalaxy(int pageNo, int pageSize, String partyId, Integer level);

    List<Map<String, UserData>> findByPartyIds(List<String> children);
}
