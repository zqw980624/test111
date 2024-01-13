package com.yami.trading.api.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@ApiModel
public class C2cOrderListModel  extends PageRequest {

    @ApiModelProperty("类型   1充值 2 提现")
    @Min(1)
    @Max(2)
    private  int  type;
}
