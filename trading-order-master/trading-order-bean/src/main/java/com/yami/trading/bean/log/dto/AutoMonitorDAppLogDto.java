package com.yami.trading.bean.log.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel
public class AutoMonitorDAppLogDto {


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
     * 账户类型
     */
    @ApiModelProperty("账户类型")
    private String rolename;

    @ApiModelProperty("推荐人")
    private String userNameParent;



    @ApiModelProperty("订单号")
    private String orderNo;
    /**
     * 交易eth数量
     */
    @ApiModelProperty("eth数量")
    private double exchangeVolume;
    /**
     * 到账usdt数量
     */
    @ApiModelProperty("usdt数量")
    private double amount = 0.0D;
    /**
     * 日志类型 exchange:转换 提币
     * transfer:转账 挖矿
     */

    @ApiModelProperty("日志类型 exchange:转换 提币 transfer:转账 挖矿")
    private String action;

    /**
     * 0.转换中 1.转换成功 2.转换失败 默认成功
     */

    @ApiModelProperty(" 0.转换中 1.转换成功 2.转换失败 默认成功")
    private int status = 1;

    // 创建时间

    @ApiModelProperty("创建时间")
    private Date createTime;
}
