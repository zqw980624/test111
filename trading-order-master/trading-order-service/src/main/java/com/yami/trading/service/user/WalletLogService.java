package com.yami.trading.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.model.WalletLog;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface WalletLogService   extends IService<WalletLog> {

    Page<Map> pagedQueryRecharge(String partyId,
                                  String order_no_null, Page page);

    public Page pagedQueryWithdraw(int pageNo, int pageSize, String partyId, String order_no_null);

    void updateStatus(String orderNo, int status);


    public WalletLog find(String category, String order_no);

    public Page pagedQueryRecords(int pageNo, int pageSize, String partyId, String category, String start_time, String end_time, String symbol, Integer status);


}
