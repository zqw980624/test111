package com.yami.trading.bean.chat.query.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lucas
 */
@Data
@ApiModel("聊天记录列表请求参数")
public class ChatRecordReqModel {

    @ApiModelProperty("messageId")
    private String message_id;

    @ApiModelProperty("partyId")
    private String partyId;
}
