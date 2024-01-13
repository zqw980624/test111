package com.yami.trading.bean.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel
public class RechargeBlockchainOrderDto  implements Serializable {
    private  String uuid;
    private  Date createTime;

    @ApiModelProperty("用户")
    private String userName;

    @ApiModelProperty("UID")
    private String userCode;

    @ApiModelProperty("账户类型")
    private String roleName;

    @ApiModelProperty("推荐人")
    private  String recomUserName;
    private  String deflag;
    /**
     * 订单号
     */
    @ApiModelProperty("订单号")
    private String orderNo;

    private String userId;

    /**
     * 充值数量
     */
    @ApiModelProperty("充值数量")
    private Double channelAmount;

    /**
     * 充值币种
     */
    @ApiModelProperty("充值币种")
    private String coin;

    /**
     * 充值状态 0 初始状态，未知或处理中 1 成功 2 失败
     */
    @ApiModelProperty("充值状态 0 初始状态，未知或处理中 1 成功 2 失败")
    private int status = 0;

    /**
     * 审核操作时间
     */
    @ApiModelProperty("审核操作时间")
    private Date reviewTime;

    /**
     * 备注说明，管理员操作
     */
    @ApiModelProperty("备注说明，管理员操作")
    private String description;

    /**
     * 区块链充值地址
     */
    @ApiModelProperty("区块链充值地址")
    private String blockchainName;
    /**
     * 已充值的上传图片
     */
    @ApiModelProperty("已充值的上传图片")
    private String img;

    /**
     * 客户自己的区块链地址
     */
    @ApiModelProperty("客户自己的区块链地址")
    private String address;
    /**
     * 通道充值地址
     */
    @ApiModelProperty("通道充值地址")
    private String channelAddress;

}
