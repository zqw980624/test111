package com.yami.trading.admin.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class UserAuthListModel extends PageRequest {

    @ApiModelProperty("账号类型 ")
    private  String  roleName;


    @ApiModelProperty("证件号码")
    private String idNumber;


    @ApiModelProperty(" 0已申请未审核 ，1.审核中 2 审核通过,3审核未通过")
    private  String status;

    @ApiModelProperty("uuid 用户名")
    private  String userName;
}
