package com.yami.trading.bean.vo;

import com.yami.trading.common.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel
public class WithdrawFeeVo {

    @ApiModelProperty("手续费")
    private BigDecimal fee;


    @ApiModelProperty("实际到账")
    private String volumeLast;

    private  String withdrawFeeType;
}
