package com.yami.trading.bean.future.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

/**
 * 交割合约DTO
 *
 * @author lucas
 * @version 2023-04-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProfitLossConfigUpdate {
    @NotEmpty
    private String uuid;
    @NotEmpty
    private String type;
    @NotEmpty
    private String remark;


}
