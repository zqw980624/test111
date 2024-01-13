package com.yami.trading.admin.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel
public class MoneylogListModel extends PageRequest {

    @ApiModelProperty("用户id")
    private  String  userCode;


    @ApiModelProperty("账号类型")
    private String rolename;
    @ApiModelProperty("uuid  用户名")
    private String userName;

    @ApiModelProperty("创建时间开始时间 2023-03-22 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @ApiModelProperty("创建时间结束时间 2023-03-22 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @ApiModelProperty("日志(关键字查询)")
    private  String log;

    @ApiModelProperty("类型")
    private  String category;

    //private String title;
}
