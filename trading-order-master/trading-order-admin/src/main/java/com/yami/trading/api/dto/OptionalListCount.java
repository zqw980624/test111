package com.yami.trading.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
@Data
public class OptionalListCount {
    @ApiModelProperty("分组id，自选的为0")
    private String listId;

    @ApiModelProperty("分组名称")
    private String listName;
    @ApiModelProperty("法币币种")
    private String currency;
    @ApiModelProperty("币数量")
    private Integer symbolCount;


}
