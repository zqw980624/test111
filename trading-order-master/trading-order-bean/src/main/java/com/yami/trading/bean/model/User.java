/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */

package com.yami.trading.bean.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yami.trading.common.serializer.json.EmojiJsonSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@TableName("tz_user")
public class User implements Serializable {
    private static final long serialVersionUID = 2090714647038636896L;
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 真实姓名
     */

    private String realName;

    /**
     * 用户邮箱
     */

    private String userMail;
    /**
     * 邮箱绑定
     */
    private boolean mailBind=false;


    /**
     * 登录密码
     */
    /**
     * 密码
     */
   // @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String loginPassword;

    /**
     * 资金密码
     */

    private String safePassword;

    /**
     * 手机号码
     */

    private String userMobile;
    /**
     * 手机绑定
     */
    private boolean userMobileBind=false;

    /**
     * 承兑商类型：0不是承兑商/1后台承兑商/2用户承兑商
     */
    private int c2cUserType;


    /**
     * 修改时间
     */

    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    /**
     * 注册时间
     */

    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 注册IP
     */

    private String userRegip;

    /**
     * 最后登录时间
     */

    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date userLasttime;

    /**
     * 最后登录IP
     */

    private String userLastip;




    /**
     * 谷歌验证器
     */
    private String googleAuthSecret;
    /**
     * 谷歌验证器是否绑定
     */
    private boolean googleAuthBind=false ;

    /**


     */
    private String roleName;


    /**
     * 状态 1 正常 0 无效
     */
    private Integer status;


    /**
     * 用户code-UID
     */
    private String userCode;
    /**
     * 推荐人
     */
    private  String userRecom;
    /**
     * 实名认证
     */
    private boolean realNameAuthority = false;
    /**
     * 高级认证
     */
    private boolean highlevelAuthority = false;



    /**
     * 会员等级 默认1
     *
     * 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
     * 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
     * 如：级别11表示：新注册的前端显示为VIP1；
     */
    private int userLevel;



    /**
     * 当日提现限制金额
     */
    private BigDecimal withdrawLimitAmount;

    /**
     * 当前可用提现流水 WITHDRAW_LIMIT_NOW_AMOUNT
     */
    private BigDecimal withdrawLimitNowAmount;
    /**
     * 提现权限
     */
    private boolean withdrawAuthority;
    /**
     * 备注
     */
    private String remarks;
    /**
     *  推荐code
     */
    private  String recomCode;

    private  boolean enabled=true;

    @ApiModelProperty("登录权限")
    private  boolean loginAuthority;

//    private String balanceIncomeConfig;
}
