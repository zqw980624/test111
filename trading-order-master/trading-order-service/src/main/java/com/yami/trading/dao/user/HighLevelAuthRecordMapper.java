package com.yami.trading.dao.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.model.HighLevelAuthRecord;
import org.apache.ibatis.annotations.Param;

public interface HighLevelAuthRecordMapper  extends BaseMapper<HighLevelAuthRecord> {


    Page listRecord(Page page,@Param("rolename")  String rolename,@Param("status") String status,
                    @Param("userName") String userName);
}
