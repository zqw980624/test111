package com.yami.trading.admin.controller.c2c.model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class C2cAdvertAddModel {


    private  String  id;
    @ApiModelProperty("承兑商UID")
    private String c2c_user_code;


    @ApiModelProperty("承兑商剩余保证金")
    private String all_deposit;
    @ApiModelProperty("广告保证金")
    private String deposit_open;

    @ApiModelProperty("买卖方式  buy 买入 sell 卖出")
    private String direction;

    @ApiModelProperty("支付方式1")
    private String payment_method1;
    @ApiModelProperty("支付方式2")
    private String payment_method2;
    @ApiModelProperty("支付方式3")
    private String payment_method3;
    @ApiModelProperty("支付币种")
    private String currency;
    @ApiModelProperty("上架币种")
    private String symbol;
    @ApiModelProperty("交易币种数量")

    private String coin_amount;

    @ApiModelProperty("最大可交易数量")
    private String coin_amount_max;
    @ApiModelProperty("币种单价")
    private String symbol_value;

    @ApiModelProperty("单笔订单支付金额区间 ")
    private String investment_min;

    @ApiModelProperty("单笔订单支付金额区间 ")
    private String investment_max;



    @ApiModelProperty("是否上架  0 下架 1 上架")
    private String on_sale;

    @ApiModelProperty("排序索引")
    private String sort_index;
    @ApiModelProperty("支付时效")
    private String expire_time;



    @ApiModelProperty("交易条款")
    private String transaction_terms;
    @ApiModelProperty("订单自动消息")
    private String order_msg;

    @ApiModelProperty("备注")
    private String remark;

    private String deposit_total;


    private String symbol_close;
    @ApiModelProperty("支付比率")
    private String pay_rate;

    @ApiModelProperty("最小最大限额")
    private String investment_min_limit;
    @ApiModelProperty("最小最大限额")
    private String investment_max_limit;
    @ApiModelProperty("币种市价")
    private String price;
    private String login_safeword;

}
