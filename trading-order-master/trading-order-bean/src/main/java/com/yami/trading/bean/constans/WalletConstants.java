package com.yami.trading.bean.constans;

public class WalletConstants {


    /**
     * 充币
     */
    public static final String MONEYLOG_CONTENT_RECHARGE = "recharge";
    /**
     * 提币
     */
    public static final String MONEYLOG_CONTENT_WITHDRAW = "withdraw";

    /**
     * C2C
     */
    public static final String LOG_CATEGORY_C2C = "c2c";

    /**
     * 永续合约平仓
     */
    public static final String MONEYLOG_CONTENT_CONTRACT_CLOSE = "contract_close";
    /**
     * 永续合约建仓
     */
    public static final String MONEYLOG_CONTENT_CONTRACT_OPEN = "contract_open";
    /**
     * 永续合约撤单
     */

    public static final String MONEYLOG_CONTENT_CONTRACT_CONCEL = "contract_cancel";
    /**
     * 手续费
     */
    public static final String MONEYLOG_CONTENT_FEE = "fee";

    /**
     * 币币买入
     */
    public static final String MONEYLOG_CONTENT_EXCHANGE_OPEN = "exchange_open";

    /**
     * 币币卖出
     */
    public static final String MONEYLOG_CONTENT_EXCHANGE_CLOSE = "exchange_close";
    /**
     * 币币取消
     */
    public static final String MONEYLOG_CONTENT_EXCHANGE_CANCEL = "exchange_cancel";
    /**
     * 矿机买入
     */
    public static final String MONEYLOG_CONTENT_MINER_BUY = "miner_buy";
    /**
     * 矿机退回本金
     */
    public static final String MONEYLOG_CONTENT_MINER_BACK = "miner_back";

    /**
     * 质押2.0下单
     */
    public static final String MONEYLOG_CONTENT_GALAXY_BUY = "galaxy_buy";

    /**
     * 质押2.0赎回
     */
    public static final String MONEYLOG_CONTENT_GALAXY_BACK = "galaxy_back";

    /**
     * 质押2.0推荐收益
     */
    public static final String MONEYLOG_CONTENT_GALAXY_RECOM_PROFIT = "galaxy_recom_profit";

    /**
     * 质押2.0收益
     */
    public static final String MONEYLOG_CONTENT_GALAXY_PROFIT = "galaxy_profit";

    /**
     * 质押借币 - 冻结
     */
    public static final String MONEYLOG_CONTENT_LOAN_FROZEN = "loan_frozen";

    /**
     * 质押借币 - 解冻
     */
    public static final String MONEYLOG_CONTENT_LOAN_THAW = "loan_thaw";

    /**
     * 质押借币 - 强平
     */
    public static final String MONEYLOG_CONTENT_LOAN_CLOSEOUT = "loan_closeout";

    /**
     * 质押借币 - 借款
     */
    public static final String MONEYLOG_CONTENT_LOAN_ADD = "loan_add";

    /**
     * 质押借币 - 还款
     */
    public static final String MONEYLOG_CONTENT_LOAN_REPAY = "loan_repay";

    /**
     * 矿机收益
     */
    public static final String MONEYLOG_CONTENT_MINER_PROFIT = "miner_profit";

    /**
     * 矿机推荐收益
     */
    public static final String MONEYLOG_CONTENT_MINER_RECOM_PROFIT = "miner_recom_profit";

    /**
     * 团队奖励
     */
    public static final String MONEYLOG_CONTENT_MINER_TEAM_PROFIT = "miner_team_profit";
    /**
     * 社区奖励
     */
    public static final String MONEYLOG_CONTENT_MINER_COMMUNITY_PROFIT = "miner_community_profit";

    /**
     * 理财购买
     */
    public static final String MONEYLOG_CONTENT_FINANCE_BUY = "finance_buy";

    /**
     * 理财赎回
     */
    public static final String MONEYLOG_CONTENT_FINANCE_BACK = "finance_back";
    /**
     * 理财收益
     */
    public static final String MONEYLOG_CONTENT_FINANCE_PROFIT = "finance_profit";
    /**
     * 理财推荐收益
     */
    public static final String MONEYLOG_CONTENT_FINANCE_RECOM_PROFIT = "finance_recom_profit";

