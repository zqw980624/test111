package com.yami.trading.common.constants;

public class RedisKeys {
	/**
	 * item杠杆倍数
	 */
	public final static String ITEM_LEVER_ID = "ITEM_LEVER_ID_";

	public final static String ITEM_MAP = "ITEM_MAP_";

	public final static String ITEM_SYMBOL = "ITEM_SYMBOL_";
	public final static String ITEM_SYMBOLDATA = "ITEM_SYMBOLDATA_";

	/**
	 * partyId查询party表
	 */
	public final static String PARTY_PARTYID = "PARTY_PARRTID_";
	/**
	 * party，在线用户，partyId做key
	 */
	public final static String PARTY_ONLINEUSER_PARTYID = "PARTY_ONLINEUSER_PARTYID_";
	/**
	 * party，获取到所有在线用户
	 */
	public final static String PARTY_ONLINEUSER = "PARTY_ONLINEUSER_";
	/**
	 * code查询系统参数Syspara表
	 */
	public final static String SYSPARA_CODE = "SYSPARA_CODE_";
	/**
	 * 查询系统参数Syspara表的Map
	 */
	public final static String SYSPARA_MAP = "SYSPARA_MAP_";

	/**
	 * 区块链充值地址
	 */
	public final static String CHANNEL_BLOCKCHAIN_ID = "CHANNEL_BLOCKCHAIN_ID_";
	/**
	 * 区块链充值地址 所有
	 */
	public final static String CHANNEL_BLOCKCHAIN_MAP = "CHANNEL_BLOCKCHAIN_MAP_";
	/**
	 * 区块链充值订单
	 */
	public final static String RECHARGE_BLOCKCHAIN_ORDERNO = "RECHARGE_BLOCKCHAIN_ORDERNO_";
	/**
	 * 提现订单
	 */
	public final static String WITHDRAW_ORDERNO = "WITHDRAW_ORDERNO_";

	/**
	 * 钱包 partyid 做key
	 */
	public final static String WALLET_PARTY_ID = "WALLET_PARTY_ID_";
	/**
	 * 钱包，异步更新
	 */
	public final static String WALLET = "WALLET_";
	/**
	 * 拓展钱包 partyId+walletType做key
	 */
	public final static String WALLET_EXTEND_PARTY_ID_WALLETTYPE = "WALLET_EXTEND_PARTY_ID_WALLETTYPE_";
	/**
	 * 拓展钱包 查询partyId的map
	 */
	public final static String WALLET_EXTEND_PARTY_ID = "WALLET_EXTEND_PARTY_ID_";
	/**
	 * 拓展钱包，异步更新
	 */
	public final static String WALLET_EXTEND = "WALLET_EXTEND_";

	/**
	 * 系统用户，username做key
	 */
	public final static String SECUSER_USERNAME = "SECUSER_USERNAME_";

//	/**
//	 * CMS，查询语言的map
//	 */
//	public final static String CMS_LANGUAGE = "CMS_LANGUAGE_";
//	/**
//	 * CMS,language+contentCode 做key
//	 */
//	public final static String CMS_LANGUAGE_CONTENTCODE = "CMS_LANGUAGE_CONTENTCODE_";
	
	/**
	 * C2C支付方式模板，id做key
	 */
	public final static String C2C_PAYMENT_METHOD_CONFIG_ID = "C2C_PAYMENT_METHOD_CONFIG_ID_";
	
	/**
	 * C2C支付方式模板，Map<支付方式模板id, 支付方式模板类型>
	 */
	public final static String C2C_PAYMENT_METHOD_CONFIG_ID_TYPE = "C2C_PAYMENT_METHOD_CONFIG_ID_TYPE_";
	
	/**
	 * C2C支付方式，id做key
	 */
	public final static String C2C_PAYMENT_METHOD_ID = "C2C_PAYMENT_METHOD_ID_";
	
	/**
	 * C2C支付方式，party_id做key
	 */
	public final static String C2C_PAYMENT_METHOD_PARTY_ID = "C2C_PAYMENT_METHOD_PARTY_ID_";
	
	/**
	 * C2C支付方式，Map<支付方式id, 支付方式类型>
	 */
	public final static String C2C_PAYMENT_METHOD_ID_TYPE = "C2C_PAYMENT_METHOD_ID_TYPE_";
	
	/**
	 * C2C承兑商，id做key
	 */
	public final static String C2C_USER_ID = "C2C_USER_ID_";
	
	/**
	 * C2C承兑商，party_id做key
	 */
	public final static String C2C_USER_PARTY_ID = "C2C_USER_PARTY_ID_";
	
	/**
	 * C2C承兑商参数基础设置，c2c_user_party_id做key
	 */
	public final static String C2C_USER_PARAM_BASE_SET_PARTY_ID = "C2C_USER_PARAM_BASE_SET_PARTY_ID_";
	
	/**
	 * C2C广告，id做key
	 */
	public final static String C2C_ADVERT_ID = "C2C_ADVERT_ID_";
	
	/**
	 * C2C广告，承兑商id做key
	 */
	public final static String C2C_ADVERT_C2C_USER_ID = "C2C_ADVERT_C2C_USER_ID_";
	
	/**
	 * C2C广告，广告id做key，上架币种单价做value
	 */
	public final static String C2C_ADVERT_CURRENCY_SYMBOL_DIRECTION = "C2C_ADVERT_CURRENCY_SYMBOL_DIRECTION_";
	
