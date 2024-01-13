package com.yami.trading.service.cms;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.cms.Cms;

import java.util.List;

public interface CmsService    extends IService<Cms> {
    List<Cms> getModelAndLanguage(String model, String language);

    Cms getContentCodeAndLanguage(String content_code, String language);
}
