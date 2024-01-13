package com.yami.trading.dao.cms;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.cms.Cms;
import com.yami.trading.bean.cms.News;
import com.yami.trading.bean.cms.dto.NewsDto;
import org.apache.ibatis.annotations.Param;

public interface NewsMapper extends BaseMapper<News> {

    Page<NewsDto> pageNews(Page page, @Param("title") String title, @Param("language") String language,
                           @Param("userCode") String userCode);
}