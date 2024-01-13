package com.yami.trading.dao.c2c;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.c2c.C2cAppeal;
import org.apache.ibatis.annotations.Param;

public interface C2cAppealMapper extends BaseMapper<C2cAppeal> {

    Page    pagedQuery(Page page,
                       @Param("status") String status, @Param("orderNo") String orderNo, @Param("userCode")  String userCode,@Param("roleName")  String roleName, @Param("c2cUserCode") String c2cUserCode,@Param("c2cUserType")  String c2cUserType, @Param("c2cUserPartyCode") String c2cUserPartyCode);

    long findNoHandleAppealsCountByAdvertId(@Param("c2cAdvertId") String c2cAdvertId);
}
