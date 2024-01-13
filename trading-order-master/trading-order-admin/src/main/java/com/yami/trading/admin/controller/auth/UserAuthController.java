package com.yami.trading.admin.controller.auth;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.facade.PermissionFacade;
import com.yami.trading.admin.model.RealNameExamineModel;
import com.yami.trading.admin.model.UserAuthListModel;
import com.yami.trading.bean.agent.AgentDto;
import com.yami.trading.bean.model.RealNameAuthRecord;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.user.dto.RealNameAuthDto;
import com.yami.trading.bean.user.dto.RealNameAuthUpdateDto;
import com.yami.trading.common.annotation.SysLog;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.AwsS3OSSFileService;
import com.yami.trading.service.RealNameAuthRecordService;
import com.yami.trading.service.agent.AgentService;
import com.yami.trading.service.system.TipService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.sys.dto.SysUserInfoDto;
import com.yami.trading.sys.model.SysRole;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.service.SysRoleService;
import com.yami.trading.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Api(tags = "用户基础认证")
public class UserAuthController {
/*

    @Autowired
    RealNameAuthRecordService realNameAuthRecordService;

    @Autowired
    UserService userService;

    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;

    @Autowired
    TipService tipService;
    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page<RealNameAuthDto>> list(@RequestBody @Valid UserAuthListModel request){
        Page<RealNameAuthDto> page=new Page(request.getCurrent(),request.getSize());
        realNameAuthRecordService.pageRecord(page,request.getRoleName(),request.getIdNumber(),
                request.getStatus(),request.getUserName());
        return  Result.ok(page);
    }
*/
@Autowired
private SysRoleService sysRoleService;
    @Autowired
    RealNameAuthRecordService realNameAuthRecordService;
    @Autowired
    UserService userService;
    @Autowired
    private PermissionFacade permissionFacade;
    @Autowired
    TipService tipService;
    @Autowired
    SysUserService sysUserService;
    @Autowired
    AgentService agentService;
    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page<RealNameAuthDto>> list(@RequestBody @Valid UserAuthListModel request){
        List<String> roleNames = new ArrayList<>();
        if (StrUtil.isEmpty(request.getRoleName())) {
            roleNames.add(Constants.SECURITY_ROLE_GUEST);
            roleNames.add(Constants.SECURITY_ROLE_MEMBER);
            roleNames.add(Constants.SECURITY_ROLE_TEST);
        } else {
            roleNames.add(request.getRoleName());
        }

        SysUser sysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        SysUserInfoDto sysUserInfoDto = new SysUserInfoDto();
        BeanUtils.copyProperties(sysUser, sysUserInfoDto);
        List<Long> roleIds = sysRoleService.listRoleIdByUserId(sysUser.getUserId());
        Map<Long, SysRole> sysRoleMap = sysRoleService.list().stream().collect(Collectors.toMap(SysRole::getRoleId, SysRole -> SysRole));
        List<String> roleNamess = new ArrayList<>();
        if (sysUser.getUsername().equals("admin")){
            roleNamess.add("超级管理员");
        }
        roleIds.forEach(rid -> {
            if (sysRoleMap.containsKey(rid)) {
                roleNames.add(sysRoleMap.get(rid).getRoleName());
            }
        });
        sysUserInfoDto.setRoleName(roleNames);
        Page<AgentDto> pages = new Page(1, 5);
        pages = agentService.listTotal(pages, sysUser.getUsername());
        List<AgentDto> lists = pages.getRecords();
        if (lists.size()>0) {
            Page<RealNameAuthDto> page=new Page(request.getCurrent(),request.getSize());
            realNameAuthRecordService.pageRecords(page,roleNames,request.getIdNumber(),
                    request.getStatus(),request.getUserName(),permissionFacade.getOwnerUserIds());
            return  Result.ok(page);
        }
        else if (sysUserInfoDto.getRoleName().get(0).equals("超级管理员") || sysUserInfoDto.getRoleName().get(0).equals("财务")) {
            Page<RealNameAuthDto> page=new Page(request.getCurrent(),request.getSize());
            realNameAuthRecordService.pageRecord(page,request.getRoleName(),request.getIdNumber(),
                    request.getStatus(),request.getUserName());
            return  Result.ok(page);
        }else{
            Page<RealNameAuthDto> page=new Page(request.getCurrent(),request.getSize());
            realNameAuthRecordService.pageRecord(page,request.getRoleName(),request.getIdNumber(),
                    request.getStatus(),request.getUserName());
            return  Result.ok(page);
        }
    }

