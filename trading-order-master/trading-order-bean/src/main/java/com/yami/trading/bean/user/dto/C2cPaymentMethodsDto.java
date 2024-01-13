package com.yami.trading.bean.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel
public class C2cPaymentMethodsDto implements Serializable {
    /**
     * 代理商商真实姓名
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
     * 参数名2
     */
    private String paramName3;

    private String paramName4;
    /**
     * 参数值2
     */
    private String paramValue4;

    private  String id;

    @ApiModelProperty("用户名")
    private  String userName;

    @ApiModelProperty("UID(推荐码)")
    private  String userCode;

    private  String   methodType;

    @ApiModelProperty("上级推荐人")
    private   String userNameParent;

    @ApiModelProperty("分享地址")
    private  String shareUrl;

    @ApiModelProperty("备注")
    private  String remarks;


    @ApiModelProperty("登录权限")
    private  boolean loginAuthority;

    @ApiModelProperty("操作权限")
    private  boolean operaAuthority;

    private  String roleName;

    private  int status;

    private boolean googleAuthBind;
}
