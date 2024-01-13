package com.yami.trading.service.c2c.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.yami.trading.bean.c2c.C2cOrder;
import com.yami.trading.bean.model.C2cPaymentMethod;
import com.yami.trading.bean.model.C2cTranslate;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.constants.RedisKeys;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.dao.c2c.C2cTranslateMapper;
import com.yami.trading.service.c2c.C2cAdvertService;
import com.yami.trading.service.c2c.C2cTranslateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class C2cTranslateServiceImpl extends ServiceImpl<C2cTranslateMapper,C2cTranslate>  implements C2cTranslateService {


    @Autowired
    private RedisTemplate  redisTemplate;

    @Autowired
    private C2cAdvertService c2cAdvertService;

    public C2cTranslate get(String content, String language) {
     Object  json= redisTemplate.opsForValue().get(RedisKeys.C2C_TRANSLATE_CONTENT_LANGUAGE + StringUtils.stringToUnicode(content) + language);
     if (json==null){
         return null;
     }
        return new Gson().fromJson(json.toString(),C2cTranslate.class) ;
    }

    @Override
    public void saveC2cTranslate(C2cTranslate entity) {
        save(entity);
        redisTemplate.opsForValue().set(RedisKeys.C2C_TRANSLATE_CONTENT_LANGUAGE + StringUtils.stringToUnicode(entity.getContent()) + entity.getLanguage(), new Gson().toJson(entity));
    }

    public void update(C2cTranslate entity) {
        updateById(entity);
        if (null != entity) {
            redisTemplate.opsForValue().set(RedisKeys.C2C_TRANSLATE_CONTENT_LANGUAGE + StringUtils.stringToUnicode(entity.getContent()) + entity.getLanguage(), new Gson().toJson(entity));
        }
    }

    public void delete(String content, String language) {
        C2cTranslate entity = this.get(content, language);
        if (entity != null) {
           removeById(entity);
//			this.redisHandler.remove(RedisKeys.C2C_TRANSLATE_ID + entity.getId().toString());
            redisTemplate.delete(RedisKeys.C2C_TRANSLATE_CONTENT_LANGUAGE + StringUtils.stringToUnicode(entity.getContent()) + entity.getLanguage());
        }
    }

    public void saveTranslate(String content, String langTransStr) {

        if (StringUtils.isNotEmpty(langTransStr)) {

            String[] ltStrArr = langTransStr.split("&&");
            for (int i = 0; i < ltStrArr.length; i++) {

                String ltStr = ltStrArr[i];
                if (StringUtils.isNotEmpty(ltStr)) {

                    String[] lt = ltStr.split("##");
                    String language = lt[0];
                    String translate = lt[1];

                    if (StringUtils.isNotEmpty(language) && StringUtils.isNotEmpty(translate)) {

                        C2cTranslate trans = this.get(content, language);
                        if (null == trans) {

                            C2cTranslate ct = new C2cTranslate();
                            ct.setLanguage(language);
                            ct.setContent(content);
                            ct.setTranslate(translate);
                            ct.setCreateTime(new Date());
                            ct.setUpdateTime(new Date());
                            saveC2cTranslate(ct);
                        } else {
                            trans.setTranslate(translate);
                            trans.setUpdateTime(new Date());
                            update(trans);
                        }
                    }
                }
            }
        }
    }

    public String getTranslate(String content) {

        String langTransStr = "";

        Map<String, String> langMap = Constants.LANGUAGE;
        for (String lang : langMap.keySet()) {
            C2cTranslate trans = this.get(content, lang);
            if (StringUtils.isEmptyString(langTransStr)) {
                if (null != trans) {
                    langTransStr = lang + "##" + trans.getTranslate();
                }
            } else {
                if (null != trans) {
                    langTransStr += "&&" + lang + "##" + trans.getTranslate();
                }
            }
        }

        return langTransStr;
    }

    public C2cPaymentMethod translatePm(C2cPaymentMethod cpm, String language) {

        Map<String, String> data = this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type");
        String name = data.get(String.valueOf(cpm.getMethodType()));
        if (null != name) {
            C2cTranslate trans = this.get(name, language);
            if (null != trans) {
                cpm.setMethodTypeName(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(cpm.getMethodName())) {
            C2cTranslate trans = this.get(cpm.getMethodName(), language);
            if (null != trans) {
                cpm.setMethodName(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(cpm.getParamName1())) {
            C2cTranslate trans = this.get(cpm.getParamName1(), language);
            if (null != trans) {
                cpm.setParamName1(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(cpm.getParamName2())) {
            C2cTranslate trans = this.get(cpm.getParamName2(), language);
            if (null != trans) {
                cpm.setParamName2(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(cpm.getParamName3())) {
            C2cTranslate trans = this.get(cpm.getParamName3(), language);
            if (null != trans) {
                cpm.setParamName3(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(cpm.getParamName4())) {
            C2cTranslate trans = this.get(cpm.getParamName4(), language);
            if (null != trans) {
                cpm.setParamName4(trans.getTranslate());
            }
        }



        return cpm;
    }


    /*
     * 获取 支付方式类型名称列表
     */
    public List<String> getAllPaymentMethodTypeName() {
        List<String> nameList = new ArrayList<String>();
        Map<String, String> data = this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type");
        for (String id : data.keySet()) {
            String name = data.get(id);
            if (null != name) {
                nameList.add(name);
            }
        }
        return nameList;
    }

    public C2cOrder translateOrder(C2cOrder order, String language) {

        if (StringUtils.isNotEmpty(order.getMethodName())) {
            C2cTranslate trans = this.get(order.getMethodName(), language);
            if (null != trans) {
                order.setMethodName(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(order.getParamName1())) {
            C2cTranslate trans = this.get(order.getParamName1(), language);
            if (null != trans) {
                order.setParamName1(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(order.getParamName2())) {
            C2cTranslate trans = this.get(order.getParamName2(), language);
            if (null != trans) {
                order.setParamName2(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(order.getParamName3())) {
            C2cTranslate trans = this.get(order.getParamName3(), language);
            if (null != trans) {
                order.setParamName3(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(order.getParamName4())) {
            C2cTranslate trans = this.get(order.getParamName4(), language);
            if (null != trans) {
                order.setParamName4(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(order.getParamName5())) {
            C2cTranslate trans = this.get(order.getParamName5(), language);
            if (null != trans) {
                order.setParamName5(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(order.getParamName6())) {
            C2cTranslate trans = this.get(order.getParamName6(), language);
            if (null != trans) {
                order.setParamName6(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(order.getParamName7())) {
            C2cTranslate trans = this.get(order.getParamName7(), language);
            if (null != trans) {
                order.setParamName7(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(order.getParamName8())) {
            C2cTranslate trans = this.get(order.getParamName8(), language);
            if (null != trans) {
                order.setParamName8(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(order.getParamName9())) {
            C2cTranslate trans = this.get(order.getParamName9(), language);
            if (null != trans) {
                order.setParamName9(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(order.getParamName10())) {
            C2cTranslate trans = this.get(order.getParamName10(), language);
            if (null != trans) {
                order.setParamName10(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(order.getParamName11())) {
            C2cTranslate trans = this.get(order.getParamName11(), language);
            if (null != trans) {
                order.setParamName11(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(order.getParamName12())) {
            C2cTranslate trans = this.get(order.getParamName12(), language);
            if (null != trans) {
                order.setParamName12(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(order.getParamName13())) {
            C2cTranslate trans = this.get(order.getParamName13(), language);
            if (null != trans) {
                order.setParamName13(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(order.getParamName14())) {
            C2cTranslate trans = this.get(order.getParamName14(), language);
            if (null != trans) {
                order.setParamName14(trans.getTranslate());
            }
        }

        if (StringUtils.isNotEmpty(order.getParamName15())) {
            C2cTranslate trans = this.get(order.getParamName15(), language);
            if (null != trans) {
                order.setParamName15(trans.getTranslate());
            }
        }

        return order;
    }
}
