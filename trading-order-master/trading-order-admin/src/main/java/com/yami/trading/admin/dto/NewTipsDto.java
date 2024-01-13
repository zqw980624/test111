package com.yami.trading.admin.dto;

import com.yami.trading.common.constants.TipConstants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class NewTipsDto {

    @ApiModelProperty("通知模块")
    private  String tipDomName;

    @ApiModelProperty("条数")
    private String tipContentSum;

    @ApiModelProperty("提示消息")
    private String tipMessage;

    @ApiModelProperty("请求url")
    private String tipUrl;

    @ApiModelProperty("是否右下角提示")
    private String tipShow;
    
}
