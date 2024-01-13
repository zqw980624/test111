package com.yami.trading.admin.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yami.trading.common.domain.PageRequest;
import com.yami.trading.common.domain.PageResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@ApiModel
public class UserStatisticsModel extends PageRequest {

    @ApiModelProperty("开始时间  2023-03-22 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @ApiModelProperty("结束时间  2023-03-22 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;


    @ApiModelProperty("uuid  用户名")
    private String user_name;

    private  String userId;
}
