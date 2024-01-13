package com.yami.trading.bean.chat.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author lucas
 */
@Data
@ApiModel("设置用户备注请求参数")
public class ChatResetRemarkReqModel {

    @ApiModelProperty("备注")
    private String remarks;

    @NotBlank
    @ApiModelProperty("partyId")
    private String partyId;
}
