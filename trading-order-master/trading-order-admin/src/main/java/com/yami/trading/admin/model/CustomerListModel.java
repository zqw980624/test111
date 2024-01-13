package com.yami.trading.admin.model;
import com.yami.trading.common.domain.PageRequest;
import com.yami.trading.common.domain.PageResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class CustomerListModel  extends PageRequest {

    @ApiModelProperty("userName")
    private  String userName;
}
