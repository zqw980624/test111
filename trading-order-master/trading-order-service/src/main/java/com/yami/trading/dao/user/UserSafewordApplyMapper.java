package com.yami.trading.dao.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.model.UserSafewordApply;
import org.apache.ibatis.annotations.Param;

public interface UserSafewordApplyMapper extends BaseMapper<UserSafewordApply> {


    Page listRecord(Page page, @Param("roleName")  String roleName, @Param("status") String status,
                    @Param("userCode") String userCode, @Param("userName") String userName,@Param("operate") String  operate);
}
