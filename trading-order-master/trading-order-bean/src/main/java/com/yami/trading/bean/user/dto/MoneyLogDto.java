package com.yami.trading.bean.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel
public class MoneyLogDto {

    @ApiModelProperty("钱包类型 币种")
    private String walletType;
    @ApiModelProperty("币种 补充字段")
    private String symbol;


    @ApiModelProperty("账户类型")
    private  String roleName;


    private  String roleNameText;



    @ApiModelProperty("交易类型")
    private String category;

    @ApiModelProperty("交易类型文本")
    private String categoryText;

    private String userId;

    @ApiModelProperty("用户名称")
    private  String userName;


    @ApiModelProperty("用户编号")
    private  String userCode;
    private Date createTime;
    private String conf;

    private String title;
    /**
     * 交易金额
     */
    private BigDecimal amount =new BigDecimal(0);
    /**
     * 操作之后
     */

    @ApiModelProperty("变更后")
    private BigDecimal amountBefore =new BigDecimal(0);
    /**
     * 操作之前
     */
    @ApiModelProperty("变更前")
    private BigDecimal amountAfter = new BigDecimal(0);
    @ApiModelProperty("日志")
    private String log;
    /**
     * 资金日志提供的内容 ：提币 充币 永续建仓 永续平仓 手续费
     */
    @ApiModelProperty("资金日志提供的内容 ：提币 充币 永续建仓 永续平仓 手续费")
    private String contentType;


    @ApiModelProperty("推荐人 用户名")
    private  String recomUserName;

    @ApiModelProperty("推荐人 code")
    private  String recomUserCode;
}
