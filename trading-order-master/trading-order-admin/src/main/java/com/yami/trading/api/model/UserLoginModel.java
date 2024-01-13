package com.yami.trading.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class UserLoginModel {

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名/邮箱/手机号", required = true)
    protected String userName;

    /**
     * 密码
     */
    @NotBlank(message = "passWord不能为空")
    @ApiModelProperty(value = "一般用作密码", required = true)
    protected String passWord;


    @ApiModelProperty("1手机；2邮箱；3用户名；")
    private  int type;

}
