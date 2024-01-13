package com.yami.trading.admin.task.cms;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yami.trading.admin.facade.MachineTranslationService;
import com.yami.trading.bean.item.domain.ItemSummary;
import com.yami.trading.common.config.ThreadPoolComponent;
import com.yami.trading.service.item.ItemSummaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ItemSummaryTranslate {
    @Autowired
    private ThreadPoolComponent threadPoolComponent;

    @Autowired
    private MachineTranslationService translationService;
    @Autowired
    private ItemSummaryService itemSummaryService;
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\u4e00-\u9fa5]");

    public ItemSummary translateChineseFields(ItemSummary item) throws IllegalAccessException {
        if (item == null) {
            throw new IllegalArgumentException("ItemSummary cannot be null");
        }

        // 遍历类中的所有字段
        for (Field field : ItemSummary.class.getDeclaredFields()) {
            field.setAccessible(true);  // 使得private字段可以访问
            Object fieldValue = field.get(item);  // 获取字段的值
            if(fieldValue == null){
                continue;
            }
            // 只处理字符串类型的字段
            if (fieldValue instanceof String) {
                String value = (String) fieldValue;
                // 如果字段不为空并且含有中文字符，则进行翻译
                if (CHINESE_PATTERN.matcher(value).find()) {
                    String translated = translationService.translate(value);  // 调用TranslateAPI进行翻译
                    field.set(item, translated);  // 将翻译后的结果设置回字段
                }
            }
        }
        return BeanUtil.copyProperties(item, ItemSummary.class, "lang", "uuid");

    }

    @Scheduled(cron = "0 */5 * ? * *")
    public void translate() {
        QueryWrapper<ItemSummary> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNull("translate");
        queryWrapper.eq("lang", "zh-CN");
        List<ItemSummary> list = itemSummaryService.list(queryWrapper);
        for (ItemSummary itemSummary : list) {

            threadPoolComponent.getExecutor().execute(() -> {
                try {
                    ItemSummary itemSummaryEn = translateChineseFields(itemSummary);
                    itemSummaryEn.setSymbol(itemSummary.getSymbol());
                    itemSummaryEn.setUuid(null);
                    itemSummaryEn.setLang("en");
                    itemSummary.setTranslate("1");
                    itemSummaryService.save(itemSummaryEn);
                    itemSummaryService.updateById(itemSummary);
                } catch (Exception e) {
                    log.error("翻译简况失败: {}", itemSummary.getSymbol());
                }

            });

        }

    }
}
