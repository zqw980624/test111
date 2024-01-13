package com.yami.trading.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.cms.News;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.AwsS3OSSFileService;
import com.yami.trading.service.cms.NewsSerivce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
public class ApiNewsController {
    @Autowired
    private NewsSerivce newsService;
    private final String action = "/api/news!";

    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;

    /**
     * 获取 新闻
     */
    @RequestMapping(action + "get.action")
    public Object get(HttpServletRequest request) {

        String id = request.getParameter("id");
        String index = request.getParameter("index");
        String language = request.getParameter("language");
        if (StringUtils.isEmptyString(index)) {
            index = "false";
        }
        boolean index_bool = Boolean.valueOf(index).booleanValue();
        News news = new News();
        if (index_bool) {
            news = newsService.getIndex(language);
        } else {
            news = this.newsService.getById(id);
        }
        if (news != null) {
            news.setCreateTimeStr(DateUtils.format(news.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));

        }
        if (StringUtils.isNotEmpty(news.getImgUrl())) {
            news.setImgUrl(awsS3OSSFileService.getUrl(news.getImgUrl()));
        }

        return Result.succeed(news);
    }

    /**
     * 获取 新闻 列表
     */
    @RequestMapping(action + "list.action")
    public Object list(HttpServletRequest request) {

        String page_no = request.getParameter("page_no");
        String language = request.getParameter("language");
        if (StringUtils.isNullOrEmpty(page_no)) {
            page_no = "1";
        }
        if (!StringUtils.isInteger(page_no)) {
            throw new YamiShopBindException("页码不是整数");
        }
        if (Integer.valueOf(page_no).intValue() <= 0) {
            throw new YamiShopBindException("页码不能小于等于0");
        }
        int page_no_int = Integer.valueOf(page_no).intValue();
        Page<News> page = new Page<>(1, 1000000);
//			List<News> list = this.newsService.cachePagedQuery(page_no_int, 20, language).getElements();
        LambdaQueryWrapper<News> lambdaQueryWrapper = Wrappers.<News>query().lambda().eq(News::getLanguage, language)
                .eq(News::isPopUp,false);
        lambdaQueryWrapper.orderByDesc(News::getCreateTime);
        newsService.page(page, lambdaQueryWrapper);
        List<News> list = page.getRecords();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setCreateTimeStr(DateUtils.format(list.get(i).getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
                News news = list.get(i);
                if (news.getStartTime() != null && news.getEndTime() != null) {
                    long startTime = news.getStartTime().getTime();
                    long endTime = news.getEndTime().getTime();
                    long curTime = System.currentTimeMillis();
                    if (curTime > startTime && curTime < endTime) {
                        news.setShow(true);
                    }
                }
                if (StringUtils.isNotEmpty(news.getUserId())){
                    news.setShow(news.getUserId().equals(SecurityUtils.getCurrentUserId()));
                }
                if (StringUtils.isNotEmpty(news.getImgUrl())) {
                    news.setImgUrl(awsS3OSSFileService.getUrl(news.getImgUrl()));
                }

            }
        }
        return Result.succeed(list);
    }

    /**
     * 获取 新闻 列表 版本2
     */
    @RequestMapping(action + "list_v2.action")
    public Object list_v2(HttpServletRequest request) {

        String page_no = request.getParameter("page_no");
        String language = request.getParameter("language");
        String token = request.getParameter("token");
        if (StringUtils.isNullOrEmpty(page_no)) {
            page_no = "1";
        }
        if (!StringUtils.isInteger(page_no)) {
            throw new YamiShopBindException("页码不是整数");
        }
        if (Integer.valueOf(page_no).intValue() <= 0) {
            throw new YamiShopBindException("页码不能小于等于0");
        }
        int page_no_int = Integer.valueOf(page_no).intValue();
        List<News> list = new ArrayList<News>();
        if (StringUtils.isEmptyString(token)) {
            LambdaQueryWrapper<News> lambdaQueryWrapper = Wrappers.<News>query().lambda().eq(News::getLanguage, language).eq(News::isPopUp,
                    false);
            Page<News> page = new Page<>(1, 1000000);
            newsService.page(page, lambdaQueryWrapper);
            list = page.getRecords();
        } else {
            String partyId = SecurityUtils.getUser().getUserId();
            LambdaQueryWrapper<News> lambdaQueryWrapper = Wrappers.<News>query().lambda().eq(News::getLanguage, language).eq(News::isPopUp,
                    false).eq(News::getUserId, partyId);
            Page<News> page = new Page<>(1, 1000000);
            newsService.page(page, lambdaQueryWrapper);
            list = page.getRecords();
        }
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setCreateTimeStr(DateUtils.format(list.get(i).getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
                News news = list.get(i);
                if (StringUtils.isNotEmpty(news.getImgUrl())) {
                    news.setImgUrl(awsS3OSSFileService.getUrl(news.getImgUrl()));
                }

            }
        }
        return Result.succeed(list);
    }

    /**
     * 获取 弹出新闻 列表 版本2
     */
    @RequestMapping(action + "list_v2_popup.action")
    public Object list_v2_popup(HttpServletRequest request) {

        String page_no = request.getParameter("page_no");
        String language = request.getParameter("language");
        String token = request.getParameter("token");
        if (StringUtils.isNullOrEmpty(page_no)) {
            page_no = "1";
        }
        if (!StringUtils.isInteger(page_no)) {
            throw new YamiShopBindException("页码不是整数");
        }
        if (Integer.valueOf(page_no).intValue() <= 0) {
            throw new YamiShopBindException("页码不能小于等于0");
        }
        int page_no_int = Integer.valueOf(page_no).intValue();
        List<News> list = new ArrayList<News>();
        if (StringUtils.isEmptyString(token)) {
            LambdaQueryWrapper<News> lambdaQueryWrapper = Wrappers.<News>query().lambda().eq(News::getLanguage, language).eq(News::isPopUp,
                    true);
            Page<News> page = new Page<>(1, 1000000);
            newsService.page(page, lambdaQueryWrapper);
            list = page.getRecords();
        } else {
            String partyId = SecurityUtils.getUser().getUserId();
            LambdaQueryWrapper<News> lambdaQueryWrapper = Wrappers.<News>query().lambda().eq(News::getLanguage, language).eq(News::isPopUp,
                    true).eq(News::getUserId, partyId);
            Page<News> page = new Page<>(1, 1000000);
            newsService.page(page, lambdaQueryWrapper);
            list = page.getRecords();
        }
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setCreateTimeStr(DateUtils.format(list.get(i).getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
                News news = list.get(i);
                if (StringUtils.isNotEmpty(news.getImgUrl())) {
                    news.setImgUrl(awsS3OSSFileService.getUrl(news.getImgUrl()));
                }

            }
        }
        return Result.succeed(list);
    }

}