	/**
	 * C2C订单，订单号做key
	 */
	public final static String C2C_ORDER_NO = "C2C_ORDER_NO_";
	
	/**
	 * C2C订单，订单号做key，订单超时时间戳做value
	 */
	public final static String C2C_ORDER_NO_EXPIRE_TIME = "C2C_ORDER_NO_EXPIRE_TIME_";
	
	/**
	 * C2C订单，party_id做key，未结束订单数量做value
	 */
	public final static String C2C_NOFINISH_ORDER_COUNT = "C2C_NOFINISH_ORDER_COUNT_";
	
	/**
	 * C2C申诉，订单号做key
	 */
	public final static String C2C_APPEAL_ORDER_NO = "C2C_APPEAL_ORDER_NO_";
	
	/**
	 * C2C翻译，id做key
	 */
	public final static String C2C_TRANSLATE_ID = "C2C_TRANSLATE_ID_";
	
	/**
	 * C2C翻译，内容Unicode+语言 做key
	 */
	public final static String C2C_TRANSLATE_CONTENT_LANGUAGE = "C2C_TRANSLATE_CONTENT_LANGUAGE_";
	
	/**
	 * C2C订单每日取消次数，party_id做key，次数做value
	 */
	public final static String C2C_ORDER_CANCEL_DAY_TIMES = "C2C_ORDER_CANCEL_DAY_TIMES_";

	/**
	 * 新闻，id做key
	 */
	public final static String NEWS_ID = "NEWS_ID_";

	/**
	 * 新闻，查询语言的map
	 */
	public final static String NEWS_LANGUAGE = "NEWS_LANGUAGE_";

	/**
	 * 充提日志，orderno做key
	 */
	public final static String WALLET_LOG_ORDERNO = "WALLET_LOG_ORDERNO_";
	/**
	 * 充提日志，查询分类map
	 */
	public final static String WALLET_LOG_CATEGORY = "WALLET_LOG_CATEGORY_";

	/**
	 * 汇率，CURRENCY做key
	 */
	public final static String EXCHANGE_RATE_CURRENCY = "EXCHANGE_RATE_CURRENCY_";
	/**
	 * 汇率，ID做key
	 */
	public final static String EXCHANGE_RATE_ID = "EXCHANGE_RATE_ID_";
	/**
	 * 汇率，查询out_or_in 的map
	 */
	public final static String EXCHANGE_RATE_OUTORIN = "EXCHANGE_RATE_OUTORIN_";
	/**
	 * 用户汇率配置，partyId做key
	 */
	public final static String USER_RATE_CONFIG_PARTY_ID = "USER_RATE_CONFIG_PARTY_ID_";

	/**
	 * 用户认证，partyId做key
	 */
	public final static String KYC_PARTY_ID = "KYC_PARTY_ID_";
	/**
	 * 高级认证，partyId做key
	 */
	public final static String KYC_HIGHLEVEL_PARTY_ID = "KYC_HIGHLEVEL_PARTY_ID_";
	/**
	 * 支付方式，partyId做key
	 */
	public final static String PAYMENT_METHOD_ID = "PAYMENT_METHOD_ID_";
	/**
	 * 支付方式，查询partyId的map
	 */
	public final static String PAYMENT_METHOD_PARTY_ID = "PAYMENT_METHOD_PARTY_ID_";

	/**
	 * 获取用户partyId，根据token
	 */
	public final static String TOKEN = "TOKEN_";
	/**
	 * 获取用户token，party做key
	 */
	public final static String TOKEN_PARTY_ID = "TOKEN_PARTY_ID_";
	/**
	 * 理财产品，id做key
	 */
	public final static String FINANCE_ID = "FINANCE_ID_";
	/**
	 * 理财产品，MAP
	 */
	public final static String FINANCE_MAP = "FINANCE_MAP_";
	/**
	 * 理财产品，map并且state=1
	 */
	public final static String FINANCE_MAP_STATE_1 = "FINANCE_MAP_STATE_1_";
	/**
	 * 理财产品订单，id做key
	 */
	public final static String FINANCE_ORDER_ID = "FINANCE_ORDER_ID_";

	/**
	 * 理财产品订单，map并且state=1
	 */
	public final static String FINANCE_ORDER_MAP_STATE_1 = "FINANCE_ORDER_MAP_STATE_1_";

	/**
	 * 币币交易 现货，order_no为key
	 */
	public final static String EXCHANGE_ORDER_NO = "EXCHANGE_ORDER_NO_";
	/**
	 * 币币交易 现货，所有已提交的订单
	 */
	public final static String EXCHANGE_ORDER_STATE_SUBMITTED_MAP_ORDER_NO = "EXCHANGE_ORDER_STATE_SUBMITTED_ORDER_NO_";
	/**
	 * 币币资产交易买入价格，PARTYID 和wallettype为key
	 */
	public final static String WALLETEXTENDCOSTUSDT_PARTYID_WALLETTYPE = "WALLETEXTENDCOSTUSDT_PARTYID_WALLETTYPE_";


	public final static String SYMBOL_DEPTH = "SYMBOL_DEPTH_";

	public final static String SYMBOL_AMOUNT_VOLUME = "SYMBOL_AMOUNT_VOLUME_";

}
