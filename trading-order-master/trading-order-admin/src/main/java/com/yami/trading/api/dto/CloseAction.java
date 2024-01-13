package com.yami.trading.api.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;


@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CloseAction {

    @NotBlank
    private String symbol;
    /**
     * direction "buy":多 "sell":空
     */
    @Pattern(regexp="^(buy|sell)$",message = "请输入正确的方向")
    private String direction;
    /**
     * amount 委托数量(张)
     */
    @NotNull(message = "委托数量(张)必填")
    @DecimalMin(value = "0.00000001", message = "委托数量(张)不能小于0")
    private BigDecimal amount;


    /**
     * price 交易价格
     */
    @NotNull(message = "交易价格必填")
    @DecimalMin(value = "0.00000001", message = "交易价格不能小于0")
    private BigDecimal price;


    /**
     *price_type 订单报价类型："limit":限价 "opponent":对手价（市价）
     */
    @NotNull
    @Pattern(regexp="^(limit|opponent)$",message = "请输入订单报价类型")
    private String price_type;
}
