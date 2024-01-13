package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class LoginModel {

    /**
     * 用户名
     */
    @NotBlank(message = "userName不能为空")
    @ApiModelProperty(value = "用户名/邮箱/手机号", required = true)
    protected String userName;

    /**
     * 密码
     */
    @NotBlank(message = "passWord不能为空")
    @ApiModelProperty(value = "一般用作密码", required = true)
    protected String passWord;


    @ApiModelProperty(value = "谷歌验证码", required = true)
    @Min(1)
    private  int  googleAuthCode;
}
