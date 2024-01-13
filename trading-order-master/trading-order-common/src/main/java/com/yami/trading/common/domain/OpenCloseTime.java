package com.yami.trading.common.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class OpenCloseTime {
    /**
     * 开盘时间，北京时间
     */
    @ApiModelProperty("开盘时间，北京时间")
    private Date openBjDate;
    /**
     * 停盘时间，北京时间
     */
    @ApiModelProperty("停盘时间，北京时间")
    private Date closeBjDate;
    /**
     * 开盘时间戳
     */
    @ApiModelProperty("开盘时间戳")
    private Long openTs;
    /**
     * 停盘时间戳
     */
    @ApiModelProperty("停盘时间戳")
    private Long closeTs;
}
