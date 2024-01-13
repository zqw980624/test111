package com.yami.trading.bean.purchasing.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel
public class UserPurchasingRecordDto {



    /***
     * 用户名
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

    /**
     * 创建日期
     */
    private Date createTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新日期
     */
    private Date updateTime;



    /**
     * 更新人
     */
    private String updateBy;


    private  String userId;

    @ApiModelProperty("认购付款币种数量" )
    private BigDecimal payCurrencyQuantity;


    @ApiModelProperty("支付币种" )
    private  String payCurrency;


    @ApiModelProperty("申购项目名称" )
    private  String projectName;
    /**
     * 申购id
     */
    @ApiModelProperty("申购id")
    private  String purchasingId;


    @ApiModelProperty("申购项目币种数量" )
    private BigDecimal currencyQuantity;
    private  int projectType;

    @TableField(exist = false)
    @ApiModelProperty("所属项目种类" )
    private  String projectTypeName="test";
}
