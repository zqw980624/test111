package com.yami.trading.dao.exchange;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.exchange.ExchangeApplyOrder;
import com.yami.trading.bean.exchange.dto.ExchangeApplyOrderDto;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

public interface ExchangeApplyOrderMapper extends BaseMapper<ExchangeApplyOrder> {


    Page<ExchangeApplyOrderDto> listPage(Page page, @Param("rolename")  String rolename,
                                         @Param("userName") String userName,
                                         @Param("orderNo") String orderNo,
                                         @Param("state") String state,
                                         @Param("offset") String offset,
                                         @Param("symbolType") String symbolType,
                                         @Param("userCode") String userCode,
                                         @Param("symbol") String symbol

    );

    Page<ExchangeApplyOrderDto> listPages(Page page, @Param("rolename")  String rolename,
                                         @Param("userName") String userName,
                                         @Param("orderNo") String orderNo,
                                         @Param("state") String state,
                                         @Param("offset") String offset,
                                         @Param("symbolType") String symbolType,
                                         @Param("userCode") String userCode,
                                         @Param("symbol") String symbol,
                                          @Param("recomCode") String recomCode

    );
}
