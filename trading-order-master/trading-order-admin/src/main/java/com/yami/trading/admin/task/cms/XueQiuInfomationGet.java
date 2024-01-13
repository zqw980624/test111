package com.yami.trading.admin.task.cms;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yami.trading.admin.facade.MachineTranslationService;
import com.yami.trading.bean.cms.Infomation;
import com.yami.trading.common.config.ThreadPoolComponent;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.dao.cms.InfomationMapper;
import com.yami.trading.service.cms.InfomationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class XueQiuInfomationGet {
    @Autowired
    private ThreadPoolComponent threadPoolComponent;
    @Autowired
    private InfomationService infomationService;

    @Autowired
    private MachineTranslationService translationService;


    @Scheduled(cron = "0 */20 * ? * *")
    public void translate(){
        QueryWrapper<Infomation> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNull("translate");
        queryWrapper.eq("lang", "zh-CN");
        List<Infomation> list = infomationService.list(queryWrapper);
        for(Infomation infomation : list){
            threadPoolComponent.getExecutor().execute(() ->{
                String description = infomation.getDescription();
                String source = infomation.getSource();
                Infomation infomationEn = BeanUtil.copyProperties(infomation, Infomation.class);
                infomationEn.setUuid(null);
                if(StringUtils.isNotEmpty(description)){
                    String translate = translationService.translate(description);
                    if(translate == null){
                        return;
                    }
                    infomationEn.setDescription(translate);
                }
                if(StringUtils.isNotEmpty(source)){
                    String translate = translationService.translate(source);
                    if(translate == null){
                        return;
                    }
                    infomationEn.setSource(translate);
                }
                infomationEn.setLang("en");
                infomationService.save(infomationEn);
                infomation.setTranslate("1");
                infomationService.updateById(infomation);
            });

        }

    }
}
