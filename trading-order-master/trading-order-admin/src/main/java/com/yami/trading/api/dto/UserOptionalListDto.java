package com.yami.trading.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 用户自选返回数据结构
 */
@Data
public class UserOptionalListDto {

    @ApiModelProperty("分组列表")
    private List<OptionalListCount> list;

    @ApiModelProperty("每个币对类别对应的数量，其中all为所有")
    public Map<String, Integer> count;
}
