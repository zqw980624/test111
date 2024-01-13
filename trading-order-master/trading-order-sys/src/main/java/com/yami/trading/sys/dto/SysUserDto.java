package com.yami.trading.sys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;


@Data
@ApiModel
public class SysUserDto {

    /**
     * 用户名
     */
    @NotBlank(message="用户名不能为空")
    @Size(min = 2,max = 20,message = "用户名长度要在2-20之间")
    @ApiModelProperty("用户名")
    private String username;
    /**
     * 邮箱
     */
    @NotBlank(message="邮箱不能为空")
    @Email(message="邮箱格式不正确")
    @ApiModelProperty("邮箱")
    private String email;


    /**
     * 密码
     */
    @ApiModelProperty("密码")
    @NotBlank
    private String password;



    @ApiModelProperty("状态  0：禁用   1：正常")
    @Min(0)
    @Min(1)
    private int status;

    /**
     * 角色ID列表
     */

    @ApiModelProperty("角色ID列表")
    private List<Long> roleIdList;


    /**
     * 资金密码
     */
    @ApiModelProperty("资金密码")
    @NotBlank
    private   String safePassword;


    @ApiModelProperty("备注")
    private String remarks;


    private  String mobile;


}
