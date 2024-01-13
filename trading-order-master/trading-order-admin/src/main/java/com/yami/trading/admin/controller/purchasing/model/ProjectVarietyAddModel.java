package com.yami.trading.admin.controller.purchasing.model;

import com.yami.trading.bean.purchasing.dto.RelatedStockDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel
public class ProjectVarietyAddModel {


    /**
     * 相关股票品种
     */
    @ApiModelProperty("相关股票品种")
    private List<String> relatedStockVarieties;

    /**
     * 项目总类
     */
    @ApiModelProperty("项目总类Symbol")
    private  String projectTypeSymbol;

    private  String projectTypeName;

    /**
     * 数据源类别
     */
    @ApiModelProperty("数据源类别  1 机器人 2  第三方数据采集")
    private  int dataType;
    /**
     * 代码
     */
    @ApiModelProperty("代码")
    private  String code;
    /**
     * 交易对
     */

    @ApiModelProperty("交易对")
    private  String  transactionPairsSymbol;
    /**
     * 每张金额
     */

    @ApiModelProperty("每张金额")
    private BigDecimal amount;
    /**
     * 每张手续费
     */
    @ApiModelProperty("每张手续费")
    private  BigDecimal fee;
    /**
     * 最小变动单位
     */
    @ApiModelProperty("最小变动单位")
    private  String minUnit;
    /**
     * 最小变动单位的盈亏金额
     */
    @ApiModelProperty("最小变动单位的盈亏金额")
    private BigDecimal minProfitLoss;

    /**
     * 项目名称
     */

    @ApiModelProperty("项目名称")
    private  String projectName;

    /**
     * 成交量
     */
    @ApiModelProperty("成交量")
    private  BigDecimal turnover;
    /**
     * 初始化价格
     */
    @ApiModelProperty("初始化价格")
    private  BigDecimal initPrice;
    @ApiModelProperty("状态 0下架 1 上架")
    private  int status;








}
