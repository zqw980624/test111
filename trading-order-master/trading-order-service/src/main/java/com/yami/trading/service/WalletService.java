package com.yami.trading.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.contract.domain.ContractOrder;
import com.yami.trading.bean.future.domain.FuturesOrder;
import com.yami.trading.bean.model.Wallet;
import com.yami.trading.bean.model.WalletExtend;
import com.yami.trading.bean.user.dto.AssetsDto;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface WalletService extends IService<Wallet> {
    Wallet findByUserId(String userId);
    public List<WalletExtend> findExtend(String partyId, List<String> list_symbol);

    public List<WalletExtend> findExtend(String partyId);

    public WalletExtend saveExtendByPara(String userId, String wallettype);


    public Wallet saveWalletByPartyId(String partyId);
    /**
     *
     * @param partyId
     * @param walletType
     * @param amount 修改的金额
     */
    public void updateExtend(String partyId, String walletType, double amount);

    /**
     * 获取总数
     * @return
     */
    BigDecimal sumMoney();

    void updateMoney(String symbol, String userId, BigDecimal moneyRevise);

    BigDecimal sumMoneyAgent(String userCode);

    /**
     * 更新账号余额
     * @param userId 用户id
     * @param money  交易金额
     * @param amountFee  手续费
     * @param category   交易类型
     * @param walletType  账户类型  usdt  eth
     * @param contentType  资金日志类型
     * @param log  日志
     */
    void updateMoney(String symbol,String userId, BigDecimal money, BigDecimal amountFee,
                     String category, String walletType, String contentType, String log);

//    /**
//     * 获取总资产
//     * @param userId
//     * @return
//     */
//    AssetsDto getAssets(String userId);

    /**
     * 获取其他拓展钱包币种的余额
     */
    public Map<String, Object> getMoneyAll(Serializable partyId);
    public Map<String, Object> getMoneyAll(Serializable partyId, String symbolType);
    /*
     * 获取 单个订单 永续合约总资产、总保证金、总未实现盈利
     */
    public Map<String, BigDecimal> getMoneyContractByOrder(ContractOrder order);

    /*
     * 获取 单个订单 交割合约总资产、总未实现盈利
     */
    public Map<String, Double> getMoneyFuturesByOrder(FuturesOrder order);

    void update(String userId, double gift_sum);

    void updateTo(String userId, double gift_sum);

    /*
     * 获取 所有订单 永续合约总资产、总保证金、总未实现盈利
     */
    public Map<String, Double> getMoneyContract(Serializable partyId, String symbolType);
    public void updateExtendWithLockAndFreeze(String partyId, String walletType, double amount, double lockAmount, double freezeAmount);

    public void updateWithLockAndFreeze(String partyId, double amount,double lockAmount,double freezeAmount);

}
