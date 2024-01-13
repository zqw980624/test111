package com.yami.trading.admin.controller.purchasing.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class ListTradeLeverageModel  extends PageRequest {

    /**
     * 申购id
     */
    @ApiModelProperty("申购id")
    private  String purchasingId;
}
