package com.yami.trading.service.exchange;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.exchange.ExchangeApplyOrder;
import com.yami.trading.bean.exchange.dto.ExchangeApplyOrderDto;
import com.yami.trading.bean.exchange.dto.ExchangeSymbolDto;
import com.yami.trading.bean.exchange.dto.SumEtfDto;
import com.yami.trading.bean.item.dto.RealtimeDTO;

import java.util.List;
import java.util.Map;

public interface ExchangeApplyOrderService extends IService<ExchangeApplyOrder> {

    /**
     * 创建委托单
     */
    public void saveCreate(ExchangeApplyOrder order);

    /**
     * 创建委托单
     */
    public void saveCreateyd(ExchangeApplyOrder order,String pid);

    /**
     * 所有未处理状态的委托单
     */
    public List<ExchangeApplyOrder> findSubmitted();

    /**
     * 开仓
     */
    public void saveOpen(ExchangeApplyOrder applyOrder, JSONObject msgObject);
    public void saveOpens(ExchangeApplyOrder applyOrder,  RealtimeDTO realtime);
    /**
     * 平仓，按金额进行平仓
     */
    public void saveClose(ExchangeApplyOrder applyOrder, JSONObject msgObject);
    public void saveCloses(ExchangeApplyOrder applyOrder,  RealtimeDTO realtime);

    /**
     * 撤单
     *
     * @param order_no
     */
    public void saveCancel(String partyId, String order_no);

    List<Map<String, Object>> getPaged(int pageNo, int size, String userId, String symbol, String type, String isAll, String startTime, String endTime, String symbolType,
                                           String orderPriceType);
    List<Map<String, Object>> getPageds(int pageNo, int size, String userId, String type);

    ExchangeApplyOrder findByOrderNoAndPartyId(String order_no, String userId);

    Page<ExchangeApplyOrderDto> listPage(Page page, String rolename,
                                         String userName,
                                         String orderNo,
                                         String state,String offset,String symbolType, String userCode, String symbol);

    Page<ExchangeApplyOrderDto> listPages(Page page, String rolename,
                                         String userName,
                                         String orderNo,
                                         String state,String offset,String symbolType, String userCode, String symbol,String recomCode);

    ExchangeApplyOrder findByOrderNo(String orderNo);

    SumEtfDto getProfitLossByUserId(String userId,String type);

    List<ExchangeSymbolDto> getETFListByUserId(String userId,String type);
}
