package com.yami.trading.sys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@ApiModel
public class SysUserInfoDto  implements Serializable {

    /**
     * 用户ID
     *
     */
    private Long userId;

    /**
     * 用户名
     */
    @ApiModelProperty("用户名")
    private String username;


    /**
     * 邮箱
     */
    @ApiModelProperty("邮箱")
    private String email;



    /**
     * 状态  0：禁用   1：正常
     */
    @ApiModelProperty("状态  0：禁用   1：正常")
    private Integer status;


    /**
     * 角色ID列表
     */
    @ApiModelProperty("角色ID列表")
    private List<Long> roleIdList;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private Date createTime;


    /**
     * 谷歌验证器是否绑定
     */
    @ApiModelProperty("谷歌验证器是否绑定")
    private boolean googleAuthBind=false ;

    /**
     * 更新日期
     */
    private Date updateTime;


    @ApiModelProperty("备注")
    private String remarks;

    private List<String> roleName;
}
