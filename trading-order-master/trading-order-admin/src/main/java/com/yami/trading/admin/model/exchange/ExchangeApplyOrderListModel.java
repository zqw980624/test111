package com.yami.trading.admin.model.exchange;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

@Data
@ApiModel
public class ExchangeApplyOrderListModel  extends PageRequest {

    @ApiModelProperty("账号类型")
    private  String  rolename;

    @ApiModelProperty("uuid  用户名")
    private  String  userName;

    @ApiModelProperty("订单号")
    private  String orderNo;
    @ApiModelProperty("open:买入 close:卖出")
    private String offset;

    @ApiModelProperty("状态")
    private  String  state;

    @ApiModelProperty("forex->外汇,commodities->大宗商品，指数/ETF->indices,  A-stocks->A股,  HK-stocks->港股.US-stocks->美股，cryptos->虚拟货币 ")
    private  String  type;

    @ApiModelProperty("uid")
    private String userCode;

    private String symbol;
}
