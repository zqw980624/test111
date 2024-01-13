package com.yami.trading.admin.controller.rate;

import javax.validation.Valid;
import com.google.common.collect.Lists;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.query.QueryWrapperGenerator;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.rate.domain.UserRateConfig;

import com.yami.trading.bean.rate.dto.UserRateConfigDTO;
import com.yami.trading.bean.rate.mapstruct.UserRateConfigWrapper;
import com.yami.trading.service.rate.UserRateConfigService;
import com.yami.trading.bean.rate.query.UserRateConfigQuery;




/**
 * 用户汇率管理Controller
 * @author lucas
 * @version 2023-03-28
 */

@Api(tags ="用户汇率管理")
@RestController
@RequestMapping(value = "/rate/userRateConfig")
public class UserRateConfigController {

	@Autowired
	private UserRateConfigService userRateConfigService;

	@Autowired
	private UserRateConfigWrapper userRateConfigWrapper;

	/**
	 * 用户汇率管理列表数据
	 */
	@ApiOperation(value = "查询用户汇率管理列表数据")
	@GetMapping("list")
	public Result<IPage<UserRateConfig>> list(UserRateConfigQuery userRateConfigQuery, Page<UserRateConfig> page) throws Exception {
		QueryWrapper queryWrapper = QueryWrapperGenerator.buildQueryCondition (userRateConfigQuery, UserRateConfigQuery.class);
		IPage<UserRateConfig> result = userRateConfigService.page (page, queryWrapper);
		return Result.ok (result);
	}


	/**
	 * 根据Id获取用户汇率管理数据
	 */
	@ApiOperation(value = "根据Id获取用户汇率管理数据")
	@GetMapping("queryById")
	public Result<UserRateConfigDTO> queryById(String id) {
		return Result.ok ( userRateConfigWrapper.toDTO ( userRateConfigService.getById ( id ) ) );
	}

	/**
	 * 保存用户汇率管理
	 */
	@ApiOperation(value = "保存用户汇率管理")
	@PostMapping("save")
	public  Result <String> save(@Valid @RequestBody UserRateConfigDTO userRateConfigDTO) {
		//新增或编辑表单保存
		userRateConfigService.saveOrUpdate (userRateConfigWrapper.toEntity (userRateConfigDTO));
        return Result.ok ( "保存用户汇率管理成功" );
	}


	/**
	 * 删除用户汇率管理
	 */
	@ApiOperation(value = "删除用户汇率管理")
	@DeleteMapping("delete")
	public Result <String> delete(String ids) {
		String idArray[] = ids.split(",");
        userRateConfigService.removeByIds ( Lists.newArrayList ( idArray ) );
		return Result.ok( "删除用户汇率管理成功" );
	}

}
