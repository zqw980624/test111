package com.yami.trading.admin.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class UserAgentListModel extends PageRequest {



    @ApiModelProperty("用户名、UID")
    private  String  userName;


    @ApiModelProperty(" 视图   level  层级视图   list 列表视图  ")
    private  String viewType;
}
