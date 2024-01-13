package com.yami.trading.service.cms;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.cms.Banner;
import com.yami.trading.bean.cms.Cms;

import java.util.List;
import java.util.Map;

public interface BannerService extends IService<Banner> {
    Banner getByCodeAndLanguage(String content_code, String language);

    public Map<String, Object> bindOne(Banner entity);

    List<Banner> listByModelAndLanguage(String model, String language);
}
