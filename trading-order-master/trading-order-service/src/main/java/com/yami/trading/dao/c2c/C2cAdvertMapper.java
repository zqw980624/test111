package com.yami.trading.dao.c2c;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.c2c.C2cAdvert;
import org.apache.ibatis.annotations.Param;

public interface C2cAdvertMapper  extends BaseMapper<C2cAdvert> {


   Page  pagedQuery(Page page, @Param("c2cUserCode") String c2cUserCode,
                    @Param("c2cUserType") String c2cUserType,
                    @Param("userCode")  String userCode, @Param("direction") String direction,
                    @Param("currency")  String currency,
                    @Param("symbol")    String symbol);

    Page pagedQueryC2cUser(Page page,@Param("c2cUserId") String c2cUserId,
                           @Param("direction") String direction,
                           @Param("currency") String currency,
                           @Param("symbol") String symbol,
                           @Param("amountDouble") double amountDouble,
                           @Param("onSale") Integer onSale,
                           @Param("closed") Integer closed);

    Page pagedQueryNotC2cUser(Page page,@Param("c2cUserId") String c2cUserId,
                              @Param("direction") String direction,
                              @Param("currency") String currency,
                              @Param("symbol") String symbol,
                              @Param("amountDouble") double amountDouble,
                              @Param("onSale") Integer onSale,
                              @Param("closed") Integer closed);
}



