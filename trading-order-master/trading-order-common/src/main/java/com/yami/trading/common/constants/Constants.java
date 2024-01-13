package com.yami.trading.common.constants;



import com.yami.trading.common.util.PropertiesUtil;

import java.util.*;

public class Constants {

//
	public static final String WEB_URL = PropertiesUtil.getProperty("web_url");

	public static final String IMAGES_DIR = PropertiesUtil.getProperty("images.dir");

	/**
	 * c2c卖币
	 */
	public static final String MONEYLOG_CONTENT_C2C_SELL = "c2c_sell";
	/**
	 * otc买币
	 */
	public static final String MONEYLOG_CONTENT_OTC_BUY = "otc_buy";
	/**
	 * c2c交易
	 */
	public static final String MONEYLOG_CATEGORY_C2C = "c2c";
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
	 * 当前分了多少个表
	 */
	public static final int TABLE_PARTITIONS = 10;
	/*
	 * 角色
	 */
	public static final String SECURITY_ROLE_ROOT = "ROOT";
	public static final String SECURITY_ROLE_ADMIN = "ADMIN";
	public static final String SECURITY_ROLE_FINANCE = "FINANCE";
	public static final String SECURITY_ROLE_CUSTOMER = "CUSTOMER";
	public static final String SECURITY_ROLE_MAINTAINER = "MAINTAINER";
	public static final String SECURITY_ROLE_AGENT = "AGENT";

	public static final String SECURITY_ROLE_MEMBER = "MEMBER";
	public static final String SECURITY_ROLE_GUEST = "GUEST";
	public static final String SECURITY_ROLE_TEST = "TEST";
	public static final String SECURITY_ROLE_OTCUSER = "OTCUSER";
	public static final String SECURITY_ROLE_INSIDER = "INSIDER";
	public static final String SECURITY_ROLE_AGENTLOW = "AGENTLOW";

	public static Map<String, String> ROLE_MAP = new HashMap<String, String>();

	static {
		ROLE_MAP.put(SECURITY_ROLE_ROOT, "超级管理员");
		ROLE_MAP.put(SECURITY_ROLE_ADMIN, "管理员");
		ROLE_MAP.put(SECURITY_ROLE_FINANCE, "对账专员");
		ROLE_MAP.put(SECURITY_ROLE_CUSTOMER, "客服");
		ROLE_MAP.put(SECURITY_ROLE_MAINTAINER, "运维");
		ROLE_MAP.put(SECURITY_ROLE_AGENT, "代理商");

		ROLE_MAP.put(SECURITY_ROLE_MEMBER, "正式用户");
		ROLE_MAP.put(SECURITY_ROLE_GUEST, "演示用户");
		ROLE_MAP.put(SECURITY_ROLE_TEST, "试用用户");
		ROLE_MAP.put(SECURITY_ROLE_OTCUSER, "承兑商");
		ROLE_MAP.put(SECURITY_ROLE_INSIDER, "内部专员");
		ROLE_MAP.put(SECURITY_ROLE_AGENTLOW, "代理商");
	};

	/**
	 * 理财收益
	 */
	public static final String MONEYLOG_CONTENT_FINANCE_PROFIT = "finance_profit";

	/**
	 * 理财产品
	 */
	public static final String MONEYLOG_FINANCE = "finance";
//	public static final String WEB_URL = PropertiesUtil.getProperty("web_url");
//
//	public static final String IMAGES_DIR = PropertiesUtil.getProperty("images.dir");

	/**
	 * 奖励
	 */
	public static final String MONEYLOG_CONTENT_REWARD = "reward";
	/**
	 * 交易账户(USDT)
	 */
	public static final String WALLET_USDT = "usdt";

	/**
	 * 交易账户(USDT)
	 */
	public static final String WALLET = "USDT";

	/**
	 * 交易账户(USDT)
	 */
	//public static final String WALLET = "USDT";
	/**
	 * 充值
	 */
	public static final String MONEYLOG_CATEGORY_RECHARGE = "recharge";
	/**
	 * 提现
	 */
	public static final String MONEYLOG_CATEGORY_WITHDRAW = "withdraw";

	public static final String MONEYLOG_CATEGORY_ORDER = "order";
	/**
	 * 永续合约建仓
	 */
	public static final String MONEYLOG_CONTENT_CONTRACT_OPEN = "contract_open";
	/**
	 * 交割合约建仓
	 */
	public static final String DELIVERY_MONEYLOG_CONTENT_CONTRACT_OPEN = "delivery_contract_open";
	/**
	 * 交割合约平仓
	 */
	public static final String DELIVERY_MONEYLOG_CONTENT_CONTRACT_CLOSE = "delivery_contract_close";
	/**
	 * 合约交易
	 */
	public static final String MONEYLOG_CATEGORY_CONTRACT = "contract";
	/**
	 * 法币交易
	 */
	public static final String MONEYLOG_CATEGORY_COIN = "coin";
	/**
	 * 银行卡交易
	 */
	public static final String MONEYLOG_CATEGORY_BANK_CARD = "bank_card";
	/**
	 * 银行卡充值
	 */
	public static final String MONEYLOG_CATEGORY_BANK_CARD_RECHARGE = "bank_card_recharge";
	/**
	 * 银行卡提现
	 */
	public static final String MONEYLOG_CATEGORY_BANK_CARD_WITHDRAW = "bank_card_withdraw";
	/**
	 * 系统奖励
	 */
	public static final String MONEYLOG_CATEGORY_REWARD = "reward";

