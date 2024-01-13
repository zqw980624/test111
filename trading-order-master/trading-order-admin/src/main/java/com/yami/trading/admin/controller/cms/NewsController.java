package com.yami.trading.admin.controller.cms;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.controller.cms.model.DeleteModel;
import com.yami.trading.admin.model.*;
import com.yami.trading.bean.cms.News;
import com.yami.trading.bean.cms.dto.NewsDto;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.security.common.manager.PasswordManager;
import com.yami.trading.service.AwsS3OSSFileService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.service.cms.NewsSerivce;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("news")
@Api(tags = "新闻管理")
public class NewsController  {

    @Autowired
    NewsSerivce newsSerivce;

    @Autowired
    PasswordManager passwordManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    SysUserService sysUserService;

    @Autowired
    UserService userService;

    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;

    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page<NewsDto>> list(@RequestBody @Valid NewsListModel request){
        Page<NewsDto> page=new Page(request.getCurrent(),request.getSize());
        newsSerivce.pageNews(page,request.getTitle(),request.getLanguage(),request.getUserCode());
        for (NewsDto news:page.getRecords()){
            news.setLanguageText(Constants.LANGUAGE.get(news.getLanguage()));
            String nUrl = awsS3OSSFileService.getUrl(news.getImgUrl());
            news.setHttpImgUrl(nUrl);
        }
        return  Result.ok(page);
    }

    @ApiOperation(value = "新增")
    @PostMapping("add")
    public  Result<?> add(@RequestBody @Valid NewsModel model){
        model.setLoginSafeword(passwordManager.decryptPassword(model.getLoginSafeword()));
        SysUser sysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        if (!passwordEncoder.matches(model.getLoginSafeword(), sysUser.getSafePassword())) {
            throw new YamiShopBindException("资金密码不正确!");
        }
        News news=new News();
        if (!StringUtils.isEmptyString(model.getUserCode())) {
            User user= userService.findUserByUserCode(model.getUserCode());
            if (user==null){
                throw  new YamiShopBindException("UID不存在");
            }
            news.setUserId(user.getUserId());
        } else {
            news.setUserId("");
        }
        news.setTitle(model.getTitle());
        news.setImgJumpUrl(model.getImgJumpUrl());
        news.setImgUrl(model.getImgUrl());
        news.setClick(model.isClick());
        news.setPopUp(model.isPopUp());
        news.setIndexTop(model.isIndex());
        news.setLanguage(model.getLanguage());
        news.setStartTime(model.getStartTime());
        news.setEndTime(model.getEndTime());
        news.setContent(model.getContent());
        newsSerivce.save(news);
        return  Result.ok(null);
    }

    @ApiOperation(value = "更新")
    @PostMapping("update")
    public  Result<?> update(@RequestBody @Valid NewsModel model){
        model.setLoginSafeword(passwordManager.decryptPassword(model.getLoginSafeword()));
        SysUser sysUser = sysUserService.getSysUserById(SecurityUtils.getSysUser().getUserId());
        if (!passwordEncoder.matches(model.getLoginSafeword(), sysUser.getSafePassword())) {
            throw new YamiShopBindException("资金密码不正确!");
        }
        News news=newsSerivce.getById(model.getId());
        if (news==null){
            throw  new YamiShopBindException("参数错误!");
        }

        if (!StringUtils.isEmptyString(model.getUserCode())) {
            User user= userService.findUserByUserCode(model.getUserCode());
            if (user==null){
                throw  new YamiShopBindException("UID不存在");
            }
            news.setUserId(user.getUserId());
        } else {
            news.setUserId("");
        }
        news.setTitle(model.getTitle());
        news.setImgJumpUrl(model.getImgJumpUrl());
        news.setClick(model.isClick());
        news.setPopUp(model.isPopUp());
        news.setImgUrl(model.getImgUrl());
        news.setIndexTop(model.isIndex());
        news.setLanguage(model.getLanguage());
        news.setStartTime(model.getStartTime());
        news.setEndTime(model.getEndTime());
        news.setContent(model.getContent());
        newsSerivce.updateById(news);
        return  Result.ok(null);
    }

    @ApiOperation(value = "获取语言")
    @GetMapping("getLanguage")
    public  Result<?> getLanguage(){
        return  Result.ok(Constants.LANGUAGE);
    }



    @ApiOperation(value = "删除")
    @PostMapping("delete")
    public  Result<?> delete(@RequestBody @Valid DeleteModel model){
        sysUserService.checkSafeWord(model.getLoginSafeword());
        newsSerivce.removeById(model.getId());
        return  Result.ok(null);
    }
}
