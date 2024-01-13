package com.yami.trading.service.syspara;

import com.yami.trading.bean.rate.domain.ExchangeRate;
import com.yami.trading.service.rate.ExchangeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocalSysparaServiceImpl implements LocalSysparaService {

	@Autowired
	private ExchangeRateService exchangeRateService;
	@Autowired

	private SysparaService sysparaService;

	@Override
	public Map<String, Object> find(String code) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (code.indexOf(",") == -1) {
			/**
			 * 单个code
			 */
			map.put(code, single(code));
		} else {
			/**
			 * 多个code，用逗号分隔
			 */
			String[] codes = code.split(",");
			for (int i = 0; i < codes.length; i++) {
				String split = codes[i];
				map.put(split, single(split));
			}
		}
		return map;
	}

	public Object single(String code) {
		Object object = null;
		if ("exchange_rate_out".equals(code)) {

			/**
			 * 兑出货币和汇率
			 */
			List<ExchangeRate> result = exchangeRateService.findBy("out");
			// 手续费(USDT)

			object = result;

		} else if ("exchange_rate_in".equals(code)) {
			/**
			 * 兑入货币和汇率
			 */

			List<ExchangeRate> list = exchangeRateService.findBy("in");

			object = list;

		} else if ("withdraw_fee".equals(code)) {
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("type", sysparaService.find("withdraw_fee_type").getSvalue());
			result.put("fee", sysparaService.find("withdraw_fee").getSvalue());
			object = result;
		} else if ("index_top_symbols".equals(code)) {
			String result = sysparaService.find("index_top_symbols").getSvalue();
			object = result;
		} else if ("customer_service_url".equals(code)) {
			String result = sysparaService.find("customer_service_url").getSvalue();
			object = result;
		} else if ("can_recharge".equals(code)) {
			String result = sysparaService.find("can_recharge").getSvalue();
			object = result;
		} else if ("miner_buy_symbol".equals(code)) {
			String result = sysparaService.find("miner_buy_symbol").getSvalue();
			object = result;
		} else if ("miner_bonus_parameters".equals(code)) {
			String result = sysparaService.find("miner_bonus_parameters").getSvalue();
			object = result;
		} else if ("test_user_money".equals(code)) {
			String result = sysparaService.find("test_user_money").getSvalue();
			object = result;
		} else if ("index_new_symbols".equals(code)) {
			String result = sysparaService.find("index_new_symbols").getSvalue();
			object = result;
		}

		return object;

	}

}