package com.yami.trading.admin.model.c2c;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel
public class C2cPaymentMethodListModel extends PageRequest {
    @ApiModelProperty("userCode  user name")
    private String userCode;
    @ApiModelProperty("支付方式类型")
    private String methodType;
    @ApiModelProperty("支付方式名称")
    private String methodName;
}
