package com.yami.trading.dao.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.model.RealNameAuthRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RealNameAuthRecordMapper  extends BaseMapper<RealNameAuthRecord> {

    Page pageRecord(Page page,@Param("rolename")  String rolename,
                    @Param("idNumber") String idNumber,@Param("status") String status,
                    @Param("userCode") String userCode);


    Page pageRecords(Page page,@Param("roleNames") List<String> roleNames,
                     @Param("idNumber") String idNumber,@Param("status") String status,
                     @Param("userCode") String userCode,@Param("checkedList") List<String> checkedList);

}
