package com.yami.trading.admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

@ApiModel
@Data
public class AdminMarketQuotationsUpdateDto {
    @NotEmpty
    @ApiModelProperty("币对")
    private String symbol;
    @ApiModelProperty("延迟秒")
    private Double second;
    @NotNull(message = "调整值必填")
    @ApiModelProperty("调整值")
    private BigDecimal value;
}
