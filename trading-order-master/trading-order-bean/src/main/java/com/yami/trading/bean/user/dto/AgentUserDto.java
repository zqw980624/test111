package com.yami.trading.bean.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class AgentUserDto {

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
     * 账户类型
     */
    @ApiModelProperty("账户类型")
    private String rolename;
}
