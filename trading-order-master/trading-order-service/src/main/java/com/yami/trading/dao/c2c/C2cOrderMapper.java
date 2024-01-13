package com.yami.trading.dao.c2c;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.c2c.C2cOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface C2cOrderMapper  extends BaseMapper<C2cOrder> {

    Page pagedQuery(Page page, @Param("direction")  String direction, @Param("state") String state,
               @Param("party_id") String party_id);

    Page pagedC2cQuery(Page page,
                       @Param("state") String state,
                       @Param("orderNo") String orderNo,
                       @Param("userCode") String userCode,
                       @Param("roleName") String roleName,
                       @Param("c2cUserCode") String c2cUserCode,
                       @Param("c2cUserType") String c2cUserType,
                       @Param("c2cUserPartyCode") String c2cUserPartyCode,
                       @Param("direction")  List<String> direction

                      );



    Page  pagedBankCardOrderQuery(Page page,
                                  @Param("direction")  List<String> direction,
                                  @Param("state") String state,
                                  @Param("userCode") String userCode,
                                  @Param("roleName") String roleName,
                                  @Param("orderNo") String orderNo
                                  );
}
