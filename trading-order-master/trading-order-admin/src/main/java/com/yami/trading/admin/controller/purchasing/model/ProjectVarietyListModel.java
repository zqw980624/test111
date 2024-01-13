package com.yami.trading.admin.controller.purchasing.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class ProjectVarietyListModel  extends PageRequest {

    @ApiModelProperty("项目名称")
    private  String projectName;
}
