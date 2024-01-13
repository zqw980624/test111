package com.yami.trading.admin.model.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel
public class DAppLogListModel extends PageRequest {
    @ApiModelProperty("账号类型")
    private String rolename;
    @ApiModelProperty("uuid  用户名")
    private String userName;
    @ApiModelProperty("日志类型 exchange:转换 提币 transfer:转账 挖矿")
    private String action;
    @ApiModelProperty("创建时间开始时间 2023-03-22 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @ApiModelProperty("创建时间结束时间 2023-03-22 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
}
