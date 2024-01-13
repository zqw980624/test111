package com.yami.trading.admin.controller.contract;

import javax.validation.constraints.NotBlank;

import com.yami.trading.admin.facade.PermissionFacade;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;

import com.yami.trading.service.user.UserRecomService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.contract.domain.ContractApplyOrder;

import com.yami.trading.bean.contract.dto.ContractApplyOrderDTO;
import com.yami.trading.bean.contract.mapstruct.ContractApplyOrderWrapper;
import com.yami.trading.service.contract.ContractApplyOrderService;
import com.yami.trading.bean.contract.query.ContractApplyOrderQuery;

import java.io.IOException;


/**
 * 订单表Controller
 * @author lucas
 * @version 2023-03-29
 */

@Slf4j
@Api(tags ="永续委托单")
@RestController
@RequestMapping(value = "normal/adminContractApplyOrderAction!")
public class ContractApplyOrderController {

	@Autowired
	private ContractApplyOrderService contractApplyOrderService;

	@Autowired
	private ContractApplyOrderWrapper contractApplyOrderWrapper;

	@Autowired
	private UserRecomService userRecomService;
	@Autowired
	private PermissionFacade permissionFacade;

	/**
	 * 获取 永续合约委托列表
	 */
	@ApiOperation(value = "获取 永续合约委托列表")
	@GetMapping("list.action")
	public Result<IPage<ContractApplyOrderDTO>> list(ContractApplyOrderQuery contractApplyOrderQuery, Page<ContractApplyOrder> page) throws Exception {

		contractApplyOrderQuery.setChildren(permissionFacade.getOwnerUserIds());

		IPage<ContractApplyOrderDTO> result = contractApplyOrderService.listRecord (page, contractApplyOrderQuery);
		return Result.succeed (result);
	}




	/**
	 * 撤单
	 * <p>
	 * order_no 订单号
	 */
	@GetMapping("close.action")
	@ApiOperation(value = "平仓或撤单")
	public Result<String> cancel(@RequestParam @NotBlank String orderNo) throws IOException {
		try {
			ContractApplyOrder order = this.contractApplyOrderService.findByOrderNo(orderNo);
			if (order != null) {
				this.contractApplyOrderService.saveCancel(order.getPartyId().toString(), orderNo);
			}
		} catch (Exception e) {
			log.error("执行撤单异常", e);
			throw new YamiShopBindException("执行撤单异常");
		}
		return Result.succeed ("success");
	}
}
