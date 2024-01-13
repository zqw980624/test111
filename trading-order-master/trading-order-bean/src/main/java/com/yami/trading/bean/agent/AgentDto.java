package com.yami.trading.bean.agent;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel
public class AgentDto implements Serializable {

    private  String id;

    @ApiModelProperty("用户名")
    private  String userName;

    @ApiModelProperty("UID(推荐码)")
    private  String userCode;


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

    private  String flag;//
}