    /**
     * 跟单基金策略买入
     */
    public static final String MONEYLOG_CONTENT_FUND_OPEN = "fund_open";
    /**
     * 跟单基金策略平仓
     */
    public static final String MONEYLOG_CONTENT_FUND_CLOSE = "fund_close";
    /**
     * 跟单基金策略手续费
     */
    public static final String MONEYLOG_CONTENT_FUND_FEE = "fund_fee";
    /**
     * c2c卖币
     */
    public static final String MONEYLOG_CONTENT_C2C_SELL = "c2c_sell";
    /**
     * c2c买币
     */
    public static final String MONEYLOG_CONTENT_C2C_BUY = "c2c_buy";
    /**
     * c2c订单取消
     */
    public static final String MONEYLOG_CONTENT_C2C_CANCEL = "c2c_cancel";
    /**
     * otc卖币
     */
    public static final String MONEYLOG_CONTENT_OTC_SELL = "otc_sell";
    /**
     * otc买币
     */
    public static final String MONEYLOG_CONTENT_OTC_BUY = "otc_buy";
    /**
     * otc订单取消
     */
    public static final String MONEYLOG_CONTENT_OTC_CANCEL = "otc_cancel";
    /**
     * 跟单手续费
     */
    public static final String MONEYLOG_CONTENT_FOLLOW_UP_FEE = "follow_up_fee";
    /**
     * ICO中签
     */
    public static final String MONEYLOG_CONTENT_ICO_DRAW = "ico_draw_win";
    /**
     * ICO购买
     */
    public static final String MONEYLOG_CONTENT_ICO_BUY = "ico_buy";
    /**
     * ICO上市
     */
    public static final String MONEYLOG_CONTENT_ICO_MARKET = "ico_market";
    /**
     * 币币杠杆平仓
     */
    public static final String MONEYLOG_CONTENT_EXCHANGE_LEVER_CLOSE = "exchange_lever_close";
    /**
     * 币币杠杆利息
     */
    public static final String MONEYLOG_CONTENT_EXCHANGE_LEVER_INTEREST = "exchange_lever_interest";
    /**
     * 币币杠杆开仓
     */
    public static final String MONEYLOG_CONTENT_EXCHANGE_LEVER_OPEN = "exchange_lever_open";
    /**
     * 奖励
     */
    public static final String MONEYLOG_CONTENT_REWARD = "reward";
    /**
     * 签到奖励
     */
    public static final String MONEYLOG_CONTENT_SIGN_IN_PROFIT = "sign_in_profit";
    /**
     * 活动解锁
     */
    public static final String MONEYLOG_CONTENT_ACTIVITY_UNLOCK = "activity_unlock";
    /**
     * 系统锁定转移
     */
    public static final String MONEYLOG_CONTENT_SYS_LOCK = "sys_lock";
    /**
     * 系统增加锁定金额
     */
    public static final String MONEYLOG_CONTENT_SYS_MONEY_ADD_LOCK = "sys_add_lock";
    /**
     * 系统增加锁定金额
     */
    public static final String MONEYLOG_CONTENT_SYS_MONEY_SUB_LOCK = "sys_sub_lock";
    /**
     * 债权买入
     */
    public static final String MONEYLOG_CONTENT_BOND_BUY = "bond_buy";
    /**
     * 债权退回本金
     */
    public static final String MONEYLOG_CONTENT_BOND_BACK = "bond_back";
    /**
     * 债权收益
     */
    public static final String MONEYLOG_CONTENT_BOND_PROFIT = "bond_profit";

    /**
     * 交易账户(USDT)
     */
    public static final String WALLET_USDT = "USDT";
    /**
     * BTC
     */
    public static final String WALLETEXTEND_BTC = "BTC";
    /**
     * ETH
     */
    public static final String WALLETEXTEND_ETH = "ETH";

    /**
     * XRP
     */
    public static final String WALLETEXTEND_XRP = "XRP";
    /**
     * EOS
     */
    public static final String WALLETEXTEND_EOS = "EOS";
    /**
     * LTC
     */
    public static final String WALLETEXTEND_LTC = "LTC";
    /**
     * MLCC
     */
    public static final String WALLETEXTEND_MLCC = "MLCC";



    /**
     * 合约交易
     */
    public static final String MONEYLOG_CATEGORY_CONTRACT = "contract";
    /**
     * 法币交易
     */
    public static final String MONEYLOG_CATEGORY_COIN = "coin";

    /**
     * 币币交易
     */
    public static final String MONEYLOG_CATEGORY_EXCHANGE = "exchange";
    /**
     * 矿机交易
     */
    public static final String MONEYLOG_CATEGORY_MINER = "miner";

    /**
     * 质押借币
     */
    public static final String MONEYLOG_CATEGORY_LOAN = "loan";

    /**
     * 基金交易
     */
    public static final String MONEYLOG_CATEGORY_FUND = "fund";
    /**
     * 质押2.0交易
     */
    public static final String MONEYLOG_CATEGORY_GALAXY = "galaxy";
    /**
     * c2c交易
     */
    public static final String MONEYLOG_CATEGORY_C2C = "c2c";
    /**
     * otc交易
     */
    public static final String MONEYLOG_CATEGORY_OTC = "otc";
    /**
     * ico交易
     */
    public static final String MONEYLOG_CATEGORY_ICO = "ico";
    /**
     * 币币杠杆交易
     */
    public static final String MONEYLOG_CATEGORY_EXCHANGE_LEVER = "exchange_lever";
    /**
     * 系统奖励
     */
    public static final String MONEYLOG_CATEGORY_REWARD = "reward";

}
