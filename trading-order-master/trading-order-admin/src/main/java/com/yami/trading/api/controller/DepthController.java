package com.yami.trading.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yami.trading.bean.data.domain.Depth;
import com.yami.trading.bean.data.domain.DepthEntry;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.exception.BusinessException;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.common.web.ResultObject;
import com.yami.trading.service.data.DataService;

import com.yami.trading.service.item.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 市场深度数据 20档深度，根据页面取5档或20档
 *
 */
@RestController
@CrossOrigin
public class DepthController {

	@Autowired
	@Qualifier("dataService")
	private DataService dataService;
	@Autowired
	private ItemService itemService;

	@RequestMapping("api/hobi!getDepth.action")
	public ResultObject getDepth(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();

		try {
			String symbol = request.getParameter("symbol");
			if (StringUtils.isNullOrEmpty(symbol)) {
				resultObject.setCode("400");
				resultObject.setMsg("[symbol]参数为空");
				return resultObject;
			}

			// 数据处理
			Depth data = this.dataService.depth(symbol);

			resultObject.setData(revise(data));
			return resultObject;
		} catch (BusinessException e) {
			resultObject.setCode("402");
			resultObject.setMsg(e.getMessage());
			return resultObject;
		} catch (Throwable e) {
			resultObject.setCode("500");
			resultObject.setMsg("服务器错误(500)");

			return resultObject;
		}

	}

	private Map<String, Object> revise(Depth data) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("symbol", data.getSymbol());
		map.put("ts", data.getTs());
		Item item = this.itemService.findBySymbol(data.getSymbol());
		List<Map<String, Object>> asks_list = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < data.getAsks().size(); i++) {
			DepthEntry depthEntry = data.getAsks().get(i);
			Map<String, Object> asks_map = new HashMap<String, Object>();

			if (item.getDecimals() == null || item.getDecimals() < 0) {
				asks_map.put("price", depthEntry.getPrice());
				asks_map.put("amount", depthEntry.getAmount());
			} else {
				String format = "";
				if (item.getDecimals() == 0) {
					format = "#";
				} else {
					format = "#.";
					for (int j = 0; j < item.getDecimals(); j++) {
						format = format + "#";
					}
				}

				DecimalFormat df = new DecimalFormat(format);
				df.setRoundingMode(RoundingMode.FLOOR);// 向下取整

				asks_map.put("price", df.format(depthEntry.getPrice()));
				asks_map.put("amount", df.format(depthEntry.getAmount()));

			}
			asks_list.add(asks_map);

		}
		map.put("asks", asks_list);
		List<Map<String, Object>> bids_list = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < data.getBids().size(); i++) {
			DepthEntry depthEntry = data.getBids().get(i);
			Map<String, Object> bids_map = new HashMap<String, Object>();
			if (item.getDecimals() == null || item.getDecimals() < 0) {
				bids_map.put("price", depthEntry.getPrice());
				bids_map.put("amount", depthEntry.getAmount());
			} else {
				String format = "";
				if (item.getDecimals() == 0) {
					format = "#";
				} else {
					format = "#.";
					for (int j = 0; j < item.getDecimals(); j++) {
						format = format + "#";
					}
				}

				DecimalFormat df = new DecimalFormat(format);

				bids_map.put("price", df.format(depthEntry.getPrice()));
				bids_map.put("amount", df.format(depthEntry.getAmount()));

			}
			bids_list.add(bids_map);

		}

		map.put("bids", bids_list);

		return map;
	}
	
}
