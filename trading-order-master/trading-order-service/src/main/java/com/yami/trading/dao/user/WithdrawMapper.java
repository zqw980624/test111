package com.yami.trading.dao.user;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.model.Wallet;
import com.yami.trading.bean.model.Withdraw;
import org.apache.ibatis.annotations.Param;

public interface WithdrawMapper extends BaseMapper<Withdraw> {

    Page listRecord(Page page, @Param("status")  String status, @Param("roleName") String roleName,
                    @Param("userName") String userName, @Param("orderNo") String  orderNo);


    Page listRecords(Page page, @Param("status")  String status, @Param("roleName") String roleName,
                     @Param("userName") String userName, @Param("orderNo") String  orderNo, @Param("qdcode") String  qdcode);
}

