package com.yami.trading.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class OpenAction {

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
     * lever_rate 杠杆倍数
     */
    @JsonProperty("lever_rate")
    private  BigDecimal lever_rate;

    /**
     * price 交易价格
     */
    @NotNull(message = "交易价格必填")
    @DecimalMin(value = "0.00000001", message = "交易价格不能小于0")
    private BigDecimal price;

    /**
     * stop_price_profit 止盈触发价格
     */
    @JsonProperty("stop_price_profit")
    private BigDecimal stop_price_profit;
    /**
     * stop_price_loss 止损触发价格
     */
    @JsonProperty("stop_price_loss")
    private BigDecimal stop_price_loss;
    /**
     *price_type 订单报价类型："limit":限价 "opponent":对手价（市价）
     */
    @NotNull
    @JsonProperty("price_type")
    @Pattern(regexp="^(limit|opponent)$",message = "请输入订单报价类型")
    private String price_type;


    public void setLever_rate(BigDecimal lever_rate) {
        if(lever_rate == null){
            this.lever_rate =  BigDecimal.ONE;
            return;
        }
        this.lever_rate = lever_rate;
    }

    public void setStop_price_profit(BigDecimal stop_price_profit) {
        if(stop_price_profit == null){
            this.stop_price_profit = BigDecimal.ZERO;
            return;
        }
        this.stop_price_profit = stop_price_profit;
    }

    public void setStop_price_loss(BigDecimal stop_price_loss) {
        if(stop_price_loss == null){
            this.stop_price_loss = BigDecimal.ZERO;
            return;
        }
        this.stop_price_loss = stop_price_loss;
    }
}
