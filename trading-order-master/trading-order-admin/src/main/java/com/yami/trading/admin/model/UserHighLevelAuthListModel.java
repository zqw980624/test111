package com.yami.trading.admin.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class UserHighLevelAuthListModel extends PageRequest {

    @ApiModelProperty("账号类型 ")
    private  String  roleName;





    @ApiModelProperty("0已申请未审核 ，1 审核通过,2审核未通过  不传全部")
    private  String status;

    @ApiModelProperty("uuid")
    private  String userName;
}