	public static Map<String, String> MONEYLOG_CATEGORY = new HashMap<String, String>();

	static {
		MONEYLOG_CATEGORY.put(MONEYLOG_CATEGORY_CONTRACT, "合约交易");
		MONEYLOG_CATEGORY.put(MONEYLOG_CATEGORY_COIN, "法币交易");
	};

	/**
	 * 股票交易
	 */
	public static final String MONEYLOG_CATEGORY_EXCHANGE = "exchange";


	/**
	 * 股票卖出
	 */
	public static final String MONEYLOG_CONTENT_EXCHANGE_CLOSE = "exchange_close";
	/**
	 * 股票取消
	 */
	public static final String MONEYLOG_CONTENT_EXCHANGE_CANCEL = "exchange_cancel";
	/**
	 * 股票买入
	 */
	public static final String MONEYLOG_CONTENT_EXCHANGE_OPEN = "exchange_open";

	/**
	 * C2C
	 */
	public static final String LOG_CATEGORY_C2C = "c2c";
	/**
	 * 充币
	 */
	public static final String MONEYLOG_CONTENT_RECHARGE = "recharge";
	/**
	 * 提币
	 */
	public static final String MONEYLOG_CONTENT_WITHDRAW = "withdraw";
	/**
	 * 永续合约平仓
	 */
	public static final String MONEYLOG_CONTENT_CONTRACT_CLOSE = "contract_close";
	/**
	 * 永续合约建仓
	 */
	/**
	 * 永续合约撤单
	 */
	public static final String MONEYLOG_CONTENT_CONTRACT_CONCEL = "contract_cancel";
	/**
	 * 手续费
	 */
	public static final String MONEYLOG_CONTENT_FEE = "fee";
	/**
	 * 银行卡提现
	 */
	public static final String MONEYLOG_CONTENT_BANK_CARD_WITHDRAW = "bank_card_withdraw";
	/**
	 * 银行卡充值
	 */
	public static final String MONEYLOG_CONTENT_BANK_CARD_RECHARGE = "bank_card_recharge";
	/**
	 * 银行卡订单取消
	 */
	public static final String MONEYLOG_CONTENT_BANK_CARD_ORDER_CANCEL = "bank_card_order_cancel";

	public static Map<String, String> MONEYLOG_CONTENT = new HashMap<String, String>();

