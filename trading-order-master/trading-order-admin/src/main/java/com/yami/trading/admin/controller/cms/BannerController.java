package com.yami.trading.admin.controller.cms;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.model.*;
import com.yami.trading.bean.cms.Banner;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.security.common.manager.PasswordManager;
import com.yami.trading.service.AwsS3OSSFileService;
import com.yami.trading.service.cms.BannerService;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("banner")
@Api(tags = "横幅管理")
public class BannerController {
    @Autowired
    BannerService  bannerService;
    @Autowired
    PasswordManager passwordManager;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    SysUserService sysUserService;
    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;

    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page> list(@RequestBody @Valid BannerListModel request) {
        Page<Banner> page = new Page(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<Banner> lambdaQueryWrapper = Wrappers.<Banner>query().lambda();
        if (!StrUtil.isEmpty(request.getLanguage())) {
            lambdaQueryWrapper.eq(Banner::getLanguage, request.getLanguage());
        }
        lambdaQueryWrapper.orderByDesc(Banner::getCreateTime);
        bannerService.page(page, lambdaQueryWrapper);
        for (Banner banner:page.getRecords()){
            banner.setLanguageText(Constants.LANGUAGE.get(banner.getLanguage()));
            banner.setHttpImageUrl(banner.getImage());
        }
        return Result.ok(page);
    }

    @ApiOperation(value = "新增")
    @PostMapping("add")
    public Result<?> add(@RequestBody @Valid BannerModel model) {
        model.setLoginSafeword(passwordManager.decryptPassword(model.getLoginSafeword()));
        SysUser sysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        if (!passwordEncoder.matches(model.getLoginSafeword(), sysUser.getSafePassword())) {
            throw new YamiShopBindException("资金密码不正确!");
        }
        if (StrUtil.isEmpty(model.getImage())){
            throw new YamiShopBindException("展示图片不能为空");
        }
        Banner banner = new Banner();
        banner.setContentCode(model.getContentCode());
        banner.setImage(model.getImage());
        banner.setUrl(model.getUrl());
        banner.setSortIndex(model.getSortIndex());
        banner.setModel(model.getModel());
        banner.setLanguage(model.getLanguage());
        banner.setClick(model.getClick());
        banner.setOnShow(model.getOnShow());
        bannerService.save(banner);
        return Result.ok(null);
    }

    @ApiOperation(value = "更新")
    @PostMapping("update")
    public Result<?> update(@RequestBody @Valid BannerModel model) {
        model.setLoginSafeword(passwordManager.decryptPassword(model.getLoginSafeword()));
        SysUser sysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        if (!passwordEncoder.matches(model.getLoginSafeword(), sysUser.getSafePassword())) {
            throw new YamiShopBindException("资金密码不正确!");
        }
        if (StrUtil.isEmpty(model.getImage())){
            throw new YamiShopBindException("展示图片不能为空");
        }
        Banner banner = bannerService.getById(model.getId());
        if (banner == null) {
            throw new YamiShopBindException("参数错误!");
        }
        banner.setContentCode(model.getContentCode());
        banner.setImage(model.getImage());
        banner.setUrl(model.getUrl());
        banner.setSortIndex(model.getSortIndex());
        banner.setModel(model.getModel());
        banner.setLanguage(model.getLanguage());
        banner.setClick(model.getClick());
        banner.setOnShow(model.getOnShow());
        bannerService.updateById(banner);
        return Result.ok(null);
    }

    @ApiOperation(value = "删除")
    @PostMapping("delete")
    public Result<?> delete(@RequestBody @Valid IdModel model) {
        bannerService.removeById(model.getId());
        return Result.ok(null);
    }
}
