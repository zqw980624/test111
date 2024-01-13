package com.yami.trading.bean.syspara.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 系统参数全量显示和吸怪的dto
 */
@Data
@ApiModel("系统参数全量显示对象")
public class SysparasDto {
    @ApiModelProperty("系统版本")
    private String sys_version;
    /**
     * admin修改参数 在线客服URL
     */
    @ApiModelProperty("在线客服URL")
    private String customer_service_url;
    /**
     * 客服系统游客聊天黑名单，对应的ip无法发送，多个用逗号隔开，例如:139.180.221.214,127.0.0.2
     */
    @ApiModelProperty("客服系统游客聊天黑名单")
    private String online_visitor_black_ip_menu;
    /**
     * 客服系统用户名黑名单，对多个用户名用逗号隔开，例如:aaa,bbb,ccc
     */
    @ApiModelProperty("客服系统用户名黑名单")
    private String online_username_black_menu;

    /**
     * admin修改参数 币币交易买入手续费
     */
    @ApiModelProperty("币币交易买入手续费")
    private String exchange_apply_order_buy_fee;
    /**
     * admin修改参数 交割合约24小时内赢率
     */
    @ApiModelProperty("交割合约24小时内赢率")
    private String futures_most_prfit_level;
    /**
     * admin修改参数 币币交易卖出手续费
     */
    @ApiModelProperty("币币交易卖出手续费")
    private String exchange_apply_order_sell_fee;
    /**
     * admin修改参数 订单交易状态(false不可下单)
     */
    @ApiModelProperty("订单交易状态(false不可下单)")
    private String order_open;
    /**
     * 提现差额是否开启
     */
    @ApiModelProperty("提现差额是否开启")
    private String withdraw_limit_open;
    /**
     * admin修改参数 提现最低金额
     */
    @ApiModelProperty("提现最低金额")
    private String withdraw_limit;
    /**
     * 每日可提现次数
     */
    @ApiModelProperty("提现最低金额")
    private String withdraw_limit_num;
    /**
     * 每日可提现时间段
     */
    @ApiModelProperty("提现最低金额")
    private String withdraw_limit_time;
    /**
     * 最低充值金额(USDT)
     */
    @ApiModelProperty("最低充值金额(USDT)")
    private String recharge_limit_min;
    /**
     * 最高充值金额（USDT）
     */
    @ApiModelProperty("最高充值金额（USDT）")
    private String recharge_limit_max;
    /**
     * 是否开启基础认证后才能进行提现操作(false不可操作)
     */
    @ApiModelProperty("是否开启基础认证后才能进行提现操作(false不可操作)")
    private String withdraw_by_kyc;
    /**
     * 单次最高提现金额
     */
    @ApiModelProperty("单次最高提现金额")
    private String withdraw_limit_max;
    /**
     * 提现限制流水按百分之几可以提现，1为100%
     */
    @ApiModelProperty("提现限制流水按百分之几可以提现，1为100%")
    private String withdraw_limit_turnover_percent;

    private String filter_ip;
    /**
     * 提现无限限制uid用户（当开启周提现限额时生效）(例如 1,2,3)
     */
    @ApiModelProperty("提现无限限制uid用户（当开启周提现限额时生效）(例如 1,2,3)")
    private String withdraw_week_unlimit_uid;
    /**
     * 最低提现额度(dapp usdt数量)
     */
    @ApiModelProperty("最低提现额度(dapp usdt数量)")
    private String withdraw_limit_dapp;

    /**
     * 官网配置邀请链接
     */
    @ApiModelProperty("官网配置邀请链接")
    private String invite_url;
    /**
     * admin修改参数 币币交易开关(true开启，false关闭)
     */
    @ApiModelProperty("admin修改参数")
    private String exchange_order_open;

    private String withdraw_limit_time_max;
    private String withdraw_limit_time_min;

    private String login_safeword;
    private String super_google_auth_code;

    private String user_uid_sequence;
    private String withdraw_limit_btc;
    private String withdraw_limit_eth;

    //前端用户黑名单：stop_user_internet
    private String stop_user_internet;

    private String agent_uid_sequence;
}
