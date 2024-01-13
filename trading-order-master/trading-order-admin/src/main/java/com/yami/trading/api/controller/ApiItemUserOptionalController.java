package com.yami.trading.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yami.trading.bean.item.domain.ItemUserOptional;
import com.yami.trading.bean.item.dto.ItemUserOptionalDTO;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.ThreadUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.item.ItemUserOptionalService;
import com.yami.trading.service.rate.ExchangeRateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户自选Controller
 * @author lucas
 * @version 2023-03-10
 */

@Api(tags ="h5用户自选")
@RestController
@Slf4j
@RequestMapping
public class ApiItemUserOptionalController {

	@Autowired
	private ItemUserOptionalService itemUserOptionalService;

	@Autowired
	private ExchangeRateService exchangeRateService;

	private final String action = "/api/itemUserOptional!";

	/**
	 * 返回自选币种的行情
	 */
	@ApiOperation("返回用户自选币种的列表")
	@GetMapping(action + "list.action")
	public Result<List<ItemUserOptionalDTO>> list(@RequestParam(required = false) String symbol) {
		String partyId = SecurityUtils.getCurrentUserId();
		List<ItemUserOptionalDTO> models = itemUserOptionalService.getItemUserOptionals(symbol, partyId);
		return Result.ok(models);
	}


	/**
	 * 加入自选
	 */
	@ApiOperation("加入自选")
	@GetMapping(action + "add.action")
	public Result<String> add(@RequestParam String symbol,@RequestParam String pid) {
		boolean lock = false;
		String loginPartyId =SecurityUtils.getUser().getUserId();
		try {
			if (ItemLock.add(loginPartyId)) {
				QueryWrapper queryWrapper = new QueryWrapper();
				queryWrapper.eq("party_id", loginPartyId);
				queryWrapper.eq("symbol", symbol);
				long count = itemUserOptionalService.count(queryWrapper);
				if(count > 0 ){
					throw new YamiShopBindException("当前已经加入过自选");
				}
				lock = true;
				ItemUserOptional entity = new ItemUserOptional();
				entity.setPartyId(loginPartyId);
				entity.setSymbol(symbol);
				entity.setPid(pid);
				itemUserOptionalService.save(entity);
			}else {
				throw new YamiShopBindException("请稍后再试");
			}
		} catch (Exception e) {
			log.error("保存自选失败", e);
			throw new YamiShopBindException("保存自选失败:"+ e.getMessage());
		}finally {
			if (lock) {
				ThreadUtils.sleep(50);
				ItemLock.remove(loginPartyId);
			}
		}
		return Result.succeed("保存成功");
	}

	/**
	 * 删除自选币种
	 */
	@ApiOperation("删除自选币种")
	@GetMapping(action + "delete.action")
	public Result<String> delete(@RequestParam String symbol,@RequestParam String pid) {
		String loginPartyId =SecurityUtils.getUser().getUserId();
		boolean lock = false;
		try {
			if (ItemLock.add(loginPartyId)) {
				lock = true;
				QueryWrapper<ItemUserOptional> queryWrapper = new QueryWrapper<>();
				queryWrapper.eq("party_id", loginPartyId);
				queryWrapper.eq("symbol", symbol);
				queryWrapper.eq("pid", pid);
				itemUserOptionalService.remove(queryWrapper);
			}else {
				throw new YamiShopBindException("请稍后再试");
			}
		} catch (Exception e) {
			log.error("删除失败", e);
			throw new YamiShopBindException("删除失败");

		} finally {
			if (lock) {
				ThreadUtils.sleep(50);
				ItemLock.remove(loginPartyId);
			}
		}
		return Result.ok("删除自选币种成功");
	}

	/**
	 * 查询是否已加入自选
	 */
	@ApiOperation("查询是否已加入自选")
	@GetMapping(action + "getItemOptionalStatus.action")
	public Result<Map<String, Object>> getItemOptionalStatus(@RequestParam String symbol) {
		String loginPartyId =SecurityUtils.getUser().getUserId();
		QueryWrapper<ItemUserOptional> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("party_id", loginPartyId);
		queryWrapper.eq("symbol", symbol);
		long count = itemUserOptionalService.count(queryWrapper);
		Map<String, Object> data = new HashMap<String, Object>();

		if(count <= 0){
			data.put("status", "0");
		}else{
			data.put("status", "1");
		}
		return Result.ok(data);
	}
}
