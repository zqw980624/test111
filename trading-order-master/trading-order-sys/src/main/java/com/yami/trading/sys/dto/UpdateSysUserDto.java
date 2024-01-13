package com.yami.trading.sys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@ApiModel
public class UpdateSysUserDto {

    private long id;



    /**
     * 邮箱
     */
    @NotBlank(message="邮箱不能为空")
    @Email(message="邮箱格式不正确")
    @ApiModelProperty("邮箱")
    private String email;


    @ApiModelProperty("状态  0：禁用   1：正常")
    @Min(0)
    @Min(1)
    private int status;

    /**
     * 角色ID列表
     */

    @ApiModelProperty("角色ID列表")
    private List<Long> roleIdList;


    private  String mobile;
    @ApiModelProperty("备注")
    private String remarks;


}
