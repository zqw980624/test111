package com.yami.trading.bean.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel
public class UserBasicDto implements Serializable {
    private String userId;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("用户层级")
    private Integer userLevel;

    @ApiModelProperty("UID")
    private String uid;

    @ApiModelProperty("账户类型")
    private String accountType;

    @ApiModelProperty("基础认证")
    private boolean realNameAuthority;
}
