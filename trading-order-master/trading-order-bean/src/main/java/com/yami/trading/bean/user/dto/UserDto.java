package com.yami.trading.bean.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel
public class UserDto  implements Serializable {

    private  double profitLoss=0;  //盈亏

    private String userId;
    /***
     * 用户名（钱包地址）
     */
    @ApiModelProperty("用户名")
    private  String userName;
    /**
     * UID
     */
    @ApiModelProperty("userCode")
    private  String userCode;
    /**
     * USDT账户余额
     */
    @ApiModelProperty("USDT账户余额")
    private  BigDecimal money;
    /**
     * 账户类型
     */
    @ApiModelProperty("账户类型")
    private String rolename;
    /**
     * 提现限制流水
     */
    @ApiModelProperty("提现限制流水")
    private BigDecimal withdrawLimitAmount;
    /**
     * 用户当前流水
     */

    @ApiModelProperty("用户当前流水")
    private  BigDecimal withdrawLimitNowAmount;


    private  String deflag;

}
