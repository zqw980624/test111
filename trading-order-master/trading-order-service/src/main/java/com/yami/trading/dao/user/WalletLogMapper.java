package com.yami.trading.dao.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.model.WalletLog;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface WalletLogMapper  extends BaseMapper<WalletLog> {


    Page<Map> pagedQueryRecharge(Page page,@Param("partyId") String partyId,
                                 @Param("orderNoNull") String order_no_null,@Param("category") String category) ;

    Page<Map> pagedQueryWithdraw(Page page,@Param("partyId") String partyId,
                                 @Param("orderNoNull") String order_no_null,@Param("category") String category);
    Page pagedQueryRecords(Page page,@Param("partyId")  String partyId,
                           @Param("category")  String category,@Param("startTime")  String startTime,@Param("endTime")  String endTime,
                           @Param("walletType")  String walletType, @Param("status")  Integer status);
}
