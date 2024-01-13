package com.yami.trading.admin.controller.c2c;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.controller.c2c.model.*;
import com.yami.trading.admin.model.IdModel;
import com.yami.trading.bean.c2c.C2cUser;
import com.yami.trading.bean.c2c.C2cUserParamBaseSet;
import com.yami.trading.bean.model.Log;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.syspara.domain.Syspara;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.Arith;
import com.yami.trading.common.util.HttpContextUtils;
import com.yami.trading.common.util.IPHelper;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.AwsS3OSSFileService;
import com.yami.trading.service.c2c.C2cUserParamBaseSetService;
import com.yami.trading.service.c2c.C2cUserService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.sys.model.SysRole;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.service.SysRoleService;
import com.yami.trading.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("c2cUser")
@Api(tags = "C2C承兑商管理")
public class C2cUserController {
    @Autowired
    C2cUserService c2cUserService;
    @Autowired
    C2cUserParamBaseSetService c2cUserParamBaseSetService;
    @Autowired
    SysUserService sysUserService;
    @Autowired
    UserService userService;
    @Autowired
    LogService logService;

    @Autowired
    SysRoleService roleService;

    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;

    @Autowired
    SysparaService sysparaService;

    /**
     * 获取 承兑商 列表
     */
    @PostMapping("list")
    public Result list(@RequestBody C2cUserListModel model) {
//            String secUuid = "";
//            if (null != userNameLogin) {
//                SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
//                Set<Role> roles = sec.getRoles();
//                Iterator<Role> it = roles.iterator();
//                while (it.hasNext()) {
//                    Role role = (Role) it.next();
//                    if (role.getRoleName().equals("C2C")) {
//                        secUuid = sec.getId().toString();
//                        break;
//                    }
//                }
//            }
        Page page = new Page(model.getCurrent(), model.getSize());
        c2cUserService.pagedQuery(page, model.getC2cUserId(), model.getC2cUserType(),
                model.getC2cUserPartyId(), model.getC2cManagerName());

        for (Object o:page.getRecords()){
           Map m= (Map) o;
            m.put("head_img", awsS3OSSFileService.getUrl(m.get("head_img").toString()));
        }
        return Result.succeed(page);
    }

    @GetMapping("getAllC2cManager")
    @ApiOperation("获取C2C管理员")
    public Result getAllC2cManager() {
        List<SysUser> list = sysUserService.list();
        Map<Long, SysRole> sysRoleMap = roleService.list(Wrappers.<SysRole>query()
                .lambda().eq(SysRole::getRoleName,"C2C管理员")).stream().collect(Collectors.toMap(SysRole::getRoleId, SysRole -> SysRole));
        List<SysUser>  userList=new ArrayList<>();
        for (SysUser sysUser : list) {
            List<Long> roleIds = roleService.listRoleIdByUserId(sysUser.getUserId());
            roleIds.forEach(rid -> {
                if (sysRoleMap.containsKey(rid)) {
                    userList.add(sysUser);
                }
            });
        }
        List<Map> resultList=new ArrayList<>();
        for (SysUser sysUser:userList){
            Map<String,Object> map=new HashMap<>();
            map.put("id",sysUser.getUserId()+"");
            map.put("userName",sysUser.getUsername());
            resultList.add(map);
        }
        return Result.succeed(resultList);
    }


    public int getInt(String value){
        return  StringUtils.isNotEmpty(value)?Integer.valueOf(value):0;
    }

