package com.yami.trading.bean.model;

import com.baomidou.mybatisplus.annotation.*;
import com.yami.trading.common.domain.BaseEntity;
import com.yami.trading.common.domain.UUIDEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_c2c_payment_method")
public class C2cPaymentMethod  extends UUIDEntity {

    private static final long serialVersionUID = -1883480204147696409L;

    /**
     * 用户PARTY_ID
     */
    private String partyId;

    /**
     * 支付方式类型：/1银行卡/2upt
     */
    private int methodType;

    /**
     * 支付方式类型名称
     */
    @TableField(exist = false)
    private String methodTypeName;

    /**
     * 支付方式名称
     */
    private String methodName;

    /**
     * 支付方式图片
     */
    private String methodImg;

    /**
     * 承兑商真实姓名
     */
    private String realName;

    /**
     * 参数名1
     */
    private String paramName1;

    /**
     * 参数值1
     */
    private String paramValue1;

    /**
     * 参数名2
     */
    private String paramName2;

    /**
     * 参数值2
     */
    private String paramValue2;

    /**
     * 参数名3
     */
    private String paramName3;


    /**
     * 参数名4
     */
    private String paramName4;

    /**
     * 参数值4
     */
    private String paramValue4;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    @TableField(exist = false)
    private  String qrcodePath;


}
