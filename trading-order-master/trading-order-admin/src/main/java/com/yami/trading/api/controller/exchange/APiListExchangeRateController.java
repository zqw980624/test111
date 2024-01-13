package com.yami.trading.api.controller.exchange;

import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.web.ResultObject;
import com.yami.trading.service.rate.ExchangeRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@CrossOrigin
public class APiListExchangeRateController {

	private static final Logger logger = LoggerFactory.getLogger(APiListExchangeRateController.class);

	@Autowired
	private ExchangeRateService exchangeRateService;

	@RequestMapping("api/exchangerate!list.action")
	public ResultObject list() throws IOException {

		ResultObject resultObject = new ResultObject();

		try {
			String out_or_in = Constants.OUT_OR_IN_DEFAULT;
			resultObject.setData(this.exchangeRateService.findBy(out_or_in));
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", e);
		}
		return resultObject;
	}
}
