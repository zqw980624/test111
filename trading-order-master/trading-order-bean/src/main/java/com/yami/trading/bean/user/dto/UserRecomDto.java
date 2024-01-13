package com.yami.trading.bean.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel
public class UserRecomDto  implements Serializable {


    private  String uuid;

    @ApiModelProperty("用户名")
    private  String userName;


    @ApiModelProperty("UID")
    private String userCode;

    @ApiModelProperty("账户类型")
    private  String roleName;

    @ApiModelProperty("推荐用户")
    private  String recomUserName;

    @ApiModelProperty("推荐用户code")
    private  String recomUserCode;

    private  String roleNameDesc;

}
