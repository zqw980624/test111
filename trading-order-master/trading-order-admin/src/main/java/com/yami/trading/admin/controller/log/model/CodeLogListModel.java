package com.yami.trading.admin.controller.log.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class CodeLogListModel extends PageRequest {

    @ApiModelProperty("手机号或邮箱号")
    private  String  target;
}
