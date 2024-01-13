package com.yami.trading.admin.controller.purchasing.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class UserPurchasingRecordModel  extends PageRequest {

    @ApiModelProperty("账号类型")
    private String rolename;
    @ApiModelProperty("uuid  用户名")
    private String userName;

}
