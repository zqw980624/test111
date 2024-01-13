package com.yami.trading.admin.model;

import com.yami.trading.common.domain.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class UserSafewordApplyModel extends PageRequest {

    @ApiModelProperty("账号类型 ")
    private  String  roleName;


    @ApiModelProperty("操作类型 0/修改资金密码；1/取消谷歌绑定；2/取消手机绑定；3/取消邮箱绑定；")
    private String operate;


    @ApiModelProperty("1已申请未审核 ，2 审核通过,3审核未通过")
    private  String status;

    @ApiModelProperty("uuid")
    private  String userCode;

    @ApiModelProperty("用户名")
    private  String userName;
}
