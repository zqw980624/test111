package com.yami.trading.admin.controller.future;

import com.yami.trading.admin.facade.PermissionFacade;
import com.yami.trading.common.domain.Result;

import com.yami.trading.security.common.util.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.future.domain.FuturesOrder;

import com.yami.trading.bean.future.dto.TFuturesOrderDTO;
import com.yami.trading.bean.future.mapstruct.TFuturesOrderWrapper;
import com.yami.trading.service.future.FuturesOrderService;
import com.yami.trading.bean.future.query.FuturesOrderQuery;


/**
 * 交割合约订单Controller
 * @author lucas
 * @version 2023-04-08
 */

@Api(tags ="【管理后台】交割合约订单列表")
@RestController
@RequestMapping(value = "normal/adminFuturesOrderAction!")
public class AdminFuturesOrderController {

	@Autowired
	private FuturesOrderService futuresOrderService;

	@Autowired
	private TFuturesOrderWrapper tFuturesOrderWrapper;
	@Autowired
	private PermissionFacade permissionFacade;

	/**
	 * 交割合约订单列表数据
	 */
	@ApiOperation(value = "获取 交割合约单 列表")
	@GetMapping("list.action")
	public Result<IPage<TFuturesOrderDTO>> listRecord(FuturesOrderQuery futuresOrderQuery, Page<FuturesOrder> page) throws Exception {
		futuresOrderQuery.setChildren(permissionFacade.getOwnerUserIds());

		IPage<TFuturesOrderDTO> result = futuresOrderService.listRecord(page, futuresOrderQuery);
		return Result.ok (result);
	}

	/**
	 * orderProfitLoss
	 *
	 * profit_loss 盈利还是亏损
	 */
	@ApiOperation(value = "盈利还是亏损")
	@GetMapping("orderProfitLoss.action")
	public Result<String> orderProfitLoss(@RequestParam String orderNo, @RequestParam String profitLoss) {
		futuresOrderService.saveOrderPorfitOrLoss(orderNo, profitLoss, SecurityUtils.getSysUser().getUsername());
		return Result.ok("操作成过");

	}
}
