package com.yami.trading.dao.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.model.UserRecom;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface UserRecomMapper  extends BaseMapper<UserRecom> {

    Page list(Page page, @Param("userName") String useName,
                    @Param("recomUserName") String recomUserName);
}
