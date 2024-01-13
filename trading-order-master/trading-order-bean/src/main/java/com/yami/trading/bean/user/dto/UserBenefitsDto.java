package com.yami.trading.bean.user.dto;

import com.yami.trading.bean.cms.Banner;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel
public class UserBenefitsDto {
    @ApiModelProperty("角色")
    private String roleName;
    @ApiModelProperty("用户名")
    private String userName;
    @ApiModelProperty("uuid")
    private String userCode;

    private  String userId;
    @ApiModelProperty("团队人数")
    private int recoNum;
    @ApiModelProperty("USDT ")
    private BigDecimal money = new BigDecimal(0);
    @ApiModelProperty("充值 usdt")
    private BigDecimal rechargeUsdt = new BigDecimal(0);
    @ApiModelProperty("充值 ETH")
    private BigDecimal rechargeEth = new BigDecimal(0);
    @ApiModelProperty("充值 BTC ")
    private BigDecimal rechargeBtc = new BigDecimal(0);
    @ApiModelProperty("提现 usdt")
    private BigDecimal withdraw = new BigDecimal(0);
    @ApiModelProperty("提现 ETH")
    private BigDecimal withdrawEth = new BigDecimal(0);
    @ApiModelProperty("提现 BTC ")
    private BigDecimal withdrawBtc = new BigDecimal(0);

    @ApiModelProperty("赠送(USDT)")
    private  BigDecimal giftMoney;

    @ApiModelProperty("充提差额")
    private  BigDecimal difference;


    private String userCodes ;

    @ApiModelProperty("交易盈亏")
    private  BigDecimal businessProfit;

    @ApiModelProperty("矿机盈亏")
    private  BigDecimal minerIncome;

    @ApiModelProperty("手续费")
    private BigDecimal totleFee = new BigDecimal(0);
    @ApiModelProperty("总收益")
    private BigDecimal totleIncome = new BigDecimal(0);
}
