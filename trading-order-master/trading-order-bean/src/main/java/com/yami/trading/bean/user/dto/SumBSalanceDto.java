package com.yami.trading.bean.user.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel
public class SumBSalanceDto {

  private   BigDecimal dateamount;

    private BigDecimal sumamount;
}
