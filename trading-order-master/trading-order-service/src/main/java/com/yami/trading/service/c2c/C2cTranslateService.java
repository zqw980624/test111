package com.yami.trading.service.c2c;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.c2c.C2cOrder;
import com.yami.trading.bean.model.C2cPaymentMethod;
import com.yami.trading.bean.model.C2cTranslate;

import java.util.List;

public interface C2cTranslateService  extends IService<C2cTranslate> {

    public C2cTranslate get(String content, String language);

    public void saveC2cTranslate(C2cTranslate entity);

    public void update(C2cTranslate entity);

    public void delete(String content, String language);

    public void saveTranslate(String content, String langTransStr);

    public String getTranslate(String content);

    public C2cPaymentMethod translatePm(C2cPaymentMethod cpm, String language);

    public List<String> getAllPaymentMethodTypeName();

    C2cOrder translateOrder(C2cOrder c2cOrder, String language);
}