	static {
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_RECHARGE, "充币");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_WITHDRAW, "提币");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_CONTRACT_CLOSE, "永续合约平仓");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_CONTRACT_OPEN, "永续合约建仓");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_CONTRACT_CONCEL, "永续合约撤单");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_FEE, "手续费");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_BANK_CARD_WITHDRAW, "银行卡提现");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_BANK_CARD_RECHARGE, "银行卡充值");
		MONEYLOG_CONTENT.put(MONEYLOG_CONTENT_BANK_CARD_ORDER_CANCEL, "银行卡订单取消");
	}

	/**
	 * 操作
	 */
	public static final String LOG_CATEGORY_OPERATION = "operation";

	/**
	 * 安全
	 */
	public static final String LOG_CATEGORY_SECURITY = "security";

	/**
	 * 安全
	 */
	public static final String LOG_CATEGORY_SECURITY_CODE = "security_code";

	/**
	 * 银行卡充值提现
	 */
	public static final String LOG_CATEGORY_BANK_CARD_RW = "bank_card_rw";

	public static Map<String, String> LOG_CATEGORY = new HashMap<String, String>();

	static {
		LOG_CATEGORY.put(LOG_CATEGORY_OPERATION, "用户操作");
		LOG_CATEGORY.put(LOG_CATEGORY_SECURITY, "安全事件");
		LOG_CATEGORY.put(LOG_CATEGORY_BANK_CARD_RW, "银行卡充值提现");
	};

	public static Map<String, String> BLOCKCHAIN_COINS = new HashMap<String, String>();

	static {
		BLOCKCHAIN_COINS.put("USDT", "USDT");
	};
	public static Map<String, String> BLOCKCHAIN_COINS_NAME = new HashMap<String, String>();

	static {
		/**
		 * usdt、HT
		 */
		BLOCKCHAIN_COINS_NAME.put("ERC20", "ERC20");
		BLOCKCHAIN_COINS_NAME.put("TRC20", "TRC20");
		/**
		 * usdt
		 */
		BLOCKCHAIN_COINS_NAME.put("OMNI", "OMNI");
	};

	/**
	 * 语言
	 */
	public static Map<String, String> LANGUAGE = new HashMap<String, String>();

	static {
		LANGUAGE.put("en", "英文");
		LANGUAGE.put("zh-CN", "简体中文");
		LANGUAGE.put("CN", "繁体中文");
		LANGUAGE.put("Japanese", "日文");
		LANGUAGE.put("Korean", "韩文");
		LANGUAGE.put("ru", "俄文");
		LANGUAGE.put("pt", "葡萄牙语");
		LANGUAGE.put("es", "西班牙语");
		LANGUAGE.put("th", "泰语");
		LANGUAGE.put("fr", "法语");
	    LANGUAGE.put("vi", "越南语");
	    LANGUAGE.put("ar", "阿拉伯语");
	    LANGUAGE.put("my", "缅甸语");
	    LANGUAGE.put("de", "德语");
	}

	/**
	 * cms模块
	 */
	public static Map<String, String> CMS_MODEL = new HashMap<String, String>();

	static {
		CMS_MODEL.put("system", "系统");
		CMS_MODEL.put("info", "说明");
		CMS_MODEL.put("help_center", "帮助中心");
		CMS_MODEL.put("knowledge", "百科");
	}

	/**
	 * banner模块
	 */
	public static Map<String, String> BANNER_MODEL = new HashMap<String, String>();

	static {
		BANNER_MODEL.put("top", "轮播");
		BANNER_MODEL.put("other", "其他");
		BANNER_MODEL.put("poster", "海报");
	}

	public static String LEVEL_ERROR = "error";
	public static String LEVEL_WARN = "warn";
	public static String LEVEL_INFO = "info";
	// 系统日志类型
	public static Map<String, String> SYS_LOG_LEVEL = new HashMap<String, String>();
	static {
		SYS_LOG_LEVEL.put(LEVEL_ERROR, "错误");
		SYS_LOG_LEVEL.put(LEVEL_WARN, "警告");
		SYS_LOG_LEVEL.put(LEVEL_INFO, "信息");
	};

	public static String NOTIFY_TYPE_EMAIL = "email";
	public static String NOTIFY_TYPE_PHONE = "phone";
	public static Map<String, String> NOTIFY_TYPE = new HashMap<String, String>();
	static {
		NOTIFY_TYPE.put(NOTIFY_TYPE_EMAIL, "邮箱");
		NOTIFY_TYPE.put(NOTIFY_TYPE_PHONE, "手机");
	};

	public static String NOTIFY_STATUS_OPEN = "1";
	public static String NOTIFY_STATUS_CLOSE = "0";
	public static Map<String, String> NOTIFY_STATUS = new HashMap<String, String>();
	static {
		NOTIFY_STATUS.put(NOTIFY_STATUS_OPEN, "启用");
		NOTIFY_STATUS.put(NOTIFY_STATUS_CLOSE, "停止");
	};
	/**
	 * 默认汇率转化为in
	 */
	public static String OUT_OR_IN_DEFAULT = "in";

	public final static String PROFIT_LOSS_TYPE_PROFIT = "profit";
	public final static String PROFIT_LOSS_TYPE_LOSS = "loss";
	public final static String PROFIT_LOSS_TYPE_BUY_PROFIT = "buy_profit";
	public final static String PROFIT_LOSS_TYPE_SELL_PROFIT = "sell_profit";
	/**
	 * 买多盈利并且买空亏损
	 */
	public final static String PROFIT_LOSS_TYPE_BUY_PROFIT_SELL_LOSS = "buy_profit_sell_loss";
	/**
	 * 买空盈利并且买多亏损
	 */
	public final static String PROFIT_LOSS_TYPE_SELL_PROFIT_BUY_LOSS = "sell_profit_buy_loss";

	public static Map<String, String> PROFIT_LOSS_TYPE = new LinkedHashMap<String, String>();
	static {
		PROFIT_LOSS_TYPE.put(PROFIT_LOSS_TYPE_PROFIT, "盈利");
		PROFIT_LOSS_TYPE.put(PROFIT_LOSS_TYPE_LOSS, "亏损");
		PROFIT_LOSS_TYPE.put(PROFIT_LOSS_TYPE_BUY_PROFIT, "买多盈利");
		PROFIT_LOSS_TYPE.put(PROFIT_LOSS_TYPE_SELL_PROFIT, "买空盈利");
		PROFIT_LOSS_TYPE.put(PROFIT_LOSS_TYPE_BUY_PROFIT_SELL_LOSS, "买多盈利并且买空亏损");
		PROFIT_LOSS_TYPE.put(PROFIT_LOSS_TYPE_SELL_PROFIT_BUY_LOSS, "买空盈利并且买多亏损");
	};

	/**
	 * 合约
	 */
	public final static String OPTIONAL_MODULE_CONTRACT = "contract";
	public static List<String> OPTIONAL_MODULE = new ArrayList<String>();
	static {
		OPTIONAL_MODULE.add(OPTIONAL_MODULE_CONTRACT);
	};

}
