package com.yami.trading.service.cms;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.cms.Cms;
import com.yami.trading.bean.cms.News;
import com.yami.trading.bean.cms.dto.NewsDto;

public interface NewsSerivce  extends IService<News> {


    Page<NewsDto> pageNews(Page page,String title,String language,String userCode);

    public News getIndex(String language);

}
