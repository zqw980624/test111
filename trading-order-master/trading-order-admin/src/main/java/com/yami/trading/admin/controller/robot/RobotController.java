package com.yami.trading.admin.controller.robot;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.robot.domain.Robot;
import com.yami.trading.bean.robot.dto.RobotDTO;
import com.yami.trading.bean.robot.mapstruct.RobotWrapper;
import com.yami.trading.bean.robot.query.RobotQuery;
import com.yami.trading.bean.robot.vo.RobotVO;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.query.QueryWrapperGenerator;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.robot.RobotService;
import com.yami.trading.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;


/**
 * 下单机器人Controller
 * @author lucas
 * @version 2023-05-04
 */

@Api(tags ="下单机器人")
@RestController
@RequestMapping(value = "/etf/robot")
public class RobotController {

	@Autowired
	private RobotService robotService;

	@Autowired
	private RobotWrapper robotWrapper;

	@Autowired
	private UserService userService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ItemService itemService;

	/**
	 * 下单机器人列表数据
	 */
	@ApiOperation(value = "查询下单机器人列表数据")
	@GetMapping("list")
	public Result<IPage<Robot>> list(RobotQuery robotQuery, Page<Robot> page) throws Exception {
		QueryWrapper queryWrapper = QueryWrapperGenerator.buildQueryCondition (robotQuery, RobotQuery.class);
		queryWrapper.orderByDesc("create_time");
		IPage<Robot> result = robotService.page (page, queryWrapper);
		for (Robot robot : result.getRecords()) {
			String symbol = robot.getSymbol();
			if(StringUtils.isNotEmpty(symbol)){
				QueryWrapper<Item> itemSummaryQueryWrapper = new QueryWrapper<>();
				itemSummaryQueryWrapper.eq("symbol", symbol);
				List<Item> list = itemService.list(itemSummaryQueryWrapper);
				Item item = itemService.findBySymbol(list.get(0).getSymbol());
				//Item item = itemService.findBySymbol(symbol);
				if (item != null) {
					robot.setItem(item);
				}
			}

		}
		return Result.ok (result);
	}


	/**
	 * 根据Id获取下单机器人数据
	 */
	@ApiOperation(value = "根据Id获取下单机器人数据")
	@GetMapping("queryById")
	public Result<RobotDTO> queryById(String id) {
		return Result.ok ( robotWrapper.toDTO ( robotService.getById ( id ) ) );
	}

	/**
	 * 保存下单机器人
	 */
	@ApiOperation(value = "保存下单机器人")
	@PostMapping("save")
	public  Result <String> save(@Valid @RequestBody RobotDTO robotDTO) {
		String password = robotDTO.getPassword();
		if(StringUtils.isEmptyString(password)){
			throw new YamiShopBindException("密码不能为空");
		}

		Robot robot = robotWrapper.toEntity(robotDTO);

		User user = userService.findByUserName(robotDTO.getUsername());
		if(user==null){
			user = userService.registerMobile(robotDTO.getUsername(), passwordEncoder.encode(password), null,  true);
			robot.setUser(user.getUserId());
			robot.setCreateTime(new Date());
		}else{
			//用户存在，并且不是机器人
			Robot robot1 = robotService.getOne(new LambdaQueryWrapper<Robot>().eq(Robot::getUser, user.getUserId()));
			if(robot1 == null){
				throw new YamiShopBindException("账号已存在");
			}
//			robotService.getOne(robot);
//			robot.getUser(user.getUserId());
//			robotService.getById()
		}

		//新增或编辑表单保存
		robotService.saveOrUpdate (robot);
		return Result.ok ( "保存下单机器人成功" );
	}


	/**
	 * 删除下单机器人
	 */
	@ApiOperation(value = "删除下单机器人")
	@DeleteMapping("delete")
	public Result <String> delete(String ids) {
		String idArray[] = ids.split(",");
		robotService.removeByIds ( Lists.newArrayList ( idArray ) );
		return Result.ok( "删除下单机器人成功" );
	}

}
