package com.yami.trading.admin.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class NewsListModel extends PageRequest {



    @ApiModelProperty("标题")
    private  String title;



    @ApiModelProperty("用户名 uuid")
    private  String userCode;

    @ApiModelProperty("语言")
    private String language;
}
