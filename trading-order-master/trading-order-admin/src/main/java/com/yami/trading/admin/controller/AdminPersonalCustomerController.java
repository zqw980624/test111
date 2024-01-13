package com.yami.trading.admin.controller;

import javax.servlet.http.HttpServletRequest;

import com.yami.trading.admin.dto.GoogleAuthDto;
import com.yami.trading.admin.dto.PersonalCustomerDto;
import com.yami.trading.api.util.ServletUtil;
import com.yami.trading.bean.model.Customer;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.customer.CustomerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * 客服个人中心
 *
 */
@RestController
@Api(tags = "客服个人中心")
public class AdminPersonalCustomerController {

	private Logger logger=LoggerFactory.getLogger(AdminPersonalCustomerController.class);

	@Autowired
	private CustomerService customerService;

	private final String action = "normal/adminPersonalCustomerAction!";

	/**
	 * 点击客服中心
	 */
	@GetMapping(action + "personalCustomer.action")
	@ApiOperation(value = "获取客服中心数据")
	public Result<PersonalCustomerDto> personalCustomer(HttpServletRequest request) {
		boolean off_to_online = true;
		if("/normal/adminPersonalCustomerAction!personalCustomer.action".equals(request.getServletPath())) {
			off_to_online = false;
		}

		String username = SecurityUtils.getSysUser().getUsername();
		String last_online_time = null;
		String last_offline_time = null;
		String auto_answer = null;
		Integer online_state = null;

		try {
			Customer customer = this.customerService.cacheByUsername(username);
		    if (null != customer) {
		    	last_online_time = DateUtils.format(customer.getLastOnlineTime(), DateUtils.DF_yyyyMMddHHmmss);
				last_offline_time = DateUtils.format(customer.getLastOfflineTime(), DateUtils.DF_yyyyMMddHHmmss);
				auto_answer = customer.getAutoAnswer();
				online_state = customer.getOnlineState();
		    }
		} catch (Exception e) {
			return Result.failed(e.getMessage());
		}
		PersonalCustomerDto dto = new PersonalCustomerDto();
		dto.setUsername(username);
		dto.setOff_to_online(off_to_online);
		dto.setLast_offline_time(last_offline_time);
		dto.setLast_online_time(last_online_time);
		dto.setAuto_answer(auto_answer);
		dto.setOnline_state(online_state);
		return Result.ok(dto);
	}

	/**
	 * 上线
	 */
	@GetMapping(action + "personalOnline.action")
	@ApiOperation(value = "上线")
	public Result<Boolean> personalOnline() {
		boolean off_to_online = false;
		String username = SecurityUtils.getSysUser().getUsername();
		try {
			customerService.online(username);
		    off_to_online = true;
		} catch (Exception e){
			return Result.failed(e.getMessage());
		}
		return Result.ok(off_to_online);
	}

	/**
	 * 下线
	 */
		@ApiOperation(value = "下线")
		@RequestMapping(action + "personalOffline.action")
		public Result<Boolean>  personalOffline() {
			boolean off_to_online = false;
			String username = SecurityUtils.getSysUser().getUsername();

			try {
				customerService.offline(username);
				off_to_online = true;
			} catch (Exception e){
				return Result.failed(e.getMessage());
			}
			return Result.ok(off_to_online);
		}


	@RequestMapping(action + "personalUpdateAutoAnswer.action")
	@ApiOperation(value = "更新客服中心数据")
	public Result<Boolean> personalUpdateAutoAnswer(HttpServletRequest request, @RequestParam String login_safeword, @RequestParam String auto_answer) {
		boolean off_to_online = true;
		String username = SecurityUtils.getSysUser().getUsername();
		String ip = ServletUtil.getIp(request);
		try {
			customerService.updatePersonalAutoAnswer(username, login_safeword,
					ip, auto_answer);
			off_to_online = false;
		} catch (Exception e) {
			e.printStackTrace();
			return Result.failed(e.getMessage());
		}
		return Result.ok(off_to_online);
	}

}
