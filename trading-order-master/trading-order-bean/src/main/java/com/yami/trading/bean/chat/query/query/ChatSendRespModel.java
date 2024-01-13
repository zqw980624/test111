package com.yami.trading.bean.chat.query.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lucas
 */
@Data
@ApiModel("消息发送请求体")
public class ChatSendRespModel {

    @ApiModelProperty("发送时间")
    private String sendTimeStamp;

    @ApiModelProperty("消息ID")
    private String chatId;

    @ApiModelProperty("更新时间")
    private String updatetime;
}
