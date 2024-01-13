package com.yami.trading.api.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yami.trading.bean.cms.Banner;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.service.cms.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 横幅管理
 */
@RestController
@CrossOrigin
public class ApiBannerController {
    @Autowired
    private BannerService bannerService;
    private final String action = "/api/banner!";

    /**
     * 获取 横幅
     * <p>
     * language 语言
     * content_code 业务代码，同种内容不同语言下的code相同
     */
    @RequestMapping(action + "get.action")
    public Object get(HttpServletRequest request) {
        String language = request.getParameter("language");
        String content_code = request.getParameter("content_code");
        Banner banner = this.bannerService.getByCodeAndLanguage(content_code, language);
        return Result.succeed(bannerService.bindOne(banner));
    }
    /**
     * 获取 横幅
     * <p>
     * language 语言
     * content_code 业务代码，同种内容不同语言下的code相同
     */
   /* @RequestMapping(action + "gets.action")
    public Object gets(HttpServletRequest request) {
        String language = request.getParameter("language");
        String content_code = request.getParameter("content_code");
        Banner banner = this.bannerService.getByCodeAndLanguage(content_code, language);

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


        return Result.succeed(bannerService.bindOne(banner));
    }*/
    /**
     * 获取 横幅 列表
     */
    @RequestMapping(action + "list.action")
    public Object list(HttpServletRequest request) {
       // String model = request.getParameter("model");
        //String language = request.getParameter("language");
            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            List<Banner> cacheListByModel = this.bannerService.listByModelAndLanguage("top", "en");
            for (Banner banner : cacheListByModel) {
                if (banner.getOnShow()==1){
                    result.add(this.bannerService.bindOne(banner));
                }
            }
          return Result.succeed(result);
    }
}
