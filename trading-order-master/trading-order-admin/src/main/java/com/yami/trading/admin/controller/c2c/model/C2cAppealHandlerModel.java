
package com.yami.trading.admin.controller.c2c.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class C2cAppealHandlerModel extends PageRequest {

    @ApiModelProperty("订单号")
    private  String orderNo;


}
