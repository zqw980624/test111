package com.yami.trading.bean.chat.query.query;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lucas
 */
@Data
@ApiModel("聊天记录列表返回体")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatRecordRespModel {


    @ApiModelProperty("消息uuid")
    private String id;

    @ApiModelProperty("send 发送 receive 接收，// 1 用户发送 2 用户接收")
    private String sendReceive;

    @ApiModelProperty("内容类型")
    private String contentType;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("创建时间")
    private String createtime;

    @ApiModelProperty("创建时间戳")
    private Long  createtimeTs;
    @ApiModelProperty("标记删除，-1:删除，0:正常")
    private Integer deleteStatus;
}
