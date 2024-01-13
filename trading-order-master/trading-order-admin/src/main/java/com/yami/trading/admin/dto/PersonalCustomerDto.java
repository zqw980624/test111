package com.yami.trading.admin.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PersonalCustomerDto {
    @ApiModelProperty("注释了，没啥用")
    private Boolean off_to_online;
    @ApiModelProperty("用户名")
    private String username;
    @ApiModelProperty("最后上线时间")
    private String last_online_time;
    @ApiModelProperty("最后下线时间")
    private String last_offline_time;
    @ApiModelProperty("首次访问自动回复")
    private String auto_answer;
    @ApiModelProperty("当前在线状态，1是在线，其他是离线")
    private Integer online_state;

}
