package com.yami.trading.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.yami.trading.bean.item.dto.ItemLeverageDTO;
import com.yami.trading.bean.item.mapstruct.TItemLeverageWrapper;
import com.yami.trading.bean.item.query.TItemLeverageQuery;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.query.QueryWrapperGenerator;
import com.yami.trading.service.item.ItemLeverageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 产品杠杠倍数Controller
 * @author lucas
 * @version 2023-03-10
 */

@Api(tags ="h5/pc产品杠杠倍数")
@RestController
@RequestMapping(value = "api/item/tItemLeverage")
public class ApiTItemLeverageController {

	@Autowired
	private ItemLeverageService itemLeverageService;

	@Autowired
	private TItemLeverageWrapper tItemLeverageWrapper;

	/**
	 * 产品杠杠倍数列表数据
	 */
	@ApiOperation(value = "列表查询")
	@GetMapping("list")
	public Result<IPage<ItemLeverageDTO>> list(TItemLeverageQuery tItemLeverageQuery, Page<ItemLeverageDTO> page) throws Exception {
		QueryWrapper queryWrapper = QueryWrapperGenerator.buildQueryCondition (tItemLeverageQuery, TItemLeverageQuery.class);
		IPage<ItemLeverageDTO> result = itemLeverageService.findPage (page, queryWrapper);
		return Result.succeed (result);
	}


	/**
	 * 根据Id获取产品杠杠倍数数据
	 */
	@ApiOperation(value = "根据Id获取产品杠杠倍数数据")
	@GetMapping("queryById")
	public Result<ItemLeverageDTO> queryById(String id) {
		return Result.succeed (itemLeverageService.findById ( id ) );
	}

}
