package com.yami.trading.bean.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class FileInfoVo {


    @ApiModelProperty("path")
    private  String path;

    @ApiModelProperty("httpurl")
    private  String httpUrl;
}
