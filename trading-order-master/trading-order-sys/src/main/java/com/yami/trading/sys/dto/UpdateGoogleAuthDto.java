package com.yami.trading.sys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class UpdateGoogleAuthDto {


    private  long id;

    @ApiModelProperty("谷歌密钥")
    @NotBlank
    private String secret;


    @ApiModelProperty("验证码")
    @NotBlank
    private String googleAuthCode;


    @ApiModelProperty("超级谷歌验证码")
    @NotBlank
    private String rootGoogleAuthCode;

}
