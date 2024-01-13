package com.yami.trading.bean.future.dto;

import com.yami.trading.common.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
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
public class ProfitLossConfigAdd {

    @NotEmpty(message = "请输入用户UID")
    @ApiModelProperty("用户uid")
    private String userCode;
    @NotEmpty
    private String type;
    private String remark;


}
