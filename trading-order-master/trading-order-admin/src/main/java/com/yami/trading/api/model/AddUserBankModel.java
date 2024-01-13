package com.yami.trading.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
@Data
@ApiModel
public class AddUserBankModel {

    @ApiModelProperty("更新传id 新增不传")
    private String UUID;

    /**
     * 真实姓名
     */
    @NotBlank()
    @ApiModelProperty("真实姓名")
    private String userName;

    @NotBlank()
    @ApiModelProperty("银行卡名称")
    private String bankName;

    /**
     * bank_no
     */
    @NotBlank()
    @ApiModelProperty("bankNo")
    private String bankNo;

    /**
     * bank_address
     */
    @ApiModelProperty("bankAddress")
    private String bankAddress;

    @ApiModelProperty("bankImg")
    private String bankImg;

    /**
     * bank_address
     */
    @ApiModelProperty("bankPhone")
    private String bankPhone;

    private String methodName;
}


