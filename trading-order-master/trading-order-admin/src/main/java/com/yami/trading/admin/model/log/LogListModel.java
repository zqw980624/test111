package com.yami.trading.admin.model.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel
public class LogListModel  extends PageRequest {





    @ApiModelProperty("账号类型")
    private String roleName;
    @ApiModelProperty("uuid  用户名")
    private String userName;



    @ApiModelProperty("日志(关键字查询)")
    private  String log;

    @ApiModelProperty("类型")
    private  String category;
    @ApiModelProperty("操作人")
    private  String operator;

}
