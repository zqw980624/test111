package com.yami.trading.bean.c2c.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel
public class C2cPaymentMethodDto {

    private  String  uuid;
    private  String userCode;
    private  String usrName;
    private  String deflag;
    /**
     * 支付方式类型：/1银行卡/2ups
     */
    @ApiModelProperty("支付方式类型：/1银行卡/2ups")
    private int methodType;

    /**
     * 支付方式类型名称
     */
    @TableField(exist = false)
    private String methodTypeName;

    /**
     * 支付方式名称
     */
    @ApiModelProperty("支付方式名称")
    private String methodName;

    /**
     * 支付方式图片
     */
    @ApiModelProperty("支付方式图片")
    private String methodImg;

    private String methodImgUrl;


    /**
     * 承兑商真实姓名
     */
    @ApiModelProperty("代理商真实姓名")
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
     * 参数值3
     */
    private String paramValue3;

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
    @ApiModelProperty("备注")
    private String remark;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private Date updateTime;


    @ApiModelProperty("用户id")
    private  String userId;


    @ApiModelProperty("用户名")
    private  String userName;
}
