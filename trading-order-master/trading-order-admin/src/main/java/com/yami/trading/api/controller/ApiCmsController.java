package com.yami.trading.api.controller;

import com.yami.trading.bean.cms.Cms;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.service.cms.CmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户端内容管理
 */
@RestController
public class ApiCmsController {
    @Autowired
    private CmsService cmsService;
    private final String action = "/api/cms!";

    /**
     * 获取 用户端内容管理
     */
    @RequestMapping(action + "get.action")
    public Object get(HttpServletRequest request) {
        // 业务代码， 同种内容 不同语言下的code相同
        String content_code = request.getParameter("content_code");
        String language = request.getParameter("language");
        Cms cms = cmsService.getContentCodeAndLanguage(content_code, language);
        return Result.succeed(cms);
    }

    /**
     * 获取 用户端内容管理 列表
     */
    @RequestMapping(action + "list.action")
    public Object list(HttpServletRequest request) {
        String model = request.getParameter("model");
        String language = request.getParameter("language");
        List<Cms> cacheListByModel = this.cmsService.getModelAndLanguage(model, language);
        for (Cms cms : cacheListByModel) {
            cms.setCreateTimeStr(DateUtils.format(cms.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
        }
        return Result.succeed(cacheListByModel);
    }
}
