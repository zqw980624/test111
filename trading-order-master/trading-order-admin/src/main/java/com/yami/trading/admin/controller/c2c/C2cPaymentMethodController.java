package com.yami.trading.admin.controller.c2c;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.controller.c2c.model.PaymentMethodAddModel;
import com.yami.trading.admin.controller.c2c.model.PaymentMethodDeleteModel;
import com.yami.trading.admin.controller.c2c.model.PaymentMethodUpdateModel;
import com.yami.trading.admin.model.IdModel;
import com.yami.trading.admin.model.c2c.C2cPaymentMethodListModel;
import com.yami.trading.bean.agent.AgentDto;
import com.yami.trading.bean.c2c.dto.C2cPaymentMethodDto;
import com.yami.trading.bean.model.*;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.IPHelper;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.AwsS3OSSFileService;
import com.yami.trading.service.agent.AgentService;
import com.yami.trading.service.c2c.C2cAdvertService;
import com.yami.trading.service.c2c.C2cPaymentMethodService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("paymentMethod")
@Api(tags = "C2C支付方式管理")
@Slf4j
public class C2cPaymentMethodController {
    @Autowired
    private C2cPaymentMethodService adminC2cPaymentMethodService;
    @Autowired
    private UserService userService;
    @Autowired
    private LogService logService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SysparaService sysparaService;
    @Autowired
    private C2cPaymentMethodService c2cPaymentMethodService;
    @Autowired
    private C2cAdvertService c2cAdvertService;
    @Autowired
    SysUserService sysUserService;
    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;
    @Autowired
    AgentService agentService;

    /**
     * 获取 代理商支付方式 列表
     */
    @PostMapping("list")
    @ApiOperation("列表")
    public Result<Page<C2cPaymentMethodDto>> list(@RequestBody @Valid C2cPaymentMethodListModel model) {
        Map<String, String> pmtMap = this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type");
        Page<C2cPaymentMethodDto> page =null;
        SysUser sysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        Page<AgentDto> pages = new Page(model.getCurrent(), model.getSize());
        pages = agentService.listTotal(pages, sysUser.getUsername());
        List<AgentDto> lists = pages.getRecords();
        if (lists.size()>0) {//代理商
            String userCode = lists.get(0).getUserCode();
             page = new Page<>(model.getCurrent(), model.getSize());
            adminC2cPaymentMethodService.listPage(page, "", userCode, model.getMethodType(), model.getMethodName());
            List<C2cPaymentMethodDto> list = page.getRecords();
            for (C2cPaymentMethodDto dto : list) {
                    dto.setMethodTypeName(pmtMap.containsKey(dto.getMethodType() + "") ? pmtMap.get(dto.getMethodType() + "") : dto.getMethodType() + "");
                    dto.setDeflag("2");
            }
        }else{//总管理
            page = new Page<>(model.getCurrent(), model.getSize());
            adminC2cPaymentMethodService.listPage(page, "", model.getUserCode(), model.getMethodType(), model.getMethodName());
            List<C2cPaymentMethodDto> list = page.getRecords();
            for (C2cPaymentMethodDto dto : list) {
                dto.setMethodTypeName(pmtMap.containsKey(dto.getMethodType() + "") ? pmtMap.get(dto.getMethodType() + "") : dto.getMethodType() + "");
                dto.setDeflag("1");
            }
        }
        return Result.ok(page);
    }

    @PostMapping("add")
    @ApiOperation("新增支付方式")
    public Result add(@RequestBody @Valid PaymentMethodAddModel model) {
        SysUser sysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        Page<AgentDto> page = new Page(1, 5);
        page = agentService.listTotal(page, sysUser.getUsername());
        List<AgentDto> list = page.getRecords();
        if(StringUtils.isNullOrEmpty(model.getParamName1()) ||StringUtils.isNullOrEmpty(model.getParamName4())){
            throw new YamiShopBindException("null");
        }
        if (list.size()>0) {
            QueryWrapper<C2cPaymentMethod> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(StringUtils.isNotEmpty(list.get(0).getUserCode()), "param_value2", list.get(0).getUserCode())
            .eq(StringUtils.isNotEmpty(model.getParamName1()), "param_name1", model.getParamName1());
            C2cPaymentMethod one = adminC2cPaymentMethodService.getOne(queryWrapper);
            if(one!=null){
                throw new YamiShopBindException("The agent has added bank information");
            }
            C2cPaymentMethod method = new C2cPaymentMethod();
            method.setRealName(model.getRealName());//收款人真实姓名
            method.setParamName1(model.getParamName1());//账号卡号
            method.setParamValue1(model.getParamValue1());//银行开户地址
            method.setParamName2(model.getParamName2());//银行联行号  ifcs
            method.setParamValue2(list.get(0).getUserCode());//userCode推存吗
            method.setParamName3(model.getParamName3());//银行名称
            method.setParamName4(model.getParamName4());//upi
            method.setParamValue4("2");//处理状态1-已处理，2-处理中，3-拒绝
            method.setRemark(model.getRemark());//备注
            method.setMethodName(model.getMethodName());//银行卡类型
            method.setCreateTime(new Date());
            adminC2cPaymentMethodService.save(method);
        }else{
            throw new YamiShopBindException("Not an agent and cannot be added");
        }
        return Result.ok(null);
    }

