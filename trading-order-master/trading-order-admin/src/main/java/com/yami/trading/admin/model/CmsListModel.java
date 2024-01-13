package com.yami.trading.admin.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class CmsListModel  extends PageRequest {

    @ApiModelProperty("语言")
    private String language;

    @ApiModelProperty("业务代码， 同种内容 不同语言下的code相同")
    private  String contentCode;

    @ApiModelProperty("标题")
    private  String title;
}
