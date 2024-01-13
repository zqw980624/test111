package com.yami.trading.bean.log.dto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
@Data
@ApiModel
public class LogDto {

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

    private  String roleNameText;

    @ApiModelProperty("日志类型")
    private String category;


    @ApiModelProperty("日志")
    private String log;


    @ApiModelProperty("操作人")
    private String operator;


    @ApiModelProperty("操作时间")
    private Date createTime;


}
