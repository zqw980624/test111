package com.yami.trading.admin.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class UserDataListModel extends PageRequest {

    @ApiModelProperty("账号类型 ")
    private  String  rolename;


    @ApiModelProperty("用户名")
    private  String  userName;


    @ApiModelProperty("userCode")
    private  String  userCode;
    @ApiModelProperty("最后登录IP")

    private  String lastIp;


}