    @ApiOperation("查询用户信息")
    @GetMapping("getById/{id}")
    public Result<RealNameAuthDto> getById(@PathVariable String id) {
        if(StringUtils.isEmpty(id)) {
            return Result.failed("记录ID不能为空");
        }

        RealNameAuthRecord realNameAuthRecord = realNameAuthRecordService.getOne(Wrappers.<RealNameAuthRecord>lambdaQuery().eq(RealNameAuthRecord::getUuid, id));
        if(null == realNameAuthRecord) {
            return Result.failed("记录不存在");
        }
        User user = userService.getById(realNameAuthRecord.getUserId());
        if(null == user) {
            return Result.failed("用户不存在");
        }

        RealNameAuthDto realNameAuthDto = new RealNameAuthDto();
        realNameAuthDto.setUserName(user.getUserName());
        realNameAuthDto.setName(user.getRealName());
        if(null != realNameAuthRecord) {
            realNameAuthDto.setUuid(realNameAuthRecord.getUuid());
            realNameAuthDto.setIdName(realNameAuthRecord.getIdName());
            realNameAuthDto.setIdNumber(realNameAuthRecord.getIdNumber());
            realNameAuthDto.setNationality(realNameAuthRecord.getNationality());
            realNameAuthDto.setIdFrontImg(realNameAuthRecord.getIdFrontImg());
            realNameAuthDto.setIdBackImg(realNameAuthRecord.getIdBackImg());
            realNameAuthDto.setHandheldPhoto(realNameAuthRecord.getHandheldPhoto());
        }
        return Result.succeed(realNameAuthDto, "查询成功");
    }

    @ApiOperation(value = "修改")
    @PutMapping("edit")
    public Result<?> edit(@RequestBody @Valid RealNameAuthUpdateDto dto) {
       RealNameAuthRecord realNameAuthRecord=  realNameAuthRecordService.getById(dto.getUuid());
        if(realNameAuthRecord==null) {
            return Result.failed("记录ID不能为空");
        }
        if(StringUtils.isNotEmpty(dto.getIdName())) {
            realNameAuthRecord.setIdName(dto.getIdName());
        }
        if(StringUtils.isNotEmpty(dto.getIdNumber())) {
            realNameAuthRecord.setIdNumber(dto.getIdNumber());
        }
        if(StringUtils.isNotEmpty(dto.getName())) {
            realNameAuthRecord.setName(dto.getName());
        }
        if(StringUtils.isNotEmpty(dto.getNationality())) {
            realNameAuthRecord.setNationality(dto.getNationality());

        }
        if(StringUtils.isNotEmpty(dto.getIdFrontImg())) {
            realNameAuthRecord.setIdFrontImg(dto.getIdFrontImg());
        }
        if(StringUtils.isNotEmpty(dto.getIdBackImg())) {
            realNameAuthRecord.setIdBackImg(dto.getIdBackImg());

        }
        if(StringUtils.isNotEmpty(dto.getHandheldPhoto())) {
            realNameAuthRecord.setHandheldPhoto(dto.getHandheldPhoto());

        }
        boolean flag = realNameAuthRecordService.updateById(realNameAuthRecord);
        return Result.ok("修改成功");
    }

    @ApiOperation(value = "审核")
    @PostMapping("examine")
    @SysLog("用户基础认证-审核")
    public Result<?> examine(@RequestBody @Valid RealNameExamineModel model){
        RealNameAuthRecord realNameAuthRecord= realNameAuthRecordService.getById(model.getId());
        if (realNameAuthRecord==null){
            throw  new YamiShopBindException("参数错误");
        }
        int status=  realNameAuthRecord.getStatus();
        if (model.getType()==1){
            realNameAuthRecord.setStatus(2);
            realNameAuthRecord.setOperationTime(new Date());
            realNameAuthRecordService.updateById(realNameAuthRecord);

           User user= userService.getById(realNameAuthRecord.getUserId());
            user.setRealNameAuthority(true);
            user.setRealName(realNameAuthRecord.getName());
            // 获取用户系统等级：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证； 4/用户高级认证；
            int userLevelSystem = userService.getUserLevelByAuth(user);

            // 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
            // 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
            // 如：级别11表示：新注册的前端显示为VIP1；
            int userLevel = user.getUserLevel();
//            user.setUserLevel(((int) Math.floor(userLevel / 10)) * 10 + userLevelSystem);
            userService.updateById(user);
        }
        if (model.getType()==2){
            realNameAuthRecord.setStatus(3);
            realNameAuthRecord.setMsg(model.getContent());
            realNameAuthRecord.setOperationTime(new Date());
            realNameAuthRecordService.updateById(realNameAuthRecord);
        }
        tipService.deleteTip(realNameAuthRecord.getUuid());
        return  Result.ok(null);
    }

}
