package com.yami.trading.sys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@ApiModel
public class SysRoleListDto {

    /**
     * 角色名称
     */
    @NotBlank(message="角色名称不能为空")
    private String roleName;

    /**
     * 备注
     */
    private String remark;

    @ApiModelProperty("权限")
    private String menuIdStr;

    private Date createTime;
}
