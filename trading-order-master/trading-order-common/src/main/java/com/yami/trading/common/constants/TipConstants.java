package com.yami.trading.common.constants;

import com.yami.trading.common.util.PropertiesUtil;

import java.util.HashMap;
import java.util.Map;

public class TipConstants {

    /**
     * C2C订单模块
     */
    public static final String C2C_ORDER = "OP_ADMIN_C2C_ORDER_TIP";
    /**
     * C2C申诉模块
     */
    public static final String C2C_APPEAL = "OP_ADMIN_C2C_APPEAL_TIP";

    /**
     * 交割单
     */
    public static final String FUTURES_ORDER = "OP_ADMIN_FUTURES_ORDER_TIP";


    /**
     * 区块链充值模块
     */
    public static final String RECHARGE_BLOCKCHAIN = "OP_ADMIN_RECHARGE_BLOCKCHAIN_TIP";

    /**
     * 三方充值模块
     */
    public static final String RECHARGE = "OP_ADMIN_RECHARGE_TIP";

    /**
     * 提现模块
     */
    public static final String WITHDRAW = "OP_ADMIN_WITHDRAW_TIP";

    /**
     * 认证模块
     */
    public static final String KYC = "OP_ADMIN_KYC_TIP";

    /**
     * 高级认证模块
     */
    public static final String KYC_HIGH_LEVEL = "OP_ADMIN_KYC_HIGH_LEVEL_TIP";

    /**
     * OTC订单模块
     */
    public static final String OTCORDER = "OP_ADMIN_OTC_ORDER_TIP";

    /**
     * OTC订单聊天模块
     */
    public static final String OTCORDER_ONLINECHAT = "OP_ADMIN_OTC_ORDER_ONLINECHAT_TIP";

    /**
     * 银行卡订单模块
     */
    public static final String BANK_CARD_ORDER = "OP_ADMIN_BANK_CARD_ORDER_TIP";

    /**
     * 用户资金密码申请模块
     */
    public static final String USER_SAFEWORD_APPLY = "OP_ADMIN_USER_SAFEWORD_APPLY_TIP";

    /**
     * 永续合约持仓单
     */
    public static final String CONTRACT_ORDER = "OP_ADMIN_CONTRACT_ORDER_TIP";

    /**
     * 用户客服模块
     */
    public static final String ONLINECHAT = "OP_ADMIN_ONLINECHAT";

    /**
     * 活动申请模块
     */
    public static final String ACTIVITY_USER_APPLY = "OP_ADMIN_ACTIVITY_USER_APPLY_TIP";
    public static final String ADMIN_URL = PropertiesUtil.getProperty("admin_url");

    /**
     * 请求action数据
     */
    public static Map<String, String> ACTION_MAP = new HashMap<String, String>();

    static {
        ACTION_MAP.put(RECHARGE_BLOCKCHAIN,  "/order-rechange");
        ACTION_MAP.put(RECHARGE,  "/order-rechange");
        ACTION_MAP.put(WITHDRAW,  "/order-withdraw");
        ACTION_MAP.put(KYC,  "/user-relation-basics");
        ACTION_MAP.put(KYC_HIGH_LEVEL, "/user-relation-senior");
        ACTION_MAP.put(OTCORDER, "/normal/adminOtcOrderAction!list.action");
        ACTION_MAP.put(BANK_CARD_ORDER,  "/c2c-bank_pay_order");
        ACTION_MAP.put(USER_SAFEWORD_APPLY,  "/user-relation-reset");
        ACTION_MAP.put(CONTRACT_ORDER,  "/normal/adminContractOrderAction!list.action");

        ACTION_MAP.put(USER_SAFEWORD_APPLY+"-0",  "/user-relation-reset");
        ACTION_MAP.put(USER_SAFEWORD_APPLY+"-1",  "/user-relation-reset");
        ACTION_MAP.put(USER_SAFEWORD_APPLY+"-2",  "/user-relation-reset");
        ACTION_MAP.put(USER_SAFEWORD_APPLY+"-3",  "/user-relation-reset");
//        ACTION_MAP.put(ONLINECHAT, "javascript:openNewChat();$('a[href^=\\'javascript:openNewChat();\\']').parent().find('.closed').click();");
    };

