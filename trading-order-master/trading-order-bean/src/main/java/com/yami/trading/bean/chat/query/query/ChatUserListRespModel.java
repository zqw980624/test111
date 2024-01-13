package com.yami.trading.bean.chat.query.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author lucas
 */
@Data
@ApiModel("在线聊天消息列表返回体")
public class ChatUserListRespModel {

    @ApiModelProperty("消息用户id")
    private String id;

    @ApiModelProperty("用户名称")
    private String userName;

    @ApiModelProperty("userCode")
    private String uid;

    @ApiModelProperty("partyId")
    private String partyId;

    @ApiModelProperty("备注")
    private String remarks;

    @ApiModelProperty("未读消息")
    private String unreadmsg;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("最近一次消息时间")
    private String updatetime;

    @ApiModelProperty("排序时间")
    private Date orderUpdatetime;

}
