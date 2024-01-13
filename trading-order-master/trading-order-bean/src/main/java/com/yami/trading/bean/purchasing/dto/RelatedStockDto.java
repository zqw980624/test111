package com.yami.trading.bean.purchasing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RelatedStockDto {
    /**
     * 币种名称
     */
    @NotBlank(message = "名称不能为空")
    @ApiModelProperty("币种名称")
    private String name;
    /**
     * 代码
     */
    @ApiModelProperty("代码")
    @NotBlank(message = "代码不能为空")
    private String symbol;
}
