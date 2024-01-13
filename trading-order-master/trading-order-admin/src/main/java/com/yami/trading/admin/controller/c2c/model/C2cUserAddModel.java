package com.yami.trading.admin.controller.c2c.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class C2cUserAddModel {
    @ApiModelProperty("C2C管理员")
    private String c2c_manager_party_id;
    @ApiModelProperty("1  手机 2.邮箱")
    private String type_front;
    @ApiModelProperty("前端登录用户名")
    private String username_front;
    @ApiModelProperty("前端登录密码")
    private String password_front;
    @ApiModelProperty("前端登录密码")
    private String re_password_front;

    @ApiModelProperty("推荐码")
    private String usercode_front;
    @ApiModelProperty("承兑商类型")
    private String c2c_user_type;
    @ApiModelProperty("承兑商uuid")
    private String c2c_user_party_code;
    @ApiModelProperty("承兑商昵称")
    private String nick_name;
    @ApiModelProperty("承兑商头像")
    private String head_img;
    @ApiModelProperty("30日成单数基础设置")
    private String thirty_days_order_base;
    @ApiModelProperty("30日成单数统计值")
    private String thirty_days_order;
    @ApiModelProperty("30日成单率基础设置")
    private String thirty_days_order_ratio_base;
    @ApiModelProperty("30日成单率统计值")
    private String thirty_days_order_ratio;
    @ApiModelProperty("30日平均放行时间基础设置")
    private String thirty_days_pass_average_time_base;
    @ApiModelProperty("30日平均放行时间统计值")
    private String thirty_days_pass_average_time;
    @ApiModelProperty("30日平均付款时间基础设置")
    private String thirty_days_pay_average_time_base;
    @ApiModelProperty("30日平均付款时间统计值")
    private String thirty_days_pay_average_time;
    @ApiModelProperty("30日交易量基础设置")
    private String thirty_days_amount_base;
    @ApiModelProperty("30日交易量统计值")
    private String thirty_days_amount;
    @ApiModelProperty("买交易量基础设置")
    private String buy_amount_base;
    @ApiModelProperty("买交易量统计值")
    private String buy_amount;
    @ApiModelProperty("卖交易量基础设置")
    private String sell_amount_base;
    @ApiModelProperty("卖交易量统计值")
    private String sell_amount;
    @ApiModelProperty("总交易量基础设置")
    private String total_amount_base;
    @ApiModelProperty("总交易量统计值")
    private String total_amount;
    @ApiModelProperty("账号创建天数基础设置")
    private String account_create_days_base;
    @ApiModelProperty("账号创建天数统计值")
    private String account_create_days;
    @ApiModelProperty("首次交易至今天数基础设置")
    private String first_exchange_days;
    @ApiModelProperty("首次交易至今天数统计值")
    private String first_exchange_days_base;
    @ApiModelProperty("交易人数基础设置")
    private String exchange_users_base;
    @ApiModelProperty("交易人数统计值")
    private String exchange_users;
    @ApiModelProperty("买成单数基础设置")
    private String buy_success_orders_base;
    @ApiModelProperty("买成单数统计值")
    private String buy_success_orders;
    @ApiModelProperty("卖成单数基础设置")
    private String sell_success_orders_base;
    @ApiModelProperty("卖成单数统计值")
    private String sell_success_orders;
    @ApiModelProperty("总成单数基础设置")
    private String total_success_orders_base;
    @ApiModelProperty("总成单数统计值")
    private String total_success_orders;
    @ApiModelProperty("好评数基础设置")
    private String appraise_good_base;
    @ApiModelProperty("好评数统计值")
    private String appraise_good;
    @ApiModelProperty("差评数基础设置")
    private String appraise_bad_base;
    @ApiModelProperty("差评数统计值")
    private String appraise_bad;
    @ApiModelProperty("手机验证状态基础设置")
    private String phone_authority_base;
    @ApiModelProperty("手机验证状态统计值")
    private String phone_authority;
    @ApiModelProperty("邮箱验证状态基础设置")
    private String email_authority_base;
    @ApiModelProperty("邮箱验证状态统计值")
    private String email_authority;
    @ApiModelProperty("身份认证状态基础设置")
    private String kyc_authority_base;
    @ApiModelProperty("身份认证状态统计值")
    private String kyc_authority;
    @ApiModelProperty("高级认证状态基础设置")
    private String kyc_highlevel_authority_base;
    @ApiModelProperty("高级认证状态统计值")
    private String kyc_highlevel_authority;
    @ApiModelProperty("备注")
    private String remark;
    @ApiModelProperty("登录人密码")
    private String login_safeword;
}
