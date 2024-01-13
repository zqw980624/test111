/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */

package com.yami.trading.dao.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.user.dto.AgentUserDto;
import com.yami.trading.bean.user.dto.UserDataDto;
import com.yami.trading.bean.user.dto.UserDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

	User getUserByUserMail(@Param("userMail") String userMail);

	User selectOneByUserName(@Param("userName") String userName);

	Page<UserDto> listUser(Page page,@Param("roleNames")  List<String> roleNames, @Param("userCode") String userCode,@Param("userName")   String userName,@Param("checkedList") List<String> checkedList);

   Page<UserDataDto> listUserAndRecom(Page page,@Param("roleNames")  List<String> roleNames, @Param("userCode") String userCode,@Param("userName")   String userName,
									  @Param("lastIp")   String lastIp,@Param("checkedList") List<String> checkedList);

	Page<AgentUserDto> getAgentAllStatistics(Page page,@Param("userName") String userName,@Param("children")
											 List<String> children);
}
