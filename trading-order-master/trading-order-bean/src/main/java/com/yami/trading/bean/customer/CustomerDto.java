package com.yami.trading.bean.customer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel
public class CustomerDto implements Serializable {

    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("用户名")
    private String userName;

    private  String userId;

    /**
     * 在线状态,0:下线，1：在线
     */
    @ApiModelProperty("在线状态,0:下线，1：在线")
    private int onlineState;
    /**
     * 最后一次分配的时间
     */

    @ApiModelProperty("最后一次分配的时间")
    private Date lastCustomerTime;

    /**
     * 最后一次上线的时间
     */
    @ApiModelProperty("最后一次上线的时间")
    private Date lastOnlineTime;

    @ApiModelProperty("备注")
    private String remarks;

    @ApiModelProperty("状态 1 正常 0 无效")
    private String status;


    /**
     * 谷歌验证器是否绑定
     */

    @ApiModelProperty("谷歌验证器是否绑定")
    private boolean googleAuthBind=false ;

    @ApiModelProperty("登录权限")
    private  boolean loginAuthority;
    /**
     * 自动回复语句
     */
    @ApiModelProperty("自动回复语句")
    private String autoAnswer;
}
