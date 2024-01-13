package com.yami.trading.admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel
@Data
public class GoogleAuthDto {


    @ApiModelProperty("密钥")
    private  String googleAuthSecret;

    @ApiModelProperty("图片")
    private  String googleAuthImg;
}
