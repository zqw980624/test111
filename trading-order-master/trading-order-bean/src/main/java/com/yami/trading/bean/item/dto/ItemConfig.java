package com.yami.trading.bean.item.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class ItemConfig {
    private String uuid;
    private static final long serialVersionUID = 1L;

    /**
     * 币种名称
     */
    @NotBlank(message = "名称不能为空")
    private String name;
    /**
     * 代码
     */
    @NotBlank(message = "代码不能为空")
    private String symbol;
    /**
     * 数据源编码
     */
    private String symbolData;
    /**
     * 小数位精度
     */
    @NotNull
    @Min(value = 0, message = "精度必须大于等于0")
    private Integer decimals;
    /**
     * 交易量倍数
     */
    @NotNull(message = "交易量放大倍数必填")
    @DecimalMin(value = "0", message = "交易量放大倍数不能小于0")
    private Double multiple;
    /**
     * 借贷利率
     */
    private Double borrowingRate;
//    @NotBlank
//    private String loginSafeword;


}
