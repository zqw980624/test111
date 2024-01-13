package com.yami.trading.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
@ApiModel
public class FutureOpenAction {

    @NotBlank
    @ApiModelProperty("币对")
    private String symbol;
    /**
     * direction "buy":多 "sell":空
     */
    @Pattern(regexp="^(buy|sell)$",message = "请输入正确的方向")
    @ApiModelProperty("方向")
    private String direction;
    /**
     * amount 委托数量(张)
     */
    @NotNull(message = "金额")
    @DecimalMin(value = "0.00000001", message = "金额小于0")
    @ApiModelProperty("金额")
    private BigDecimal amount;

    @NotNull(message = "交割参数不能为空")
    @ApiModelProperty("交割参数")
    private String para_id;

    @NotNull(message = "token")
    private String session_token;
}
