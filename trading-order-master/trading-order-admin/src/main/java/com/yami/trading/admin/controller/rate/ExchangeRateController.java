package com.yami.trading.admin.controller.rate;

import javax.validation.Valid;
import com.google.common.collect.Lists;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.query.QueryWrapperGenerator;

import com.yami.trading.common.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.rate.domain.ExchangeRate;

import com.yami.trading.bean.rate.dto.ExchangeRateDTO;
import com.yami.trading.bean.rate.mapstruct.ExchangeRateWrapper;
import com.yami.trading.service.rate.ExchangeRateService;
import com.yami.trading.bean.rate.query.ExchangeRateQuery;




/**
 * 汇率管理Controller
 * @author lucas
 * @version 2023-03-28
 */

@Api(tags ="汇率管理")
@RestController
@RequestMapping(value = "/rate/exchangeRate")
public class ExchangeRateController {

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
		QueryWrapper<ExchangeRate> queryWrapper = new QueryWrapper<>();
		String name = exchangeRateQuery.getName();
		queryWrapper.and(StringUtils.isNotEmpty(name), w->w.like("name",name).or().like("currency", name).or().like("currency_symbol", name));
		IPage<ExchangeRate> result = exchangeRateService.page (page, queryWrapper);
		return Result.ok (result);
	}


	/**
	 * 根据Id获取汇率管理数据
	 */
	@ApiOperation(value = "根据Id获取汇率管理数据")
	@GetMapping("queryById")
	public Result<ExchangeRateDTO> queryById(String id) {
		return Result.ok ( exchangeRateWrapper.toDTO ( exchangeRateService.getById ( id ) ) );
	}

	/**
	 * 保存汇率管理
	 */
	@ApiOperation(value = "保存汇率管理")
	@PostMapping("save")
	public  Result <String> save(@Valid @RequestBody ExchangeRateDTO exchangeRateDTO) {
		//新增或编辑表单保存
		exchangeRateService.saveOrUpdate (exchangeRateWrapper.toEntity (exchangeRateDTO));
        return Result.ok ( "保存汇率管理成功" );
	}


	/**
	 * 删除汇率管理
	 */
	@ApiOperation(value = "删除汇率管理")
	@DeleteMapping("delete")
	public Result <String> delete(String ids) {
		String idArray[] = ids.split(",");
        exchangeRateService.removeByIds ( Lists.newArrayList ( idArray ) );
		return Result.ok( "删除汇率管理成功" );
	}

}