    /**
     * 新增 承兑商
     */
    @PostMapping("add")
    @ApiOperation("新增 承兑商")
    public Result add( @Valid C2cUserAddModel model) {
        HttpServletRequest request= HttpContextUtils.getHttpServletRequest();
        String c2c_manager_party_id = request.getParameter("c2c_manager_party_id"); //C2C管理员
        String type_front = request.getParameter("type_front"); // 1  手机 2.邮箱
        String username_front = request.getParameter("username_front"); //前端登录用户名
        String password_front = request.getParameter("password_front"); //前端登录密码
        String re_password_front = request.getParameter("re_password_front"); //前端登录密码
        String usercode_front = request.getParameter("usercode_front"); //推荐码
        String c2c_user_type = request.getParameter("c2c_user_type"); // 承兑商类型
        String c2c_user_party_code = request.getParameter("c2c_user_party_code"); //承兑商uuid
        String nick_name = request.getParameter("nick_name");  //承兑商昵称
        String head_img = request.getParameter("head_img");  //承兑商头像
        String deposit = request.getParameter("deposit");
        String deposit_open = request.getParameter("deposit_open");
        String deposit_gift_rate = request.getParameter("deposit_gift_rate");
        //30日成单数
        String thirty_days_order_base = request.getParameter("thirty_days_order_base");//基础设置
        String thirty_days_order = request.getParameter("thirty_days_order");  //30日成单数统计值  thirty_days_order_result最终值

        //30日成单率
        String thirty_days_order_ratio_base = request.getParameter("thirty_days_order_ratio_base"); //基础设置
        String thirty_days_order_ratio = request.getParameter("thirty_days_order_ratio"); //统计值

        //30日平均放行时间
        String thirty_days_pass_average_time_base = request.getParameter("thirty_days_pass_average_time_base"); //基础设置
        String thirty_days_pass_average_time = request.getParameter("thirty_days_pass_average_time"); //统计值

        //30日平均付款时间
        String thirty_days_pay_average_time_base = request.getParameter("thirty_days_pay_average_time_base");
        String thirty_days_pay_average_time = request.getParameter("thirty_days_pay_average_time");

        //30日交易量
        String thirty_days_amount_base = request.getParameter("thirty_days_amount_base");
        String thirty_days_amount = request.getParameter("thirty_days_amount");

        //买交易量
        String buy_amount_base = request.getParameter("buy_amount_base");
        String buy_amount = request.getParameter("buy_amount");


        //卖交易量
        String sell_amount_base = request.getParameter("sell_amount_base");
        String sell_amount = request.getParameter("sell_amount");

        //总交易量
        String total_amount_base = request.getParameter("total_amount_base");
        String total_amount = request.getParameter("total_amount");

        //账号创建天数
        String account_create_days_base = request.getParameter("account_create_days_base");
        String account_create_days = request.getParameter("account_create_days");

        //首次交易至今天数
        String first_exchange_days = request.getParameter("first_exchange_days");
        String first_exchange_days_base = request.getParameter("first_exchange_days_base");

        //交易人数
        String exchange_users_base = request.getParameter("exchange_users_base");
        String exchange_users = request.getParameter("exchange_users");


        //买成单数
        String buy_success_orders_base = request.getParameter("buy_success_orders_base");
        String buy_success_orders = request.getParameter("buy_success_orders");

        //卖成单数
        String sell_success_orders_base = request.getParameter("sell_success_orders_base");
        String sell_success_orders = request.getParameter("sell_success_orders");

        //总成单数
        String total_success_orders = request.getParameter("total_success_orders");
        String total_success_orders_base = request.getParameter("total_success_orders_base");
        //好评数
        String appraise_good_base = request.getParameter("appraise_good_base");
        String appraise_good = request.getParameter("appraise_good");

        //差评数
        String appraise_bad_base = request.getParameter("appraise_bad_base");
        String appraise_bad = request.getParameter("appraise_bad");


        //手机验证状态
        String phone_authority_base = request.getParameter("phone_authority_base");
        String phone_authority = request.getParameter("phone_authority");

        //邮箱验证状态
        String email_authority_base = request.getParameter("email_authority_base");
        String email_authority = request.getParameter("email_authority");

        //身份认证状态
        String kyc_authority = request.getParameter("kyc_authority");
        String kyc_authority_base = request.getParameter("kyc_authority_base");

        //高级认证状态
        String kyc_highlevel_authority = request.getParameter("kyc_highlevel_authority");
        String kyc_highlevel_authority_base = request.getParameter("kyc_highlevel_authority_base");
        String remark = request.getParameter("remark");
        String login_safeword = request.getParameter("login_safeword");



        String order_mail_notice_open = request.getParameter("order_mail_notice_open");
        String order_sms_notice_open = request.getParameter("order_sms_notice_open");
        String order_app_notice_open = request.getParameter("order_app_notice_open");
        String appeal_mail_notice_open = request.getParameter("appeal_mail_notice_open");
        String appeal_sms_notice_open = request.getParameter("appeal_sms_notice_open");
        String appeal_app_notice_open = request.getParameter("appeal_app_notice_open");
        String chat_app_notice_open = request.getParameter("chat_app_notice_open");
        String security_mail_notice_open = request.getParameter("security_mail_notice_open");
        String security_sms_notice_open = request.getParameter("security_sms_notice_open");
        String security_app_notice_open = request.getParameter("security_app_notice_open");
        String order_mail_notice_open_base = request.getParameter("order_mail_notice_open_base");
        String order_sms_notice_open_base = request.getParameter("order_sms_notice_open_base");
        String order_app_notice_open_base = request.getParameter("order_app_notice_open_base");
        String appeal_mail_notice_open_base = request.getParameter("appeal_mail_notice_open_base");
        String appeal_sms_notice_open_base = request.getParameter("appeal_sms_notice_open_base");
        String appeal_app_notice_open_base = request.getParameter("appeal_app_notice_open_base");
        String chat_app_notice_open_base = request.getParameter("chat_app_notice_open_base");
        String security_mail_notice_open_base = request.getParameter("security_mail_notice_open_base");
        String security_sms_notice_open_base = request.getParameter("security_sms_notice_open_base");
        String security_app_notice_open_base = request.getParameter("security_app_notice_open_base");
        if (StringUtils.isEmptyString(deposit)) {
            deposit = "0";
        }
        if (StringUtils.isEmptyString(deposit_open)) {
            deposit_open = "0";
        }
        if (StringUtils.isEmptyString(deposit_gift_rate)) {
            deposit_gift_rate = "0";
        }
        if (StringUtils.isEmptyString(thirty_days_order)) {
            thirty_days_order = "0";
        }
        if (StringUtils.isEmptyString(thirty_days_order_ratio)) {
            thirty_days_order_ratio = "0";
        }
        if (StringUtils.isEmptyString(thirty_days_pass_average_time)) {
            thirty_days_pass_average_time = "0";
        }
        if (StringUtils.isEmptyString(thirty_days_pay_average_time)) {
            thirty_days_pay_average_time = "0";
        }
        if (StringUtils.isEmptyString(thirty_days_amount)) {
            thirty_days_amount = "0";
        }
        if (StringUtils.isEmptyString(buy_amount)) {
            buy_amount = "0";
        }
        if (StringUtils.isEmptyString(sell_amount)) {
            sell_amount = "0";
        }
        if (StringUtils.isEmptyString(total_amount)) {
            total_amount = "0";
        }
        if (StringUtils.isEmptyString(account_create_days)) {
            account_create_days = "0";
        }
        if (StringUtils.isEmptyString(first_exchange_days)) {
            first_exchange_days = "0";
        }
        if (StringUtils.isEmptyString(exchange_users)) {
            exchange_users = "0";
        }
        if (StringUtils.isEmptyString(buy_success_orders)) {
            buy_success_orders = "0";
        }
        if (StringUtils.isEmptyString(sell_success_orders)) {
            sell_success_orders = "0";
        }
        if (StringUtils.isEmptyString(total_success_orders)) {
            total_success_orders = "0";
        }
        if (StringUtils.isEmptyString(appraise_good)) {
            appraise_good = "0";
        }
        if (StringUtils.isEmptyString(appraise_bad)) {
            appraise_bad = "0";
        }
        if (StringUtils.isEmptyString(phone_authority)) {
            phone_authority = "N";
        }
        if (StringUtils.isEmptyString(email_authority)) {
            email_authority = "N";
        }
        if (StringUtils.isEmptyString(kyc_authority)) {
            kyc_authority = "N";
        }
        if (StringUtils.isEmptyString(kyc_highlevel_authority)) {
            kyc_highlevel_authority = "N";
        }
        if (StringUtils.isEmptyString(thirty_days_order_base)) {
            thirty_days_order_base = "0";
        }
        if (StringUtils.isEmptyString(thirty_days_order_ratio_base)) {
            thirty_days_order_ratio_base = "0";
        }
        if (StringUtils.isEmptyString(thirty_days_pass_average_time_base)) {
            thirty_days_pass_average_time_base = "0";
        }
        if (StringUtils.isEmptyString(thirty_days_pay_average_time_base)) {
            thirty_days_pay_average_time_base = "0";
        }
        if (StringUtils.isEmptyString(thirty_days_amount_base)) {
            thirty_days_amount_base = "0";
        }
        if (StringUtils.isEmptyString(buy_amount_base)) {
            buy_amount_base = "0";
        }
        if (StringUtils.isEmptyString(sell_amount_base)) {
            sell_amount_base = "0";
        }
        if (StringUtils.isEmptyString(total_amount_base)) {
            total_amount_base = "0";
        }
        if (StringUtils.isEmptyString(account_create_days_base)) {
            account_create_days_base = "0";
        }
        if (StringUtils.isEmptyString(first_exchange_days_base)) {
            first_exchange_days_base = "0";
        }
        if (StringUtils.isEmptyString(exchange_users_base)) {
            exchange_users_base = "0";
        }
        if (StringUtils.isEmptyString(buy_success_orders_base)) {
            buy_success_orders_base = "0";
        }
        if (StringUtils.isEmptyString(sell_success_orders_base)) {
            sell_success_orders_base = "0";
        }
        if (StringUtils.isEmptyString(total_success_orders_base)) {
            total_success_orders_base = "0";
        }
        if (StringUtils.isEmptyString(appraise_good_base)) {
            appraise_good_base = "0";
        }
        if (StringUtils.isEmptyString(appraise_bad_base)) {
            appraise_bad_base = "0";
        }
        if (StringUtils.isEmptyString(phone_authority_base)) {
            phone_authority_base = "N";
        }


        if (StringUtils.isEmptyString(email_authority_base)) {
            email_authority_base = "N";
        }
        if (StringUtils.isEmptyString(kyc_authority_base)) {
            kyc_authority_base = "N";
        }
        if (StringUtils.isEmptyString(kyc_highlevel_authority_base)) {
            kyc_highlevel_authority_base = "N";
        }
        String error = this.verifBase(c2c_user_type, c2c_user_party_code, nick_name, head_img, deposit, deposit_open, deposit_gift_rate);
        if (!StringUtils.isNullOrEmpty(error)) {
            throw new YamiShopBindException(error);
        }
        error = this.verifFront(type_front, username_front, password_front, re_password_front, usercode_front);
        if (!StringUtils.isNullOrEmpty(error)) {
            throw new YamiShopBindException(error);
        }
        error = this.verifStat(" 基础设置 ", thirty_days_order_base, thirty_days_order_ratio_base, thirty_days_pass_average_time_base, thirty_days_pay_average_time_base,
                thirty_days_amount_base, buy_amount_base, sell_amount_base, total_amount_base, account_create_days_base, first_exchange_days_base);
        if (!StringUtils.isNullOrEmpty(error)) {
            throw new YamiShopBindException(error);
        }
//        error = this.verifNotice(" 基础设置 ", order_mail_notice_open_base, order_sms_notice_open_base, order_app_notice_open_base, appeal_mail_notice_open_base, appeal_sms_notice_open_base,
//                appeal_app_notice_open_base, chat_app_notice_open_base, security_mail_notice_open_base, security_sms_notice_open_base, security_app_notice_open_base);
//        if (!StringUtils.isNullOrEmpty(error)) {
//            throw new YamiShopBindException(error);
//        }
        error = this.verifVerif(" 基础设置 ", phone_authority_base, email_authority_base, kyc_authority_base, kyc_highlevel_authority_base);
        if (!StringUtils.isNullOrEmpty(error)) {
            throw new YamiShopBindException(error);
        }
        error = this.verifOthers(" 基础设置 ", exchange_users_base, buy_success_orders_base, sell_success_orders_base, total_success_orders_base, appraise_good_base, appraise_bad_base);
        if (!StringUtils.isNullOrEmpty(error)) {
            throw new YamiShopBindException(error);
        }
        sysUserService.checkSafeWord(login_safeword);
        User party = null;
        if ("2".equals(c2c_user_type)) {
            // 用户承兑商
            party = userService.findUserByUserCode(c2c_user_party_code);
            if (null == party) {
                throw new YamiShopBindException("用户承兑商：用户UID不存在");
            }
        } else if ("1".equals(c2c_user_type)) {
            ///  后台承兑商
            party = userService.registerMobile(username_front, password_front, usercode_front,false);
            if (null == party) {
                throw new YamiShopBindException("后台承兑商：注册失败");
              }
            this.saveLog(party, SecurityUtils.getSysUser().getUsername(), "管理员操作：后台承兑商用户注册,ip[" + IPHelper.getIpAddr() + "]", Constants.LOG_CATEGORY_SECURITY);
        } else {
            throw new YamiShopBindException("承兑商类型不正确");
        }
        party.setC2cUserType(Integer.valueOf(c2c_user_type).intValue());
        userService.updateById(party);
        C2cUser c2cUser = new C2cUser();
        c2cUser.setC2cManagerPartyId(c2c_manager_party_id);
        c2cUser.setC2cUserType(Integer.valueOf(c2c_user_type).intValue());
        c2cUser.setC2cUserCode(getUsercode());
        c2cUser.setC2cUserPartyId(party.getUserId());
        c2cUser.setNickName(nick_name);
        c2cUser.setHeadImg(head_img);
        c2cUser.setDeposit(getInt(deposit));
        c2cUser.setDepositOpen(getInt(deposit_open));
        c2cUser.setDepositGiftRate(Double.valueOf(deposit_gift_rate).doubleValue());
        c2cUser.setThirtyDaysOrder(getInt(thirty_days_order_base));
        c2cUser.setThirtyDaysOrderRatio(getInt(thirty_days_order_ratio_base));
        c2cUser.setThirtyDaysPassAverageTime(getInt(thirty_days_pass_average_time_base));
        c2cUser.setThirtyDaysPayAverageTime(getInt(thirty_days_pass_average_time_base));
        c2cUser.setThirtyDaysAmount(getInt(thirty_days_amount_base));
        c2cUser.setBuyAmount(getInt(buy_amount_base));
        c2cUser.setSellAmount(getInt(sell_amount_base));
        c2cUser.setTotalAmount(getInt(total_amount_base));
        c2cUser.setAccountCreateDays(getInt(account_create_days_base));
        c2cUser.setFirstExchangeDays(getInt(first_exchange_days_base));
        c2cUser.setExchangeUsers(getInt(exchange_users_base));
        c2cUser.setBuySuccessOrders(getInt(buy_success_orders_base));
        c2cUser.setSellSuccessOrders(getInt(sell_success_orders_base));
        c2cUser.setTotalSuccessOrders(getInt(total_success_orders_base));
        c2cUser.setAppraiseGood(getInt(appraise_good_base));
        c2cUser.setAppraiseBad(getInt(appraise_bad_base));
        c2cUser.setOrderMailNoticeOpen(getInt(order_mail_notice_open_base));
        c2cUser.setOrderSmsNoticeOpen(getInt(order_sms_notice_open_base));
        c2cUser.setOrderAppNoticeOpen(getInt(order_app_notice_open_base));
        c2cUser.setAppealMailNoticeOpen(getInt(appeal_mail_notice_open_base));
        c2cUser.setAppealSmsNoticeOpen(getInt(appeal_sms_notice_open_base));
        c2cUser.setAppealAppNoticeOpen(getInt(appeal_app_notice_open_base));
        c2cUser.setChatAppNoticeOpen(getInt(chat_app_notice_open_base));
        c2cUser.setSecurityMailNoticeOpen(getInt(security_mail_notice_open_base));
        c2cUser.setSecuritySmsNoticeOpen(getInt(security_sms_notice_open_base));
        c2cUser.setSecurityAppNoticeOpen(getInt(security_app_notice_open_base));
        c2cUser.setRemark(remark);
        c2cUser.setCreateTime(new Date());
        c2cUser.setUpdateTime(new Date());
        c2cUserService.save(c2cUser);
        String log = MessageFormat.format("ip:" + IPHelper.getIpAddr()
                        + ",管理员新增承兑商,id:{0},承兑商类型:{1},承兑商CODE:{2},承兑商PARTY_ID:{3},承兑商昵称:{4},承兑商头像:{5},剩余保证金:{6},保证金:{7},"
                        + "备注:{8},创建时间:{9},更新时间:{10},C2C管理员PARTY_ID:{11},保证金赠送比率:{12}",
                c2cUser.getUuid(), c2cUser.getC2cUserType(), c2cUser.getC2cUserCode(), c2cUser.getC2cUserPartyId(), c2cUser.getNickName(), c2cUser.getHeadImg(), c2cUser.getDeposit(), c2cUser.getDepositOpen(),
                c2cUser.getRemark(), c2cUser.getCreateTime(), c2cUser.getUpdateTime(), c2cUser.getC2cManagerPartyId(), c2cUser.getDepositGiftRate());
        saveLog(party, SecurityUtils.getSysUser().getUsername(), log, Constants.LOG_CATEGORY_C2C);
        C2cUserParamBaseSet paramBaseSet = new C2cUserParamBaseSet();
        paramBaseSet.setC2cUserPartyId(party.getUserId());
        paramBaseSet.setThirtyDaysOrder(Integer.valueOf(thirty_days_order_base).intValue());
        paramBaseSet.setThirtyDaysOrderRatio(Double.valueOf(thirty_days_order_ratio_base).doubleValue());
        paramBaseSet.setThirtyDaysPassAverageTime(Integer.valueOf(thirty_days_pass_average_time_base).intValue());
        paramBaseSet.setThirtyDaysPayAverageTime(Integer.valueOf(thirty_days_pay_average_time_base).intValue());
        paramBaseSet.setThirtyDaysAmount(Double.valueOf(thirty_days_amount_base).doubleValue());
        paramBaseSet.setBuyAmount(Double.valueOf(buy_amount_base).doubleValue());
        paramBaseSet.setSellAmount(Double.valueOf(sell_amount_base).doubleValue());
        paramBaseSet.setTotalAmount(Double.valueOf(total_amount_base).doubleValue());
        paramBaseSet.setAccountCreateDays(Integer.valueOf(account_create_days_base).intValue());
        paramBaseSet.setFirstExchangeDays(Integer.valueOf(first_exchange_days_base).intValue());
        paramBaseSet.setExchangeUsers(Integer.valueOf(exchange_users_base).intValue());
        paramBaseSet.setBuySuccessOrders(Integer.valueOf(buy_success_orders_base).intValue());
        paramBaseSet.setSellSuccessOrders(Integer.valueOf(sell_success_orders_base).intValue());
        paramBaseSet.setTotalSuccessOrders(Integer.valueOf(total_success_orders_base).intValue());
        paramBaseSet.setAppraiseGood(Integer.valueOf(appraise_good_base).intValue());
        paramBaseSet.setAppraiseBad(Integer.valueOf(appraise_bad_base).intValue());
//        paramBaseSet.setOrderMailNoticeOpen(Integer.valueOf(order_mail_notice_open_base).intValue());
//        paramBaseSet.setOrderSmsNoticeOpen(Integer.valueOf(order_sms_notice_open_base).intValue());
//        paramBaseSet.setOrderAppNoticeOpen(Integer.valueOf(order_app_notice_open_base).intValue());
//        paramBaseSet.setAppealMailNoticeOpen(Integer.valueOf(appeal_mail_notice_open_base).intValue());
//        paramBaseSet.setAppealSmsNoticeOpen(Integer.valueOf(appeal_sms_notice_open_base).intValue());
//        paramBaseSet.setAppealAppNoticeOpen(Integer.valueOf(appeal_app_notice_open_base).intValue());
//        paramBaseSet.setChatAppNoticeOpen(Integer.valueOf(chat_app_notice_open_base).intValue());
//        paramBaseSet.setSecurityMailNoticeOpen(Integer.valueOf(security_mail_notice_open_base).intValue());
//        paramBaseSet.setSecuritySmsNoticeOpen(Integer.valueOf(security_sms_notice_open_base).intValue());
//        paramBaseSet.setSecurityAppNoticeOpen(Integer.valueOf(security_app_notice_open_base).intValue());
        paramBaseSet.setPhoneAuthority(phone_authority_base);
        paramBaseSet.setEmailAuthority(email_authority_base);
        paramBaseSet.setKycAuthority(kyc_authority_base);
        paramBaseSet.setKycHighlevelAuthority(kyc_highlevel_authority_base);
        paramBaseSet.setCreateTime(new Date());
        paramBaseSet.setUpdateTime(new Date());
        c2cUserParamBaseSetService.save(paramBaseSet);
        String logBase = MessageFormat.format("ip:" + IPHelper.getIpAddr()
                        + ",管理员新增承兑商参数基础设置,id:{0},承兑商PARTY_ID:{1},"
                        + "30日成单数:{2},30日成单率:{3},30日平均放行时间:{4},30日平均付款时间:{5},30日交易量:{6},买交易量:{7},卖交易量:{8},总交易量:{9},"
                        + "账号创建天数:{10},首次交易至今天数:{11},交易人数:{12},买成单数:{13},卖成单数:{14},总成单数:{15},"
                        + "好评数:{16},差评数:{17},订单邮件通知:{18},订单短信通知:{19},订单APP通知:{20},申诉邮件通知:{21},"
                        + "申诉短信通知:{22},申诉APP通知:{23},聊天APP通知:{24},安全邮件通知:{25},安全短信通知:{26},安全APP通知:{27},"
                        + "手机验证:{28},邮箱验证:{29},身份认证:{30},高级认证:{31},创建时间:{32},更新时间:{33}",
                paramBaseSet.getUuid(), paramBaseSet.getC2cUserPartyId(),
                paramBaseSet.getThirtyDaysOrder(), paramBaseSet.getThirtyDaysOrderRatio(), paramBaseSet.getThirtyDaysPassAverageTime(), paramBaseSet.getThirtyDaysPayAverageTime(), paramBaseSet.getThirtyDaysAmount(), paramBaseSet.getBuyAmount(), paramBaseSet.getSellAmount(), paramBaseSet.getTotalAmount(),
                paramBaseSet.getAccountCreateDays(), paramBaseSet.getFirstExchangeDays(), paramBaseSet.getExchangeUsers(), paramBaseSet.getBuySuccessOrders(), paramBaseSet.getSellSuccessOrders(), paramBaseSet.getTotalSuccessOrders(),
                paramBaseSet.getAppraiseGood(), paramBaseSet.getAppraiseBad(), paramBaseSet.getOrderMailNoticeOpen(), paramBaseSet.getOrderSmsNoticeOpen(), paramBaseSet.getOrderAppNoticeOpen(), paramBaseSet.getAppealMailNoticeOpen(),
                paramBaseSet.getAppealSmsNoticeOpen(), paramBaseSet.getAppealAppNoticeOpen(), paramBaseSet.getChatAppNoticeOpen(), paramBaseSet.getSecurityMailNoticeOpen(), paramBaseSet.getSecuritySmsNoticeOpen(), paramBaseSet.getSecurityAppNoticeOpen(),
                paramBaseSet.getPhoneAuthority(), paramBaseSet.getEmailAuthority(), paramBaseSet.getKycAuthority(), paramBaseSet.getKycHighlevelAuthority(), paramBaseSet.getCreateTime(), paramBaseSet.getUpdateTime());
        this.saveLog(party, SecurityUtils.getSysUser().getUsername(), logBase, Constants.LOG_CATEGORY_C2C);
        return Result.succeed();
    }

