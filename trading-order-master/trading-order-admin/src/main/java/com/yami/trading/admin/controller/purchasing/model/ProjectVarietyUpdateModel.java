package com.yami.trading.admin.controller.purchasing.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel
public class ProjectVarietyUpdateModel {

    @ApiModelProperty("id")
    private  String  id;

    private  String projectTypeSymbol;

    private  String projectTypeName;

    @ApiModelProperty("相关股票品种")
    private List<String> relatedStockVarieties;

    @ApiModelProperty("数据源类别  1 机器人 2  第三方数据采集")
    private  int dataType;

    @ApiModelProperty("代码")
    private  String code;

    @ApiModelProperty("交易对")
    private  String  transactionPairsSymbol;

    @ApiModelProperty("每张金额")
    private BigDecimal amount;

    @ApiModelProperty("每张手续费")
    private  BigDecimal fee;

    @ApiModelProperty("最小变动单位")
    private  String minUnit;

    @ApiModelProperty("最小变动单位的盈亏金额")
    private BigDecimal minProfitLoss;

    @ApiModelProperty("项目名称")
    private  String projectName;

    @ApiModelProperty("成交量")
    private  BigDecimal turnover;

    @ApiModelProperty("初始化价格")
    private  BigDecimal initPrice;

    @ApiModelProperty("状态 0下架 1 上架")
    private  int status;
}

