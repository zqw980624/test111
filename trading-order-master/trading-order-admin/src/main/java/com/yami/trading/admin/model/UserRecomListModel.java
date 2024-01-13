package com.yami.trading.admin.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class UserRecomListModel extends PageRequest {


    @ApiModelProperty("用户名")
    private  String userName;

    @ApiModelProperty("推荐用户")
    private  String recomUserName;
}
