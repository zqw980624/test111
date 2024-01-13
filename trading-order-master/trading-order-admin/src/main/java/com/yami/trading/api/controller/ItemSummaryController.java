package com.yami.trading.api.controller;
import com.alibaba.fastjson2.JSONObject;
import com.yami.trading.api.util.HttpClientRequest;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.item.domain.ItemSummary;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.lang.LangUtils;
import com.yami.trading.service.item.ItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.yami.trading.bean.item.mapstruct.ItemSummaryWrapper;
import com.yami.trading.service.item.ItemSummaryService;

import java.util.ArrayList;

/**
 * 简况Controller
 * @author lucas
 * @version 2023-05-01
 */

@Api(tags ="h5简况")
@RestController
@RequestMapping(value = "api/item/itemSummary")
public class ItemSummaryController {

	@Autowired
	private ItemSummaryService itemSummaryService;

	@Autowired
	private ItemSummaryWrapper itemSummaryWrapper;
	@Autowired
	private ItemService itemService;
	/*
	 * 根据Id获取简况数据
	 */
	@ApiOperation(value = "根据symbol获取简况数据")
	@GetMapping("get")
	public Result<ItemSummary> queryById(@RequestParam String symbol) {
		Item bySymbol = itemService.findBySymbols(symbol);
		if(bySymbol == null){
			return Result.failed ("股票对不存在");
		}
		String results = HttpClientRequest.doGet("http://api-in.js-stock.top/companies?pid=" +bySymbol.getPid() + "&size=10&page=1&key=zHPU8uWYMY7eWx78kbC0");
		com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSONObject.parseObject(results);
		//取出data里的数组
		ArrayList diff = (ArrayList) json.get("data");
		ItemSummary item = new ItemSummary();
		for (Object o : diff) {
			//取数据存入redisDB1
			com.alibaba.fastjson2.JSONObject data = (JSONObject) o;
			String company_name = data.getString("company_name");
			String description = data.getString("description");
			item.setOrgProfile(description);
			item.setOrgName(company_name);
			if(LangUtils.isEnItem()){
				bySymbol.transName();
				return Result.ok (item);
			}
		}
		return Result.ok (item);
	}
}
