package com.yami.trading.bean.exchange.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel
public class ExchangeApplyOrderDto {
    private  String id;
    @ApiModelProperty("用户")
    private  String userName;
    @ApiModelProperty("usercode")
    private  String userCode;
    @ApiModelProperty("账户类型")
    private  String roleName;
    @ApiModelProperty("推荐人")
    private  String usernameParent;
    @ApiModelProperty("股票代码")
    private  String symbol;
    @ApiModelProperty("股票pid")
    private  String pid;
    @ApiModelProperty("品种名称")
    private  String symbolName;
    /**
     * "open":买入 "close":卖出
     */

    @ApiModelProperty(" 操作  open:买入 close 卖出 ")
    private String offset;


    @ApiModelProperty("币种数量委托数量")
    private Double symbolValue;


    @ApiModelProperty("订单号")
    private String orderNo;


    @ApiModelProperty("委托数量")
    private Double volume;


    @ApiModelProperty(" 订单报价类型。 limit:限价  opponent:对手价（市价）")
    private String orderPriceType;


    @ApiModelProperty("限价")
    private Double price;



    /**
     * 状态。submitted 已提交，canceled 已撤销， created 委托完成
     */

    @ApiModelProperty("状态。submitted 已提交，canceled 已撤销， created 委托完成")
    private String state = "submitted";
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private Date createTime;
    /**
     * 成交时行情点位
     */
    @ApiModelProperty("成交价格")
    private Double closePrice;
    /**
     * 成交时间
     */
    @ApiModelProperty("成交时间")
    private Date closeTime;
    @ApiModelProperty("邀请码")
    private String recomCode;

}
