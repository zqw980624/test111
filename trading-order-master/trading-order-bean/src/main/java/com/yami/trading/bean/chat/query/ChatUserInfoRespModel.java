package com.yami.trading.bean.chat.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lucas
 */
@Data
@ApiModel("在线聊天")
public class ChatUserInfoRespModel {

    @ApiModelProperty("备注")
    private String remarks;

    @ApiModelProperty("partyId")
    private String partyId;

    @ApiModelProperty("用户名称")
    private String userName;

    @ApiModelProperty("用户code")
    private String userCode;

    @ApiModelProperty("最近一次登录时间")
    private String lastLoginTime;

    @ApiModelProperty("创建时间")
    private String createtime;

    @ApiModelProperty("角色名称")
    private String roleName;

    @ApiModelProperty("登入ip")
    private String loginIp;
}
