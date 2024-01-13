/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.trading.sys.controller;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.agent.AgentDto;
import com.yami.trading.common.annotation.SysLog;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.GoogleAuthenticator;
import com.yami.trading.common.util.PageParam;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.security.common.enums.SysTypeEnum;
import com.yami.trading.security.common.manager.PasswordManager;
import com.yami.trading.security.common.manager.TokenStore;
import com.yami.trading.service.agent.AgentService;
import com.yami.trading.sys.constant.Constant;
import com.yami.trading.sys.dto.*;
import com.yami.trading.sys.model.SysRole;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.model.UnbindingGoogleAuthModel;
import com.yami.trading.sys.service.SysRoleService;
import com.yami.trading.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统用户
 *
 * @author lgh
 */
@RestController
@RequestMapping("/sys/user")
@Slf4j
@Api(tags = "系统用户")
public class SysUserController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PasswordManager passwordManager;
    @Autowired
    private TokenStore tokenStore;
    @Autowired
    AgentService agentService;
    /**
     * 所有用户列表
     */
    @GetMapping("/page")
    @PreAuthorize("@pms.hasPermission('sys:user:page')")
    @ApiOperation("用户列表")
    public ResponseEntity<IPage<SysUser>> page(String username, PageParam<SysUser> page) {
        IPage<SysUser> sysUserPage = sysUserService.page(page, new LambdaQueryWrapper<SysUser>()
                .like(StrUtil.isNotBlank(username), SysUser::getUsername, username).orderByDesc(SysUser::getCreateTime));
        Map<Long, SysRole> sysRoleMap = sysRoleService.list().stream().collect(Collectors.toMap(SysRole::getRoleId, SysRole -> SysRole));
        for (SysUser sysUser : sysUserPage.getRecords()) {
            List<Long> roleIds = sysRoleService.listRoleIdByUserId(sysUser.getUserId());
            List<String> roleNames = new ArrayList<>();
            if (sysUser.getUsername().equals("admin")){
                roleNames.add("超级管理员");
            }
            roleIds.forEach(rid -> {
                if (sysRoleMap.containsKey(rid)) {
                    roleNames.add(sysRoleMap.get(rid).getRoleName());
                }
            });
            sysUser.setRoleName(roleNames);
        }
        return ResponseEntity.ok(sysUserPage);
    }

    /**
     * 获取登录的用户信息
     */
    @GetMapping("/info")
    @ApiOperation("获取登录的用户信息")
    public ResponseEntity<SysUserInfoDto> info() {
        SysUser sysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        SysUserInfoDto sysUserInfoDto = new SysUserInfoDto();
        BeanUtils.copyProperties(sysUser, sysUserInfoDto);
        List<Long> roleIds = sysRoleService.listRoleIdByUserId(sysUser.getUserId());
        Map<Long, SysRole> sysRoleMap = sysRoleService.list().stream().collect(Collectors.toMap(SysRole::getRoleId, SysRole -> SysRole));
        List<String> roleNames = new ArrayList<>();
        if (sysUser.getUsername().equals("admin")){
            roleNames.add("超级管理员");
        }
        roleIds.forEach(rid -> {
            if (sysRoleMap.containsKey(rid)) {
                roleNames.add(sysRoleMap.get(rid).getRoleName());
            }
        });
        sysUserInfoDto.setRoleName(roleNames);
        return ResponseEntity.ok(sysUserInfoDto);
    }

    /**
     * 获取代理商的用户信息
     */
    @GetMapping("/infoConfig")
    @ApiOperation("获取代理商的用户信息")
    public ResponseEntity<AgentDto> infoConfig() {
        SysUser sysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
            Page<AgentDto> page = new Page(1, 5);
            page = agentService.listTotal(page, sysUser.getUsername());
            List<AgentDto> list = page.getRecords();
            AgentDto agentDto = new AgentDto();
            if (list.size() >0) {
                String userCode = list.get(0).getUserCode();
                String userName = list.get(0).getUserName();
                String id = list.get(0).getId();
                String shareUrl ="https://h5.skyrimc.com/syn/#/register?=";
                agentDto.setShareUrl(shareUrl + userCode);
                agentDto.setUserCode(userCode);
                agentDto.setRoleName(userName);
                agentDto.setId(id);
            }
        if(list.size() ==0 || list.size() <0){
            agentDto.setFlag("1");
            log.info("不是代理 {} ", list);
        }
        return ResponseEntity.ok(agentDto);
    }

    /**
     * 修改密码
     */
    @SysLog("修改密码")
    @PostMapping("/password")
    @ApiOperation(value = "修改密码")
    public ResponseEntity<String> password(@RequestBody @Valid UpdatePasswordDto param) {
        // 开源版代码，禁止用户修改admin 的账号密码
        // 正式使用时，删除此部分代码即可
        if (Objects.equals(1L, param.getId()) && StrUtil.isNotBlank(param.getNewPassword())) {
            throw new YamiShopBindException("禁止修改admin的账号密码");
        }
        SysUser sysUser = sysUserService.getSysUserById(param.getId());
        if (sysUser==null){
            throw  new YamiShopBindException("参数错误!");
        }
        String password = passwordManager.decryptPassword(param.getPassword());
        if (!passwordEncoder.matches(password, sysUser.getPassword())) {
            return ResponseEntity.badRequest().body("原密码不正确");
        }
        //新密码
        String newPassword = passwordEncoder.encode(passwordManager.decryptPassword(param.getNewPassword()));
//		更新密码
        sysUserService.updatePasswordByUserId(sysUser.getUserId(), newPassword);
        tokenStore.deleteAllToken(String.valueOf(SysTypeEnum.ADMIN.value()), String.valueOf(sysUser.getUserId()));
        return ResponseEntity.ok().build();
    }

    /**
     * 修改资金密码
     */
    @SysLog("修改资金密码")
    @PostMapping("/updateSafePassword")
    @ApiOperation(value = "修改资金密码")
    public ResponseEntity<String> updateSafePassword(@RequestBody @Valid UpdateSafePasswordDto param) {
        SysUser sysUser = sysUserService.getSysUserById(param.getId());
        if (sysUser == null) {
            throw new YamiShopBindException("参数错误!");
        }
        String safePassword = passwordManager.decryptPassword(param.getSafePassword());
        sysUser.setSafePassword(passwordEncoder.encode(safePassword));
        sysUserService.updateById(sysUser);
        return ResponseEntity.ok().build();
    }

    /**
     * 绑定谷歌验证码
     */
    @SysLog("绑定谷歌验证码")
    @PostMapping("/bindGoogleAuth")
    @ApiOperation(value = "绑定谷歌验证码")
    public Result<String> updateGoogleAuth(@RequestBody @Valid UpdateGoogleAuthDto param) {
        SysUser sysUser = sysUserService.getSysUserById(param.getId());
        if (sysUser == null) {
            throw new YamiShopBindException("参数错误!");
        }
        SysUser rootSysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        long t = System.currentTimeMillis();
        GoogleAuthenticator ga = new GoogleAuthenticator();
        ga.setWindowSize(5);
        boolean flag = ga.check_code(rootSysUser.getGoogleAuthSecret(), Long.valueOf(param.getRootGoogleAuthCode()), t);
        if (flag) {
            if (sysUser.isGoogleAuthBind()) {
                throw new YamiShopBindException("谷歌验证码已绑定!");
            }
            boolean userFlag = ga.check_code(param.getSecret(), Long.valueOf(param.getGoogleAuthCode()), t);
            if (!userFlag) {
                throw new YamiShopBindException("谷歌验证码错误!");
            }
            sysUser.setGoogleAuthBind(true);
            sysUser.setGoogleAuthSecret(param.getSecret());
            sysUser.setUpdateTime(new Date());
            sysUserService.updateById(sysUser);
        } else {
            throw new YamiShopBindException("超级谷歌验证码错误!");
        }
        return Result.succeed();
    }



    @SysLog("解绑谷歌验证码")
    @PostMapping("/unbindingGoogleAuth")
    @ApiOperation(value = "解绑谷歌验证码")
    public Result unbindingGoogleAuth(@RequestBody @Valid UnbindingGoogleAuthModel param) {
        SysUser sysUser = sysUserService.getSysUserById(param.getId());
        if (sysUser == null) {
            throw new YamiShopBindException("参数错误!");
        }
        SysUser rootSysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        long t = System.currentTimeMillis();
        GoogleAuthenticator ga = new GoogleAuthenticator();
        ga.setWindowSize(5);
        boolean flag = ga.check_code(rootSysUser.getGoogleAuthSecret(), Long.valueOf(param.getRootGoogleAuthCode()), t);
        if (flag) {
            if (!sysUser.isGoogleAuthBind()) {
                throw new YamiShopBindException("谷歌验证码未绑定，无需解绑!");
            }
            sysUser.setGoogleAuthBind(false);
            sysUser.setGoogleAuthSecret("");
            sysUser.setUpdateTime(new Date());
            sysUserService.updateById(sysUser);
        } else {
            throw new YamiShopBindException("超级谷歌验证码错误!");
        }
        return Result.succeed();
    }

    /**
     * 用户信息
     */
    @GetMapping("/info/{userId}")
    @PreAuthorize("@pms.hasPermission('sys:user:info')")
    public ResponseEntity<SysUser> info(@PathVariable("userId") Long userId) {
        SysUser user = sysUserService.getSysUserById(userId);
        user.setUserId(null);
//		if (!Objects.equals(user.getShopId(), SecurityUtils.getSysUser().getShopId())) {
//			throw new YamiShopBindException("没有权限获取该用户信息");
//		}
        //获取用户所属的角色列表
        List<Long> roleIdList = sysRoleService.listRoleIdByUserId(userId);
        user.setRoleIdList(roleIdList);
        return ResponseEntity.ok(user);
    }

    /**
     * 保存用户
     */
    @SysLog("保存用户")
    @PostMapping
    @PreAuthorize("@pms.hasPermission('sys:user:save')")
    @ApiOperation("保存用户")
    public ResponseEntity<String> save(@Valid @RequestBody SysUserDto user) {
        String username = user.getUsername();
        SysUser dbUser = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (dbUser != null) {
            return ResponseEntity.badRequest().body("该用户已存在");
        }
        SysUser sysUser = new SysUser();
        sysUser.setPassword(passwordEncoder.encode(passwordManager.decryptPassword(user.getPassword())));
        sysUser.setRemarks(user.getRemarks());
        sysUser.setSafePassword(passwordEncoder.encode(passwordManager.decryptPassword(user.getSafePassword())));
        sysUser.setEmail(user.getEmail());
        sysUser.setRoleIdList(user.getRoleIdList());
        sysUser.setMobile(user.getMobile());
        sysUser.setUsername(username);
        sysUser.setStatus(user.getStatus());
        sysUserService.saveUserAndUserRole(sysUser);
        return ResponseEntity.ok().build();
    }

    /**
     * 修改用户
     */
    @SysLog("修改用户")
    @PutMapping
    @PreAuthorize("@pms.hasPermission('sys:user:update')")
    @ApiOperation("修改用户")
    public ResponseEntity<String> update(@Valid @RequestBody UpdateSysUserDto dto) {
        SysUser dbUser = sysUserService.getSysUserById(dto.getId());
        if (dbUser == null) {
            throw new YamiShopBindException("参数错误!");
        }
//		SysUser dbUserNameInfo = sysUserService.getByUserName(dto.getUsername());
//		if (dbUserNameInfo != null && !Objects.equals(dbUserNameInfo.getUserId(),dto.getUserId())) {
//			return ResponseEntity.badRequest().body("该用户已存在");
//		}
        // 开源版代码，禁止用户修改admin 的账号密码密码
        // 正式使用时，删除此部分代码即可
        boolean is = Objects.equals(1L, dbUser.getUserId()) && !StrUtil.equals("admin", dbUser.getUsername());
        if (is) {
            throw new YamiShopBindException("禁止修改admin的账号密码");
        }
        if (Objects.equals(1L, dbUser.getUserId()) && dbUser.getStatus() == 0) {
            throw new YamiShopBindException("admin用户不可以被禁用");
        }
        dbUser.setRemarks(dto.getRemarks());
        dbUser.setEmail(dto.getEmail());
        dbUser.setRoleIdList(dto.getRoleIdList());
        dbUser.setStatus(dto.getStatus());
        dbUser.setMobile(dto.getMobile());
        dbUser.setRemarks(dto.getRemarks());
        sysUserService.updateUserAndUserRole(dbUser);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除用户
     */
    @SysLog("删除用户")
    @DeleteMapping
    @PreAuthorize("@pms.hasPermission('sys:user:delete')")
    public ResponseEntity<String> delete(@RequestBody Long[] userIds) {
        if (userIds.length == 0) {
            return ResponseEntity.badRequest().body("请选择需要删除的用户");
        }
        if (ArrayUtil.contains(userIds, Constant.SUPER_ADMIN_ID)) {
            return ResponseEntity.badRequest().body("系统管理员不能删除");
        }
        if (ArrayUtil.contains(userIds, SecurityUtils.getSysUser().getUserId())) {
            return ResponseEntity.badRequest().body("当前用户不能删除");
        }
        sysUserService.deleteBatch(userIds, SecurityUtils.getSysUser().getShopId());
        return ResponseEntity.ok().build();
    }
}
