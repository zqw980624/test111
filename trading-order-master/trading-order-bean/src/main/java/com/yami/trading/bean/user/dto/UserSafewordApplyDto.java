package com.yami.trading.bean.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel
public class UserSafewordApplyDto  implements Serializable {
    private String uuid;
    @ApiModelProperty("用户")
    private String userName;
    @ApiModelProperty("UID")
    private String userCode;
    @ApiModelProperty("账户类型")
    private String roleName;
    @ApiModelProperty("证件正面照")
    private String idcardPathFront;
    /**
     * 证件背面照
     */
    @ApiModelProperty("证件背面照")
    private String idcardPathBack;
    /**
     * 正面手持证件照
     */
    @ApiModelProperty("正面手持证件照")
    private String idcardPathHold;
    /**
     * 1审核中 ，2 审核通过,3审核未通过
     */
    @ApiModelProperty("1审核中 ，2 审核通过,3审核未通过")
    private int status;
    /**
     * 审核消息，未通过原因
     */
    @ApiModelProperty("审核消息，未通过原因")
    private String msg;
    /**
     * 审核时间
     */
    @ApiModelProperty("审核时间")
    private Date applyTime;
    /**
     * 操作类型 0/修改资金密码；1/取消谷歌绑定；2/取消手机绑定；3/取消邮箱绑定；
     */
    @ApiModelProperty("操作类型 0/修改资金密码；1/取消谷歌绑定；2/取消手机绑定；3/取消邮箱绑定；")
    private int operate;
    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;
    @ApiModelProperty("申请时间")
    private Date createTime;
    /**
     * 用户等级
     */
    private int userLevel;


    /**
     * 真实姓名
     */

    @ApiModelProperty("实名姓名")
    private String realName;
    /**
     * 实名认证
     */
    @ApiModelProperty("实名认证状态")
    private boolean realNameAuthority = false;
}