    private String getUsercode() {
        Syspara syspara = sysparaService.find("c2c_user_uid_sequence");
        int random = (int) (Math.random() * 3 + 1);
        int user_uid_sequence = syspara.getInteger() + random;
        syspara.setSvalue(user_uid_sequence+"");
        sysparaService.updateById(syspara);
        String usercode = String.valueOf(user_uid_sequence);
        return usercode;
    }

    /**
     *
     */
    @GetMapping("getDesc")
    @ApiOperation("获取详情")
    public Result getDesc( @Valid IdModel model) {
        String id =model.getId();
        C2cUser c2cUser = c2cUserService.getById(id);
        if (null == c2cUser) {
            throw new YamiShopBindException("承兑商不存在");
        }
        C2cUserParamBaseSet paramBaseSet = c2cUserParamBaseSetService.getByPartyId(c2cUser.getC2cUserPartyId());
        if (null == paramBaseSet) {
            throw new YamiShopBindException("承兑商参数基础设置不存在");
        }
        User party = userService.getById(c2cUser.getC2cUserPartyId());
        if (null == party) {
            throw new YamiShopBindException("承兑商的用户信息不存在");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("c2c_manager_party_id", c2cUser.getC2cManagerPartyId());
        map.put("c2c_user_type", c2cUser.getC2cUserType());
        map.put("c2c_user_type_name", 1 == c2cUser.getC2cUserType() ? "后台承兑商" : "用户承兑商");
        map.put("c2c_user_code", c2cUser.getC2cUserCode());
        map.put("nick_name", c2cUser.getNickName());
        map.put("c2c_user_party_code", party.getUserCode());
        map.put("head_img", c2cUser.getHeadImg());
        map.put("user_code", party.getUserCode());
        map.put("user_name", party.getUserName());
        map.put("deposit", c2cUser.getDeposit());
        map.put("deposit_open", c2cUser.getDepositOpen());
        map.put("deposit_gift_rate", c2cUser.getDepositGiftRate());
        map.put("thirty_days_order", c2cUser.getThirtyDaysOrder());
        map.put("thirty_days_order_ratio", c2cUser.getThirtyDaysOrderRatio());
        map.put("thirty_days_pass_average_time", c2cUser.getThirtyDaysPassAverageTime());
        map.put("thirty_days_pay_average_time", c2cUser.getThirtyDaysPayAverageTime());
        map.put("thirty_days_amount", c2cUser.getThirtyDaysAmount());
        map.put("buy_amount", c2cUser.getBuyAmount());
        map.put("sell_amount", c2cUser.getSellAmount());
        map.put("total_amount", c2cUser.getTotalAmount());
        map.put("account_create_days", c2cUser.getAccountCreateDays());
        map.put("first_exchange_days", c2cUser.getFirstExchangeDays());
        map.put("exchange_users", c2cUser.getExchangeUsers());
        map.put("buy_success_orders", c2cUser.getBuySuccessOrders());
        map.put("sell_success_orders", c2cUser.getSellSuccessOrders());
        map.put("total_success_orders", c2cUser.getTotalSuccessOrders());
        map.put("appraise_good", c2cUser.getAppraiseGood());
        map.put("appraise_bad", c2cUser.getAppraiseBad());
        map.put("order_mail_notice_open", c2cUser.getOrderMailNoticeOpen());
        map.put("order_sms_notice_open", c2cUser.getOrderSmsNoticeOpen());
        map.put("order_app_notice_open", c2cUser.getOrderAppNoticeOpen());
        map.put("appeal_mail_notice_open", c2cUser.getAppealMailNoticeOpen());
        map.put("appeal_sms_notice_open", c2cUser.getAppealSmsNoticeOpen());
        map.put("appeal_app_notice_open", c2cUser.getAppealAppNoticeOpen());
        map.put("chat_app_notice_open", c2cUser.getChatAppNoticeOpen());
        map.put("security_mail_notice_open", c2cUser.getSecurityMailNoticeOpen());
        map.put("security_sms_notice_open", c2cUser.getSecuritySmsNoticeOpen());
        map.put("security_app_notice_open", c2cUser.getSecurityAppNoticeOpen());
        map.put("phone_authority", party.isUserMobileBind());
        map.put("email_authority", party.isMailBind());
        map.put("kyc_authority", party.isRealNameAuthority());
        map.put("kyc_highlevel_authority", party.isHighlevelAuthority());
        map.put("thirty_days_order_base", paramBaseSet.getThirtyDaysOrder());
        map.put("thirty_days_order_ratio_base", paramBaseSet.getThirtyDaysOrderRatio());
        map.put("thirty_days_pass_average_time_base", paramBaseSet.getThirtyDaysPassAverageTime());
        map.put("thirty_days_pay_average_time_base", paramBaseSet.getThirtyDaysPayAverageTime());
        map.put("thirty_days_amount_base", paramBaseSet.getThirtyDaysAmount());
        map.put("buy_amount_base", paramBaseSet.getBuyAmount());
        map.put("sell_amount_base", paramBaseSet.getSellAmount());
        map.put("total_amount_base", paramBaseSet.getTotalAmount());
        map.put("account_create_days_base", paramBaseSet.getAccountCreateDays());
        map.put("first_exchange_days_base", paramBaseSet.getFirstExchangeDays());
        map.put("exchange_users_base", paramBaseSet.getExchangeUsers());
        map.put("buy_success_orders_base", paramBaseSet.getBuySuccessOrders());
        map.put("sell_success_orders_base", paramBaseSet.getSellSuccessOrders());
        map.put("total_success_orders_base", paramBaseSet.getTotalSuccessOrders());
        map.put("appraise_good_base", paramBaseSet.getAppraiseGood());
        map.put("appraise_bad_base", paramBaseSet.getAppraiseBad());
        map.put("order_mail_notice_open_base", paramBaseSet.getOrderMailNoticeOpen());
        map.put("order_sms_notice_open_base", paramBaseSet.getOrderSmsNoticeOpen());
        map.put("order_app_notice_open_base", paramBaseSet.getOrderAppNoticeOpen());
        map.put("appeal_mail_notice_open_base", paramBaseSet.getAppealMailNoticeOpen());
        map.put("appeal_sms_notice_open_base", paramBaseSet.getAppealSmsNoticeOpen());
        map.put("appeal_app_notice_open_base", paramBaseSet.getAppealAppNoticeOpen());
        map.put("chat_app_notice_open_base", paramBaseSet.getChatAppNoticeOpen());
        map.put("security_mail_notice_open_base", paramBaseSet.getSecurityMailNoticeOpen());
        map.put("security_sms_notice_open_base", paramBaseSet.getSecuritySmsNoticeOpen());
        map.put("security_app_notice_open_base", paramBaseSet.getSecurityAppNoticeOpen());
        map.put("security_app_notice_open_base", paramBaseSet.getSecurityAppNoticeOpen());
        map.put("security_app_notice_open_base", paramBaseSet.getSecurityAppNoticeOpen());
        map.put("security_app_notice_open_base", paramBaseSet.getSecurityAppNoticeOpen());
        map.put("security_app_notice_open_base", paramBaseSet.getSecurityAppNoticeOpen());
        map.put("phone_authority_base", paramBaseSet.getPhoneAuthority());
        map.put("email_authority_base", paramBaseSet.getEmailAuthority());
        map.put("kyc_authority_base", paramBaseSet.getKycAuthority());
        map.put("kyc_highlevel_authority_base", paramBaseSet.getKycHighlevelAuthority());
        map.put("remark", c2cUser.getRemark());
        return Result.succeed(map);
    }

    @PostMapping("update")
    @ApiOperation(" 修改承兑商")
    public Result update(@Valid  C2cUserUpdateModel  model) {
        HttpServletRequest request= HttpContextUtils.getHttpServletRequest();
        String id = request.getParameter("id");
        String c2c_manager_party_id = request.getParameter("c2c_manager_party_id");
        String c2c_user_type = request.getParameter("c2c_user_type");
        String c2c_user_type_name = request.getParameter("c2c_user_type_name");
        String c2c_user_code = request.getParameter("c2c_user_code");
        String nick_name = request.getParameter("nick_name");
        String head_img = request.getParameter("head_img");
        String user_code = request.getParameter("user_code");
        String user_name = request.getParameter("user_name");
        String deposit = request.getParameter("deposit");
        String deposit_open = request.getParameter("deposit_open");
        String deposit_gift_rate = request.getParameter("deposit_gift_rate");
        String thirty_days_order = request.getParameter("thirty_days_order");
        String thirty_days_order_ratio = request.getParameter("thirty_days_order_ratio");
        String thirty_days_pass_average_time = request.getParameter("thirty_days_pass_average_time");
        String thirty_days_pay_average_time = request.getParameter("thirty_days_pay_average_time");
        String thirty_days_amount = request.getParameter("thirty_days_amount");
        String buy_amount = request.getParameter("buy_amount");
        String sell_amount = request.getParameter("sell_amount");
        String total_amount = request.getParameter("total_amount");
        String account_create_days = request.getParameter("account_create_days");
        String first_exchange_days = request.getParameter("first_exchange_days");
        String exchange_users = request.getParameter("exchange_users");
        String buy_success_orders = request.getParameter("buy_success_orders");
        String sell_success_orders = request.getParameter("sell_success_orders");
        String total_success_orders = request.getParameter("total_success_orders");
        String appraise_good = request.getParameter("appraise_good");
        String appraise_bad = request.getParameter("appraise_bad");
        String order_mail_notice_open = request.getParameter("order_mail_notice_open");
        String order_sms_notice_open = request.getParameter("order_sms_notice_open");
        String order_app_notice_open = request.getParameter("order_app_notice_open");
        String appeal_mail_notice_open = request.getParameter("appeal_mail_notice_open");
        String appeal_sms_notice_open = request.getParameter("appeal_sms_notice_open");
        String appeal_app_notice_open = request.getParameter("appeal_app_notice_open");
        String chat_app_notice_open = request.getParameter("chat_app_notice_open");
        String security_mail_notice_open = request.getParameter("security_mail_notice_open");
        String security_sms_notice_open = request.getParameter("security_sms_notice_open");
        String security_app_notice_open = request.getParameter("security_app_notice_open");
        String phone_authority = request.getParameter("phone_authority");
        String email_authority = request.getParameter("email_authority");
        String kyc_authority = request.getParameter("kyc_authority");
        String kyc_highlevel_authority = request.getParameter("kyc_highlevel_authority");
        String thirty_days_order_base = request.getParameter("thirty_days_order_base");
        String thirty_days_order_ratio_base = request.getParameter("thirty_days_order_ratio_base");
        String thirty_days_pass_average_time_base = request.getParameter("thirty_days_pass_average_time_base");
        String thirty_days_pay_average_time_base = request.getParameter("thirty_days_pay_average_time_base");
        String thirty_days_amount_base = request.getParameter("thirty_days_amount_base");
        String buy_amount_base = request.getParameter("buy_amount_base");
        String sell_amount_base = request.getParameter("sell_amount_base");
        String total_amount_base = request.getParameter("total_amount_base");
        String account_create_days_base = request.getParameter("account_create_days_base");
        String first_exchange_days_base = request.getParameter("first_exchange_days_base");
        String exchange_users_base = request.getParameter("exchange_users_base");
        String buy_success_orders_base = request.getParameter("buy_success_orders_base");
        String sell_success_orders_base = request.getParameter("sell_success_orders_base");
        String total_success_orders_base = request.getParameter("total_success_orders_base");
        String appraise_good_base = request.getParameter("appraise_good_base");
        String appraise_bad_base = request.getParameter("appraise_bad_base");
        String order_mail_notice_open_base = request.getParameter("order_mail_notice_open_base");
        String order_sms_notice_open_base = request.getParameter("order_sms_notice_open_base");
        String order_app_notice_open_base = request.getParameter("order_app_notice_open_base");
        String appeal_mail_notice_open_base = request.getParameter("appeal_mail_notice_open_base");
        String appeal_sms_notice_open_base = request.getParameter("appeal_sms_notice_open_base");
        String appeal_app_notice_open_base = request.getParameter("appeal_app_notice_open_base");
        String chat_app_notice_open_base = request.getParameter("chat_app_notice_open_base");
        String security_mail_notice_open_base = request.getParameter("security_mail_notice_open_base");
        String security_sms_notice_open_base = request.getParameter("security_sms_notice_open_base");
        String security_app_notice_open_base = request.getParameter("security_app_notice_open_base");
        String phone_authority_base = request.getParameter("phone_authority_base");
        String email_authority_base = request.getParameter("email_authority_base");
        String kyc_authority_base = request.getParameter("kyc_authority_base");
        String kyc_highlevel_authority_base = request.getParameter("kyc_highlevel_authority_base");
        String remark = request.getParameter("remark");
        String login_safeword = request.getParameter("login_safeword");
        String error = this.verifBase(c2c_user_type, user_code, nick_name, head_img, deposit, deposit_open, deposit_gift_rate);
        if (!StringUtils.isNullOrEmpty(error)) {
            throw new YamiShopBindException(error);
        }
        error = this.verifStat(" 基础设置 ", thirty_days_order_base, thirty_days_order_ratio_base, thirty_days_pass_average_time_base, thirty_days_pay_average_time_base,
                thirty_days_amount_base, buy_amount_base, sell_amount_base, total_amount_base, account_create_days_base, first_exchange_days_base);
        if (!StringUtils.isNullOrEmpty(error)) {
            throw new YamiShopBindException(error);
        }
//        error = this.verifNotice(" 基础设置 ", order_mail_notice_open_base, order_sms_notice_open_base, order_app_notice_open_base, appeal_mail_notice_open_base, appeal_sms_notice_open_base,
//                appeal_app_notice_open_base, chat_app_notice_open_base, security_mail_notice_open_base, security_sms_notice_open_base, security_app_notice_open_base);
//        if (!StringUtils.isNullOrEmpty(error)) {
//            throw new YamiShopBindException(error);
//        }
        error = this.verifVerif(" 基础设置 ", phone_authority_base, email_authority_base, kyc_authority_base, kyc_highlevel_authority_base);
        if (!StringUtils.isNullOrEmpty(error)) {
            throw new YamiShopBindException(error);
        }
        error = this.verifOthers(" 基础设置 ", exchange_users_base, buy_success_orders_base, sell_success_orders_base, total_success_orders_base, appraise_good_base, appraise_bad_base);
        if (!StringUtils.isNullOrEmpty(error)) {
            throw new YamiShopBindException(error);
        }
        String userNameLogin = SecurityUtils.getSysUser().getUsername();
        sysUserService.checkSafeWord(login_safeword);
        C2cUser c2cUser = c2cUserService.getById(id);
        if (null == c2cUser) {
            throw new YamiShopBindException("承兑商不存在");
        }
        C2cUserParamBaseSet paramBaseSet = this.c2cUserParamBaseSetService.getByPartyId(c2cUser.getC2cUserPartyId());
        if (null == paramBaseSet) {
            throw new YamiShopBindException("承兑商参数基础设置不存在");
        }
        User party = userService.getById(c2cUser.getC2cUserPartyId());
        if (null == party) {
            throw new YamiShopBindException("承兑商的用户信息不存在");
        }
        String log = MessageFormat.format("ip:" + IPHelper.getIpAddr()
                        + ",管理员修改承兑商信息,id:{0},原承兑商类型:{1},原承兑商CODE:{2},原承兑商PARTY_ID:{3},原承兑商昵称:{4},原承兑商头像:{5},原剩余保证金:{6},原保证金:{7},"
                        + "原备注:{8},原创建时间:{9},原更新时间:{10},原C2C管理员PARTY_ID:{11},原保证金赠送比率:{12}",
                c2cUser.getUuid(), c2cUser.getC2cUserType(), c2cUser.getC2cUserCode(), c2cUser.getC2cUserPartyId(), c2cUser.getNickName(), c2cUser.getHeadImg(), c2cUser.getDeposit(), c2cUser.getDepositOpen(),
                c2cUser.getRemark(), c2cUser.getCreateTime(), c2cUser.getUpdateTime(), c2cUser.getC2cManagerPartyId(), c2cUser.getDepositGiftRate());
        c2cUser.setC2cManagerPartyId(c2c_manager_party_id);
        c2cUser.setNickName(nick_name);
        c2cUser.setHeadImg(head_img);
//				c2cUser.setDeposit(Double.valueOf(deposit).doubleValue());
//				c2cUser.setDepositOpen(Double.valueOf(deposit_open).doubleValue());
//        c2cUser.setDepositGiftRate(Double.valueOf(deposit_gift_rate).doubleValue());
        c2cUser.setRemark(remark);
        c2cUser.setUpdateTime(new Date());
        c2cUserService.updateById(c2cUser);
        log += MessageFormat.format(",id:{0},新承兑商类型:{1},新承兑商CODE:{2},新承兑商PARTY_ID:{3},新承兑商昵称:{4},新承兑商头像:{5},新剩余保证金:{6},新保证金:{7},"
                        + "新备注:{8},新创建时间:{9},新更新时间:{10},新C2C管理员PARTY_ID:{11},新保证金赠送比率:{12}",
                c2cUser.getUuid(), c2cUser.getC2cUserType(), c2cUser.getC2cUserCode(), c2cUser.getC2cUserPartyId(), c2cUser.getNickName(), c2cUser.getHeadImg(), c2cUser.getDeposit(), c2cUser.getDepositOpen(),
                c2cUser.getRemark(), c2cUser.getCreateTime(), c2cUser.getUpdateTime(), c2cUser.getC2cManagerPartyId(), c2cUser.getDepositGiftRate());
        this.saveLog(party, userNameLogin, log, Constants.LOG_CATEGORY_C2C);
        String logBase = MessageFormat.format("ip:" + IPHelper.getIpAddr()
                        + ",管理员修改承兑商参数基础设置信息,id:{0},原承兑商PARTY_ID:{1},"
                        + "原30日成单数:{2},原30日成单率:{3},原30日平均放行时间:{4},原30日平均付款时间:{5},原30日交易量:{6},原买交易量:{7},原卖交易量:{8},原总交易量:{9},"
                        + "原账号创建天数:{10},原首次交易至今天数:{11},原交易人数:{12},原买成单数:{13},原卖成单数:{14},原总成单数:{15},"
                        + "原好评数:{16},原差评数:{17},原订单邮件通知:{18},原订单短信通知:{19},原订单APP通知:{20},原申诉邮件通知:{21},"
                        + "原申诉短信通知:{22},原申诉APP通知:{23},原聊天APP通知:{24},原安全邮件通知:{25},原安全短信通知:{26},原安全APP通知:{27},"
                        + "原手机验证:{28},原邮箱验证:{29},原身份认证:{30},原高级认证:{31},原创建时间:{32},原更新时间:{33}",
                paramBaseSet.getUuid(), paramBaseSet.getC2cUserPartyId(),
                paramBaseSet.getThirtyDaysOrder(), paramBaseSet.getThirtyDaysOrderRatio(), paramBaseSet.getThirtyDaysPassAverageTime(), paramBaseSet.getThirtyDaysPayAverageTime(), paramBaseSet.getThirtyDaysAmount(), paramBaseSet.getBuyAmount(), paramBaseSet.getSellAmount(), paramBaseSet.getTotalAmount(),
                paramBaseSet.getAccountCreateDays(), paramBaseSet.getFirstExchangeDays(), paramBaseSet.getExchangeUsers(), paramBaseSet.getBuySuccessOrders(), paramBaseSet.getSellSuccessOrders(), paramBaseSet.getTotalSuccessOrders(),
                paramBaseSet.getAppraiseGood(), paramBaseSet.getAppraiseBad(), paramBaseSet.getOrderMailNoticeOpen(), paramBaseSet.getOrderSmsNoticeOpen(), paramBaseSet.getOrderAppNoticeOpen(), paramBaseSet.getAppealMailNoticeOpen(),
                paramBaseSet.getAppealSmsNoticeOpen(), paramBaseSet.getAppealAppNoticeOpen(), paramBaseSet.getChatAppNoticeOpen(), paramBaseSet.getSecurityMailNoticeOpen(), paramBaseSet.getSecuritySmsNoticeOpen(), paramBaseSet.getSecurityAppNoticeOpen(),
                paramBaseSet.getPhoneAuthority(), paramBaseSet.getEmailAuthority(), paramBaseSet.getKycAuthority(), paramBaseSet.getKycHighlevelAuthority(), paramBaseSet.getCreateTime(), paramBaseSet.getUpdateTime());
        paramBaseSet.setThirtyDaysOrder(Integer.valueOf(thirty_days_order_base).intValue());
        paramBaseSet.setThirtyDaysOrderRatio(Double.valueOf(thirty_days_order_ratio_base).doubleValue());
        paramBaseSet.setThirtyDaysPassAverageTime(Integer.valueOf(thirty_days_pass_average_time_base).intValue());
        paramBaseSet.setThirtyDaysPayAverageTime(Integer.valueOf(thirty_days_pay_average_time_base).intValue());
        paramBaseSet.setThirtyDaysAmount(Double.valueOf(thirty_days_amount_base).doubleValue());
        paramBaseSet.setBuyAmount(Double.valueOf(buy_amount_base).doubleValue());
        paramBaseSet.setSellAmount(Double.valueOf(sell_amount_base).doubleValue());
        paramBaseSet.setTotalAmount(Double.valueOf(total_amount_base).doubleValue());
        paramBaseSet.setAccountCreateDays(Integer.valueOf(account_create_days_base).intValue());
        paramBaseSet.setFirstExchangeDays(Integer.valueOf(first_exchange_days_base).intValue());
        paramBaseSet.setExchangeUsers(Integer.valueOf(exchange_users_base).intValue());
        paramBaseSet.setBuySuccessOrders(Integer.valueOf(buy_success_orders_base).intValue());
        paramBaseSet.setSellSuccessOrders(Integer.valueOf(sell_success_orders_base).intValue());
        paramBaseSet.setTotalSuccessOrders(Integer.valueOf(total_success_orders_base).intValue());
        paramBaseSet.setAppraiseGood(Integer.valueOf(appraise_good_base).intValue());
        paramBaseSet.setAppraiseBad(Integer.valueOf(appraise_bad_base).intValue());
//        paramBaseSet.setOrderMailNoticeOpen(Integer.valueOf(order_mail_notice_open_base).intValue());
//        paramBaseSet.setOrderSmsNoticeOpen(Integer.valueOf(order_sms_notice_open_base).intValue());
//        paramBaseSet.setOrderAppNoticeOpen(Integer.valueOf(order_app_notice_open_base).intValue());
//        paramBaseSet.setAppealMailNoticeOpen(Integer.valueOf(appeal_mail_notice_open_base).intValue());
//        paramBaseSet.setAppealSmsNoticeOpen(Integer.valueOf(appeal_sms_notice_open_base).intValue());
//        paramBaseSet.setAppealAppNoticeOpen(Integer.valueOf(appeal_app_notice_open_base).intValue());
//        paramBaseSet.setChatAppNoticeOpen(Integer.valueOf(chat_app_notice_open_base).intValue());
//        paramBaseSet.setSecurityMailNoticeOpen(Integer.valueOf(security_mail_notice_open_base).intValue());
//        paramBaseSet.setSecuritySmsNoticeOpen(Integer.valueOf(security_sms_notice_open_base).intValue());
//        paramBaseSet.setSecurityAppNoticeOpen(Integer.valueOf(security_app_notice_open_base).intValue());
        paramBaseSet.setPhoneAuthority(phone_authority_base);
        paramBaseSet.setEmailAuthority(email_authority_base);
        paramBaseSet.setKycAuthority(kyc_authority_base);
        paramBaseSet.setKycHighlevelAuthority(kyc_highlevel_authority_base);
        paramBaseSet.setUpdateTime(new Date());
        c2cUserParamBaseSetService.updateById(paramBaseSet);
        logBase += MessageFormat.format(",id:{0},新承兑商PARTY_ID:{1},"
                        + "新30日成单数:{2},新30日成单率:{3},新30日平均放行时间:{4},新30日平均付款时间:{5},新30日交易量:{6},新买交易量:{7},新卖交易量:{8},新总交易量:{9},"
                        + "新账号创建天数:{10},新首次交易至今天数:{11},新交易人数:{12},新买成单数:{13},新卖成单数:{14},新总成单数:{15},"
                        + "新好评数:{16},新差评数:{17},新订单邮件通知:{18},新订单短信通知:{19},新订单APP通知:{20},新申诉邮件通知:{21},"
                        + "新申诉短信通知:{22},新申诉APP通知:{23},新聊天APP通知:{24},新安全邮件通知:{25},新安全短信通知:{26},新安全APP通知:{27},"
                        + "新手机验证:{28},新邮箱验证:{29},新身份认证:{30},新高级认证:{31},新创建时间:{32},新更新时间:{33}",
                paramBaseSet.getUuid(), paramBaseSet.getC2cUserPartyId(),
                paramBaseSet.getThirtyDaysOrder(), paramBaseSet.getThirtyDaysOrderRatio(), paramBaseSet.getThirtyDaysPassAverageTime(), paramBaseSet.getThirtyDaysPayAverageTime(), paramBaseSet.getThirtyDaysAmount(), paramBaseSet.getBuyAmount(), paramBaseSet.getSellAmount(), paramBaseSet.getTotalAmount(),
                paramBaseSet.getAccountCreateDays(), paramBaseSet.getFirstExchangeDays(), paramBaseSet.getExchangeUsers(), paramBaseSet.getBuySuccessOrders(), paramBaseSet.getSellSuccessOrders(), paramBaseSet.getTotalSuccessOrders(),
                paramBaseSet.getAppraiseGood(), paramBaseSet.getAppraiseBad(), paramBaseSet.getOrderMailNoticeOpen(), paramBaseSet.getOrderSmsNoticeOpen(), paramBaseSet.getOrderAppNoticeOpen(), paramBaseSet.getAppealMailNoticeOpen(),
                paramBaseSet.getAppealSmsNoticeOpen(), paramBaseSet.getAppealAppNoticeOpen(), paramBaseSet.getChatAppNoticeOpen(), paramBaseSet.getSecurityMailNoticeOpen(), paramBaseSet.getSecuritySmsNoticeOpen(), paramBaseSet.getSecurityAppNoticeOpen(),
                paramBaseSet.getPhoneAuthority(), paramBaseSet.getEmailAuthority(), paramBaseSet.getKycAuthority(), paramBaseSet.getKycHighlevelAuthority(), paramBaseSet.getCreateTime(), paramBaseSet.getUpdateTime());
        this.saveLog(party, userNameLogin, logBase, Constants.LOG_CATEGORY_C2C);
        return Result.succeed();
    }

    /**
     * 修改 承兑商保证金
     */
    @PostMapping(value = "reset")
    @ApiOperation("修改 承兑商保证金")
    public Result reset(@RequestBody @Valid  C2cUserResetModel model) {
        String id =model.getId();
        String recharge_withdraw = model.getRecharge_withdraw();
        String money_change = model.getMoney_change();
        String safe_password = model.getSafe_password();
        if (StringUtils.isEmptyString(recharge_withdraw)) {
            throw new YamiShopBindException("修改方式不正确");
        }
        if (StringUtils.isEmptyString(money_change) || !StringUtils.isDouble(money_change) || Double.valueOf(money_change).doubleValue() <= 0) {
            throw new YamiShopBindException("修改额度未填或格式不正确");
        }
        DecimalFormat df = new DecimalFormat("#.########");
        double money_change_double = Double.valueOf(money_change).doubleValue();
        C2cUser c2cUser = c2cUserService.getById(id);
        if (null == c2cUser) {
            throw new YamiShopBindException("承兑商不存在");
        }
        String userNameLogin = SecurityUtils.getSysUser().getUsername();
        sysUserService.checkSafeWord(safe_password);
        double newDepositOpen = 0;
        double newDeposit = 0;
        if ("recharge".equals(recharge_withdraw)) {
//					if (money_change_double > 1000000) {
//						throw new BusinessException("单次充值额度不能超过 1000000 USDT");
//					}
            newDepositOpen = Double.valueOf(df.format(Arith.add(money_change_double, c2cUser.getDepositOpen()))).doubleValue();
            newDeposit = Double.valueOf(df.format(Arith.add(money_change_double, c2cUser.getDeposit()))).doubleValue();
        } else if ("withdraw".equals(recharge_withdraw)) {
            money_change_double = 0 - money_change_double;
            newDepositOpen = Double.valueOf(df.format(Arith.add(money_change_double, c2cUser.getDepositOpen()))).doubleValue();
            newDeposit = Double.valueOf(df.format(Arith.add(money_change_double, c2cUser.getDeposit()))).doubleValue();
            if (newDepositOpen <= 0 || newDeposit <= 0) {
                throw new YamiShopBindException("提现额度不能超过总保证金和剩余保证金");
            }
        } else {
            throw new YamiShopBindException("修改方式不正确");
        }
        c2cUser.setDepositOpen(newDepositOpen);
        c2cUser.setDeposit(newDeposit);
        c2cUserService.updateById(c2cUser);
        User user = userService.getById(c2cUser.getC2cUserPartyId());
        String log = MessageFormat.format("承兑商CODE:{0},承兑商昵称:{1},剩余保证金:{2},保证金:{3},修改金额:{4}",
                c2cUser.getC2cUserCode(), c2cUser.getNickName(), c2cUser.getDeposit(), c2cUser.getDepositOpen(), money_change_double);
        saveLog(user, userNameLogin, log, Constants.LOG_CATEGORY_C2C);
        return Result.succeed();
    }

    /**
     * 获取 C2C管理员信息
     */
    @GetMapping(value = "getC2cManagerInfo")
    @ApiOperation("获取 C2C管理员信息")
    public Result getC2cManagerInfo(@RequestBody @Valid C2cManagerInfoModel model) {
        String manager_id =model.getManager_id();
        String c2c_user_id = model.getC2c_user_id();
        List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
        if (StringUtils.isEmptyString(manager_id)) {
            throw new YamiShopBindException("C2C管理员PARTY_ID为空");
        }
        if (StringUtils.isEmptyString(c2c_user_id)) {
            throw new YamiShopBindException("承兑商ID为空");
        }
        List<Map<String, Object>> result = c2cUserService.getC2cManagerC2cUser(manager_id);
        if (null == result) {
        } else {
            for (int i = 0; i < result.size(); i++) {
                Map<String, Object> res = result.get(i);
                if (c2c_user_id.equals(res.get("id"))) {
                    ret.add(res);
                    result.remove(i);
                    break;
                }
            }
            ret.addAll(result);
        }
        return Result.succeed(ret);
    }

    private String verifBase(String c2c_user_type, String c2c_user_party_code, String nick_name, String head_img, String deposit, String deposit_open, String deposit_gift_rate) {
        if (StringUtils.isEmptyString(c2c_user_type) || !Arrays.asList("1", "2").contains(c2c_user_type)) {
            return "承兑商类型未填或格式不正确";
        }
        if ("2".equals(c2c_user_type)) {
            // 用户承兑商
            if (StringUtils.isEmptyString(c2c_user_party_code)) {
                return "用户承兑商：必须填写用户UID";
            }
        }
        if (StringUtils.isEmptyString(nick_name)) {
            return "承兑商昵称必填";
        }
        if (StringUtils.isEmptyString(head_img)) {
            return "请上传承兑商头像";
        }
//        if (StringUtils.isEmptyString(deposit_gift_rate) || !StringUtils.isDouble(deposit_gift_rate) || Double.valueOf(deposit_gift_rate).doubleValue() < 0) {
//            return "保证金赠送比率未填或格式不正确";
//        }
        return null;
    }

    private String verifFront(String type_front, String username_front, String password_front, String re_password_front, String usercode_front) {
        if (StringUtils.isEmptyString(type_front) || !Arrays.asList("1", "2").contains(type_front)) {
            return "注册类型（手机或邮箱）未填或格式不正确";
        }
        if (StringUtils.isEmptyString(username_front)) {
            return "前端登录用户名必填";
        }
        if (StringUtils.isEmptyString(password_front)) {
            return "前端登录密码必填";
        }
        if (!password_front.equals(re_password_front)) {
            return "前端登录密码确认不一致";
        }
//		if (StringUtils.isEmptyString(usercode_front)) {
//			return "推荐码必填";
//		}
        return null;
    }

    private String verifStat(String baseSetTip, String thirty_days_order, String thirty_days_order_ratio, String thirty_days_pass_average_time, String thirty_days_pay_average_time,
                             String thirty_days_amount, String buy_amount, String sell_amount, String total_amount, String account_create_days, String first_exchange_days) {
        if (StringUtils.isEmptyString(thirty_days_order) || !StringUtils.isInteger(thirty_days_order) || Integer.valueOf(thirty_days_order).intValue() < 0) {
            return "30日成单数" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(thirty_days_order_ratio) || !StringUtils.isDouble(thirty_days_order_ratio) || Double.valueOf(thirty_days_order_ratio).doubleValue() < 0) {
            return "30日成单率" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(thirty_days_pass_average_time) || !StringUtils.isInteger(thirty_days_pass_average_time) || Integer.valueOf(thirty_days_pass_average_time).intValue() < 0) {
            return "30日平均放行时间" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(thirty_days_pay_average_time) || !StringUtils.isInteger(thirty_days_pay_average_time) || Integer.valueOf(thirty_days_pay_average_time).intValue() < 0) {
            return "30日平均付款时间" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(thirty_days_amount) || !StringUtils.isDouble(thirty_days_amount) || Double.valueOf(thirty_days_amount).doubleValue() < 0) {
            return "30日交易量" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(buy_amount) || !StringUtils.isDouble(buy_amount) || Double.valueOf(buy_amount).doubleValue() < 0) {
            return "买交易量" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(sell_amount) || !StringUtils.isDouble(sell_amount) || Double.valueOf(sell_amount).doubleValue() < 0) {
            return "卖交易量" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(total_amount) || !StringUtils.isDouble(total_amount) || Double.valueOf(total_amount).doubleValue() < 0) {
            return "总交易量" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(account_create_days) || !StringUtils.isInteger(account_create_days) || Integer.valueOf(account_create_days).intValue() < 0) {
            return "账号创建天数" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(first_exchange_days) || !StringUtils.isInteger(first_exchange_days) || Integer.valueOf(first_exchange_days).intValue() < 0) {
            return "首次交易至今天数" + baseSetTip + "未填或格式不正确";
        }
        return null;
    }

    private String verifNotice(String baseSetTip, String order_mail_notice_open, String order_sms_notice_open, String order_app_notice_open, String appeal_mail_notice_open, String appeal_sms_notice_open,
                               String appeal_app_notice_open, String chat_app_notice_open, String security_mail_notice_open, String security_sms_notice_open, String security_app_notice_open) {
        if (StringUtils.isEmptyString(order_mail_notice_open) || !Arrays.asList("0", "1").contains(order_mail_notice_open)) {
            return "订单邮件通知" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(order_sms_notice_open) || !Arrays.asList("0", "1").contains(order_sms_notice_open)) {
            return "订单短信通知" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(order_app_notice_open) || !Arrays.asList("0", "1").contains(order_app_notice_open)) {
            return "订单APP通知" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(appeal_mail_notice_open) || !Arrays.asList("0", "1").contains(appeal_mail_notice_open)) {
            return "申诉邮件通知" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(appeal_sms_notice_open) || !Arrays.asList("0", "1").contains(appeal_sms_notice_open)) {
            return "申诉短信通知" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(appeal_app_notice_open) || !Arrays.asList("0", "1").contains(appeal_app_notice_open)) {
            return "申诉APP通知" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(chat_app_notice_open) || !Arrays.asList("0", "1").contains(chat_app_notice_open)) {
            return "聊天APP通知" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(security_mail_notice_open) || !Arrays.asList("0", "1").contains(security_mail_notice_open)) {
            return "安全邮件通知" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(security_sms_notice_open) || !Arrays.asList("0", "1").contains(security_sms_notice_open)) {
            return "安全短信通知" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(security_app_notice_open) || !Arrays.asList("0", "1").contains(security_app_notice_open)) {
            return "安全APP通知" + baseSetTip + "未填或格式不正确";
        }
        return null;
    }

    private String verifVerif(String baseSetTip, String phone_authority, String email_authority, String kyc_authority, String kyc_highlevel_authority) {
        if (StringUtils.isEmptyString(phone_authority) || !Arrays.asList("N", "Y").contains(phone_authority)) {
            return "手机验证" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(email_authority) || !Arrays.asList("N", "Y").contains(email_authority)) {
            return "邮箱验证" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(kyc_authority) || !Arrays.asList("N", "Y").contains(kyc_authority)) {
            return "身份认证" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(kyc_highlevel_authority) || !Arrays.asList("N", "Y").contains(kyc_highlevel_authority)) {
            return "高级认证" + baseSetTip + "未填或格式不正确";
        }
        return null;
    }

    private String verifOthers(String baseSetTip, String exchange_users, String buy_success_orders, String sell_success_orders, String total_success_orders, String appraise_good, String appraise_bad) {
        if (StringUtils.isEmptyString(exchange_users) || !StringUtils.isInteger(exchange_users) || Integer.valueOf(exchange_users).intValue() < 0) {
            return "交易人数" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(buy_success_orders) || !StringUtils.isInteger(buy_success_orders) || Integer.valueOf(buy_success_orders).intValue() < 0) {
            return "买成单数" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(sell_success_orders) || !StringUtils.isInteger(sell_success_orders) || Integer.valueOf(sell_success_orders).intValue() < 0) {
            return "卖成单数" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(total_success_orders) || !StringUtils.isInteger(total_success_orders) || Integer.valueOf(total_success_orders).intValue() < 0) {
            return "总成单数" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(appraise_good) || !StringUtils.isInteger(appraise_good) || Integer.valueOf(appraise_good).intValue() < 0) {
            return "好评数" + baseSetTip + "未填或格式不正确";
        }
        if (StringUtils.isEmptyString(appraise_bad) || !StringUtils.isInteger(appraise_bad) || Integer.valueOf(appraise_bad).intValue() < 0) {
            return "差评数" + baseSetTip + "未填或格式不正确";
        }
        return null;
    }

    public void saveLog(User secUser, String operator, String context, String category) {
        Log log = new Log();
        log.setCategory(category);
        log.setOperator(operator);
        log.setUsername(secUser.getUserName());
        log.setUserId(secUser.getUserId());
        log.setLog(context);
        log.setCreateTime(new Date());
        logService.save(log);
    }
}
