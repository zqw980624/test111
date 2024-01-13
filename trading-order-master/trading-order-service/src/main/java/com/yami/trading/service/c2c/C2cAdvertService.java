package com.yami.trading.service.c2c;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.c2c.C2cAdvert;

import java.util.List;
import java.util.Map;

public interface C2cAdvertService extends IService<C2cAdvert> {
    Map<String, String> getC2cSyspara(String c2c_payment_method_type);


    /*
     * 获取 语种说明
     */
    public String getLanguageIntro();

    /**
     * 获取所有上架币种单价
     */
    public Map<String, String> getAllSymbolPrice(String currency);

    /*
     * 获取 支付方式类型说明
     */
    public String getMethodTypeIntro();

    /**
     * 获取 支付币种Map
     */
    public Map<String, String> getCurrencyMap();
    public Page pagedQuery(int page_no, int page_size, String c2c_user_id, String direction, String currency, String symbol, String amount, Integer on_sale, Integer closed, boolean is_c2c_user);

    /**
     * 获取 上架币种Map
     */
    public Map<String, String> getSymbolMap();
    public Map<String, Object> getComputeValue(double all_deposit, String currency, String symbol, double coin_amount, double symbol_value);
    public List<C2cAdvert> getByC2cUserId(String c2c_user_id);
    public Page pagedQuery(long pageNo, long pageSize, String c2cUserCode, String c2cUserType,String userCode, String direction,String currency,String symbol);
}
