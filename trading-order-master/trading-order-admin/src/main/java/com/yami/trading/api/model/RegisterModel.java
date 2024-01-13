package com.yami.trading.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class RegisterModel {

    @ApiModelProperty("用户名")
   // @NotBlank()
    private  String userName;

    private  String userMobile;

    private  String mobileCode;

    @ApiModelProperty("密码")
    @NotBlank
    private  String password;

    @ApiModelProperty("注册类型：1手机；2邮箱；3用户名；")
    private  int type;

    @ApiModelProperty("邀请码")
    private  String userCode;
}