    @ApiOperation("修改 支付方式")
    @PostMapping("update")
    public Result update(@RequestBody @Valid PaymentMethodUpdateModel model) {
        C2cPaymentMethod method = adminC2cPaymentMethodService.getById(model.getUuid());
        if (null == method) {
            throw new YamiShopBindException("支付方式不存在");
        }
        SysUser sysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        Page<AgentDto> page = new Page(1, 5);
        page = agentService.listTotal(page, sysUser.getUsername());
        List<AgentDto> list = page.getRecords();
        if (list.size()>0) {
            method.setRealName(model.getRealName());//收款人真实姓名
            method.setParamName1(model.getParamName1());//账号卡号
            method.setParamValue1(model.getParamValue1());//银行开户地址
            method.setParamName2(model.getParamName2());//银行联行号  ifcs
            method.setParamName3(model.getParamName3());//银行名称
            method.setParamName4(model.getParamName4());//upi
            method.setRemark(model.getRemark());//备注
            method.setMethodName(model.getMethodName());//银行卡类型
            method.setUpdateTime(new Date());
            method.setUpdateTime(new Date());
            this.adminC2cPaymentMethodService.updateById(method);
        }else{
            throw new YamiShopBindException("Not an agent and cannot be modified");
        }
        return Result.ok(null);
    }

    @ApiOperation("审核 支付方式")
    @PostMapping("authUpdate")
    public Result authUpdate(@RequestBody @Valid PaymentMethodUpdateModel model) {
        C2cPaymentMethod method = adminC2cPaymentMethodService.getById(model.getUuid());;
        if (null == method) {
            throw new YamiShopBindException("审核支付方式不存在");
        }
            method.setParamValue4(model.getParamValue4());//处理状态1-已处理，2-处理中，3-拒绝
            this.adminC2cPaymentMethodService.updateById(method);
        return Result.ok(null);
    }

//    /**
//     * 修改 支付方式 页面
//     */
    @ApiOperation("获取详情")
    @PostMapping("get")
    public Result<C2cPaymentMethodDto> get(@RequestBody @Valid IdModel model) {
        C2cPaymentMethod method = this.adminC2cPaymentMethodService.get(model.getId());
        if (null == method) {
            throw new YamiShopBindException("支付方式不存在");
        }
        C2cPaymentMethodDto dto = new C2cPaymentMethodDto();
        BeanUtils.copyProperties(method, dto);
        return Result.ok(dto);
    }

    /**
     * 删除 支付方式
     */
    @ApiOperation("删除 支付方式")
    @PostMapping("delete")
    public Result delete(@RequestBody @Valid PaymentMethodDeleteModel model) {

        sysUserService.checkSafeWord(model.getLoginSafeword());
        C2cPaymentMethod method = this.adminC2cPaymentMethodService.get(model.getId());
        if (null == method) {
            throw new YamiShopBindException("支付方式不存在");
        }
        String log = MessageFormat.format("ip:" + IPHelper.getIpAddr()
                        + ",管理员删除支付方式,id:{0},用户PARTY_ID:{1},支付方式模板:{2},支付方式类型:{3},支付方式名称:{4},支付方式图片:{5},真实姓名:{6},"
                        + "参数名1:{7},参数值1:{8},参数名2:{9},参数值2:{10},参数名3:{11},参数值3:{12},参数名4:{13},参数值4:{14},参数名5:{15},参数值5:{16},"
                        + "参数名6:{17},参数值6:{18},参数名7:{19},参数值7:{20},参数名8:{21},参数值8:{22},参数名9:{23},参数值9:{24},参数名10:{25},参数值10:{26},"
                        + "参数名6:{27},参数值6:{28},参数名7:{29},参数值7:{30},参数名8:{31},参数值8:{32},参数名9:{33},参数值9:{34},参数名10:{35},参数值10:{36},"
                        + "支付二维码:{37},备注:{38},创建时间:{39},更新时间:{40}",
                method.getUuid(), method.getPartyId(), method.getMethodType(), method.getMethodName(), method.getMethodImg(), method.getRealName(),
                method.getParamName1(), method.getParamValue1(), method.getParamName2(), method.getParamValue2(), method.getParamName3(), method.getParamName4(), method.getParamValue4(),
                method.getRemark(), method.getCreateTime(), method.getUpdateTime());
        adminC2cPaymentMethodService.removeById(model.getId());
        SysUser sysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        Log dbLog = new Log();
        dbLog.setCategory(Constants.LOG_CATEGORY_C2C);
        dbLog.setOperator(sysUser.getUsername() + "");
        dbLog.setUsername(sysUser.getUsername() + "");
        dbLog.setUserId(sysUser.getUserId() + "");
        dbLog.setLog(log);
        dbLog.setCreateTime(new Date());
        logService.save(dbLog);
        return Result.ok(null);
    }


    public String verifAdd(String user_code, String method_config_id, String real_name, String param_value1, String login_safeword) {

        if (StringUtils.isNullOrEmpty(user_code)) {
            return "用户UID必填";
        }
        if (StringUtils.isNullOrEmpty(method_config_id)) {
            return "支付方式模板不正确";
        }
        if (StringUtils.isNullOrEmpty(real_name)) {
            return "真实姓名必填";
        }
        if (StringUtils.isNullOrEmpty(param_value1)) {
            return "参数值1必填";
        }
        if (StringUtils.isNullOrEmpty(login_safeword)) {
            return "资金密码错误";
        }
        return "";
    }

    public String verifUpdate(String real_name, String param_value1, String login_safeword) {

        if (StringUtils.isNullOrEmpty(real_name)) {
            return "真实姓名必填";
        }
        if (StringUtils.isNullOrEmpty(param_value1)) {
            return "参数值1必填";
        }
        if (StringUtils.isNullOrEmpty(login_safeword)) {
            return "资金密码错误";
        }
        return "";
    }
}


