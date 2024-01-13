package com.yami.trading.service.cms.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.cms.Banner;
import com.yami.trading.bean.cms.Cms;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.dao.cms.BannerMapper;
import com.yami.trading.dao.cms.CmsMapper;
import com.yami.trading.service.cms.BannerService;
import com.yami.trading.service.cms.CmsService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BannerServiceImpl  extends ServiceImpl<BannerMapper, Banner> implements BannerService {
    @Override
    public Banner getByCodeAndLanguage(String content_code, String language) {
        return getOne(Wrappers.<Banner>query().lambda()
                .eq(Banner::getContentCode,content_code).eq(Banner::getLanguage,language).eq(Banner::getOnShow,1));
    }

    @Override
    public Map<String, Object> bindOne(Banner entity) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("content_code", entity.getContentCode());
        result.put("image", entity.getImage());
        result.put("index", entity.getSortIndex());
        result.put("language", entity.getLanguage());
        result.put("model", entity.getModel());
        result.put("url", entity.getUrl());
        result.put("click", entity.getClick());
        result.put("create_time", DateUtils.format(entity.getCreateTime(), DateUtils.DF_yyyyMMddHHmm));
        result.put("id", entity.getUuid());

        return result;
    }

    @Override
    public List<Banner> listByModelAndLanguage(String model, String language) {
        return list(Wrappers.<Banner>query().lambda().eq(Banner::getModel,model).eq(Banner::getLanguage,language));
    }
}