    /**
     * 消息格式数据
     */
    public static Map<String, String> MESSAGE_MAP = new HashMap<String, String>();

    static {
        MESSAGE_MAP.put(RECHARGE_BLOCKCHAIN, "您有{0}条新的区块链充值订单");
        MESSAGE_MAP.put(RECHARGE, "您有{0}条新的三方充值订单");
        MESSAGE_MAP.put(WITHDRAW, "您有{0}条新的提现订单");
        MESSAGE_MAP.put(KYC, "您有{0}条新的基础认证");
        MESSAGE_MAP.put(KYC_HIGH_LEVEL, "您有{0}条新的高级认证");
        MESSAGE_MAP.put(BANK_CARD_ORDER, "您有{0}条新的银行卡订单");
        MESSAGE_MAP.put(USER_SAFEWORD_APPLY, "您有{0}条新的用户资金密码修改申请");
        MESSAGE_MAP.put(USER_SAFEWORD_APPLY+"-0", "您有{0}条新的用户资金密码修改申请");
        MESSAGE_MAP.put(USER_SAFEWORD_APPLY+"-1", "您有{0}条新的用户取消谷歌绑定申请");
        MESSAGE_MAP.put(USER_SAFEWORD_APPLY+"-2", "您有{0}条新的用户取消手机绑定申请");
        MESSAGE_MAP.put(USER_SAFEWORD_APPLY+"-3", "您有{0}条新的用户取消邮箱绑定申请");
//        MESSAGE_MAP.put(ONLINECHAT, "您有{0}条新的聊天消息");
    };

    /**
     * 消息格式数据
     */
    public static Map<String, String> MESSAGE_TYPE = new HashMap<String, String>();

    static {
        MESSAGE_TYPE.put(RECHARGE_BLOCKCHAIN, "1");
        MESSAGE_TYPE.put(RECHARGE, "1");
        MESSAGE_TYPE.put(WITHDRAW, "2");
        MESSAGE_TYPE.put(KYC, "3");
        MESSAGE_TYPE.put(KYC_HIGH_LEVEL, "3");
        MESSAGE_TYPE.put(USER_SAFEWORD_APPLY, "4");
        MESSAGE_TYPE.put(BANK_CARD_ORDER, "4");
//        MESSAGE_MAP.put(BANK_CARD_ORDER, "您有{0}条新的银行卡订单");
//        MESSAGE_MAP.put(USER_SAFEWORD_APPLY, "您有{0}条新的用户资金密码修改申请");
//        MESSAGE_MAP.put(ONLINECHAT, "您有{0}条新的聊天消息");
    };

    /**
     * 前端标签名 数据
     */
    public static Map<String, String> DOM_MAP = new HashMap<String, String>();

    static {
        DOM_MAP.put(RECHARGE_BLOCKCHAIN, ".recharge_blockchain_order_untreated_cout");
        DOM_MAP.put(RECHARGE, ".recharge_order_untreated_cout");
        DOM_MAP.put(WITHDRAW, ".withdraw_order_untreated_cout");
        DOM_MAP.put(KYC, ".kyc_untreated_cout");
        DOM_MAP.put(KYC_HIGH_LEVEL, ".kyc_high_level_untreated_cout");
        DOM_MAP.put(BANK_CARD_ORDER, ".bank_card_order_untreated_cout");
        DOM_MAP.put(USER_SAFEWORD_APPLY, ".user_safeword_apply_untreated_cout");
        DOM_MAP.put(USER_SAFEWORD_APPLY+"-0", ".user_safeword_apply_untreated_cout-0");
        DOM_MAP.put(USER_SAFEWORD_APPLY+"-1", ".user_safeword_apply_untreated_cout-1");
        DOM_MAP.put(USER_SAFEWORD_APPLY+"-2", ".user_safeword_apply_untreated_cout-2");
        DOM_MAP.put(USER_SAFEWORD_APPLY+"-3", ".user_safeword_apply_untreated_cout-3");
    };

    /**
     * 必须指定用户名的模块
     */
    public static Map<String, String> MUST_USERNAME_MODEL = new HashMap<String, String>();

    static {
        MUST_USERNAME_MODEL.put(ONLINECHAT, ONLINECHAT);
    }

}
