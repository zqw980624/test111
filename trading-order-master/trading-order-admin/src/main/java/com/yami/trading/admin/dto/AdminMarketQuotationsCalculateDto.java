package com.yami.trading.admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@ApiModel
@Data
public class AdminMarketQuotationsCalculateDto {
    @NotEmpty
    @ApiModelProperty("币对")
    private String symbol;
    @NotEmpty
    @ApiModelProperty("0增加一个pips 1减少一个pips 2直接修改调整值")
    @Pattern(regexp = "^(1|2|0)$") String type;

    @ApiModelProperty("调整值")
    private Double value;
}
