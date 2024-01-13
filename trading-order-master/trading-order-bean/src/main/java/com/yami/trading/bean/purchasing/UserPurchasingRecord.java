package com.yami.trading.bean.purchasing;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.trading.common.domain.UUIDEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户申购记录行
 */
@Data
@TableName("t_user_purchasing_record")
public class UserPurchasingRecord extends UUIDEntity {


    /**
     * 创建日期
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新日期
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;



    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private int delFlag;

    private  String userId;

    @ApiModelProperty("认购付款币种数量" )
    private  BigDecimal  payCurrencyQuantity;


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



}
