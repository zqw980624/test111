package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class ChangeAddressModel {

    @NotBlank
    private  String id;

    @ApiModelProperty("修改后用户新提现地址")
    //  @NotBlank
    private String changeAfterAddress;


    // @NotBlank
    @ApiModelProperty("资金密码")
    private  String safeWord;

    private  String names;
    private  String address;
    private  String account;
    private  String bank;
}
