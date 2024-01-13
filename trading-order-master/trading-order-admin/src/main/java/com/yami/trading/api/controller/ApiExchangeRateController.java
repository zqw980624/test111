package com.yami.trading.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.yami.trading.bean.rate.domain.ExchangeRate;
import com.yami.trading.bean.rate.dto.ExchangeRateDTO;
import com.yami.trading.bean.rate.mapstruct.ExchangeRateWrapper;
import com.yami.trading.bean.rate.query.ExchangeRateQuery;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.query.QueryWrapperGenerator;
import com.yami.trading.service.rate.ExchangeRateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;




/**
 * 汇率管理Controller
 * @author lucas
 * @version 2023-03-28
 */

@Api(tags ="汇率管理")
@RestController
@RequestMapping(value = "api/rate/exchangeRate")
public class ApiExchangeRateController {

	@Autowired
	private ExchangeRateService exchangeRateService;

	@Autowired
	private ExchangeRateWrapper exchangeRateWrapper;

	/**
	 * 汇率管理列表数据
	 */
	@ApiOperation(value = "查询汇率管理列表数据")
	@GetMapping("list")
	public Result<IPage<ExchangeRate>> list(ExchangeRateQuery exchangeRateQuery, Page<ExchangeRate> page) throws Exception {
		QueryWrapper queryWrapper = QueryWrapperGenerator.buildQueryCondition (exchangeRateQuery, ExchangeRateQuery.class);
		IPage<ExchangeRate> result = exchangeRateService.page (page, queryWrapper);
		return Result.succeed(result);
	}


	/**
	 * 根据Id获取汇率管理数据
	 */
	@ApiOperation(value = "根据Id获取汇率管理数据")
	@GetMapping("queryById")
	public Result<ExchangeRateDTO> queryById(String id) {
		return Result.succeed ( exchangeRateWrapper.toDTO ( exchangeRateService.getById ( id ) ) );
	}

	/**
	 * 保存汇率管理
	 */
	@ApiOperation(value = "保存汇率管理")
	@PostMapping("save")
	public  Result <String> save(@Valid @RequestBody ExchangeRateDTO exchangeRateDTO) {
		//新增或编辑表单保存
		exchangeRateService.saveOrUpdate (exchangeRateWrapper.toEntity (exchangeRateDTO));
        return Result.succeed ( "","保存汇率管理成功" );
	}


	/**
	 * 删除汇率管理
	 */
	@ApiOperation(value = "删除汇率管理")
	@DeleteMapping("delete")
	public  Result<String> delete(String ids) {
		String idArray[] = ids.split(",");
        exchangeRateService.removeByIds ( Lists.newArrayList ( idArray ) );
		return Result.succeed ( "", "删除汇率管理成功" );
	}

}
