package com.yami.trading.dao.c2c;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.c2c.C2cUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface C2cUserMapper  extends BaseMapper<C2cUser> {

    Page pagedQuery(Page page,
                    @Param("c2cUserId") String c2cUserId,
                    @Param("c2cUserType") String c2cUserType,
                    @Param("c2cUserPartyId") String c2cUserPartyId,
                    @Param("c2cManagerName") String c2cManagerName);

    List<Map<String,Object>> getC2cManagerC2cUser(@Param("managerId") String managerId);

    List<Map<String,Object>>   getAllC2cManager(@Param("roleUuid") String roleUuid);
}
