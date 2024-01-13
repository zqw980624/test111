package com.yami.trading.bean.chat.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author lucas
 */
@Data
@ApiModel("消息发送请求体")
public class ChatSendReqModel {

    @NotNull
    @ApiModelProperty("发送时间")
    private String sendTimeStamp;

    @NotBlank
    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("类型")
    private String type;

    @NotBlank
    @ApiModelProperty("partyId")
    private String partyId;
}
