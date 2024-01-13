package com.yami.trading.service.cms.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.cms.Cms;
import com.yami.trading.bean.cms.News;
import com.yami.trading.bean.cms.dto.NewsDto;
import com.yami.trading.dao.cms.CmsMapper;
import com.yami.trading.dao.cms.NewsMapper;
import com.yami.trading.service.cms.CmsService;
import com.yami.trading.service.cms.NewsSerivce;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class NewsSerivceImpl extends ServiceImpl<NewsMapper, News> implements NewsSerivce {
    @Override
    public Page<NewsDto> pageNews(Page page,String title,String language,String userCode) {
        return baseMapper.pageNews(page,title,language,userCode);
    }

    @Override
    public News getIndex(String language) {
        List<News> list  = list(Wrappers.<News>query().lambda().eq(News::getLanguage,language));

        if (!CollectionUtil.isEmpty(list)) {

            Date date = new Date();

            for (News news : list) {

                if ((null != news.getStartTime() && date.before(news.getStartTime()))
                        || (null != news.getEndTime() && news.getEndTime().before(date))) {
                    continue;
                }

                if (news.isIndexTop() && !news.isPopUp()) {
                    list.add(news);
                }
            }
        }

        if (CollectionUtils.isEmpty(list))
            return null;
        return list.get(0);

    }
}
