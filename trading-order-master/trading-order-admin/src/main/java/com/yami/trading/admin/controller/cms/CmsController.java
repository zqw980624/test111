package com.yami.trading.admin.controller.cms;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.model.CmsListModel;
import com.yami.trading.admin.model.CmsModel;
import com.yami.trading.admin.model.IdModel;
import com.yami.trading.bean.cms.Cms;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.security.common.manager.PasswordManager;
import com.yami.trading.service.cms.CmsService;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("cms")
@Api(tags = "用户端内容管理")
public class CmsController {
    @Autowired
    CmsService cmsService;
    @Autowired
    PasswordManager passwordManager;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    SysUserService sysUserService;

    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page> list(@RequestBody @Valid CmsListModel request) {
        Page<Cms> page = new Page(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<Cms> lambdaQueryWrapper = Wrappers.<Cms>query().lambda();
        if (!StrUtil.isEmpty(request.getTitle())) {
            lambdaQueryWrapper.like(Cms::getTitle, request.getTitle());
        }
        if (!StrUtil.isEmpty(request.getContentCode())) {
            lambdaQueryWrapper.like(Cms::getContentCode, request.getContentCode());
        }
        if (!StrUtil.isEmpty(request.getLanguage())) {
            lambdaQueryWrapper.like(Cms::getLanguage, request.getLanguage());
        }
        lambdaQueryWrapper.orderByDesc(Cms::getCreateTime);
        cmsService.page(page, lambdaQueryWrapper);
        for (Cms cms:page.getRecords()){
            cms.setLanguageText(Constants.LANGUAGE.get(cms.getLanguage()));
        }
        return Result.ok(page);
    }

    @ApiOperation(value = "新增")
    @PostMapping("add")
    public Result<?> add(@RequestBody @Valid CmsModel model) {
        model.setLoginSafeword(passwordManager.decryptPassword(model.getLoginSafeword()));
        SysUser sysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        if (!passwordEncoder.matches(model.getLoginSafeword(), sysUser.getSafePassword())) {
            throw new YamiShopBindException("资金密码不正确!");
        }
        Cms cms = new Cms();
        cms.setContent(model.getContent());
        cms.setLanguage(model.getLanguage());
        cms.setModel(model.getModel());
        cms.setTitle(model.getTitle());
        cms.setContentCode(model.getContentCode());
        cmsService.save(cms);
        return Result.ok(null);
    }

    @ApiOperation("用户端内容管理模块")
    @GetMapping("getCmsModel")
    public  Result getCmsModel(){
        return  Result.ok( Constants.CMS_MODEL);
    }

    @ApiOperation(value = "更新")
    @PostMapping("update")
    public Result<?> update(@RequestBody @Valid CmsModel model) {
        model.setLoginSafeword(passwordManager.decryptPassword(model.getLoginSafeword()));
        SysUser sysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        if (!passwordEncoder.matches(model.getLoginSafeword(), sysUser.getSafePassword())) {
            throw new YamiShopBindException("资金密码不正确!");
        }
        Cms cms = cmsService.getById(model.getId());
        if (cms == null) {
            throw new YamiShopBindException("参数错误!");
        }
        cms.setContent(model.getContent());
        cms.setLanguage(model.getLanguage());
        cms.setModel(model.getModel());
        cms.setTitle(model.getTitle());
        cms.setContentCode(model.getContentCode());
        cmsService.updateById(cms);
        return Result.ok(null);
    }

    @ApiOperation(value = "删除")
    @PostMapping("delete")
    public Result<?> delete(@RequestBody @Valid IdModel model) {
        cmsService.removeById(model.getId());
        return Result.ok(null);
    }
}
