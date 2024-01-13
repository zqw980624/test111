package com.yami.trading.admin.controller.robot;

import cn.hutool.core.lang.Tuple;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.robot.domain.RobotOrder;
import com.yami.trading.bean.robot.dto.RobotOrderDTO;
import com.yami.trading.bean.robot.mapstruct.RobotOrderWrapper;
import com.yami.trading.bean.robot.query.RobotOrderQuery;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.query.QueryWrapperGenerator;
import com.yami.trading.common.util.UTCDateUtils;
import com.yami.trading.service.robot.RobotOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 机器人下单Controller
 * @author lucas
 * @version 2023-05-27
 */

@Api(tags ="机器人订单")
@RestController
@RequestMapping(value = "/robot/robotOrder")
public class RobotOrderController {

	@Autowired
	private RobotOrderService robotOrderService;

	@Autowired
	private RobotOrderWrapper robotOrderWrapper;

	/**
	 * 机器人下单列表数据
	 */
	@ApiOperation(value = "查询机器人下单列表数据")
	@GetMapping("list")
	public Result<IPage<RobotOrder>> list(RobotOrderQuery robotOrderQuery, Page<RobotOrder> page) throws Exception {
		QueryWrapper queryWrapper = QueryWrapperGenerator.buildQueryCondition (robotOrderQuery, RobotOrderQuery.class).orderBy(true, false, "ts");
		if (robotOrderQuery.getTime() != null) {
			Tuple tuple = UTCDateUtils.getStartAndEnd(robotOrderQuery.getTime());
			queryWrapper.between("ts",tuple.get(0) , tuple.get(1));
		}
		IPage<RobotOrder> result = robotOrderService.page (page, queryWrapper);
		return Result.ok (result);
	}


	/**
	 * 根据Id获取机器人下单数据
	 */
	@ApiOperation(value = "根据Id获取机器人下单数据")
	@GetMapping("queryById")
	public Result<RobotOrderDTO> queryById(String id) {
		return Result.ok ( robotOrderWrapper.toDTO ( robotOrderService.getById ( id ) ) );
	}
}
