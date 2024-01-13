package com.yami.trading.service.cms.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.cms.Cms;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.dao.cms.CmsMapper;
import com.yami.trading.service.cms.CmsService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class CmsServiceImpl  extends ServiceImpl<CmsMapper, Cms>  implements CmsService {
    @Override
    public List<Cms> getModelAndLanguage(String model, String language) {
        return list(Wrappers.<Cms>query().lambda().eq(Cms::getLanguage,language).eq(Cms::getModel,model));
    }

    @Override
    public Cms getContentCodeAndLanguage(String content_code, String language) {
       List<Cms> list=   list(Wrappers.<Cms>query().lambda().eq(Cms::getContentCode,content_code).eq(Cms::getLanguage,language));
        if (!CollectionUtil.isEmpty(list)){
            return list.get(0);
        }
        return null;
    }
}
