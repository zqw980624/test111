package com.yami.trading.security.common.adapter;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 授权过滤器的配置
 *
 * @author admin
 */
@Component
public class ResourceServerAdapter extends DefaultAuthConfigAdapter {

    @Value("${spring.profiles.active}")
    private String env;

    public static final List<String> EXCLUDE_PATH = Arrays.asList(
            "/webjars/**",
            "/swagger/**",
            "/v2/api-docs",
            "/doc.html",
            "/favicon.ico",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/captcha/**",
            "/adminLogin",
            "/hobi!**",
            "/websocket/**",
            "/api/captcha/**",
            "/api/login",
            "/api/user/login",
            "/api/user/getImageCode",
            "/api/registerNoVerifcode",
            "/api/user/registerUsername",
            "/api/systemInfoSocketServer",
            "/api/hobi**",
            "/api/normal/**",
            "/api/user/register",
            "/api/uploadFile",
            "/api/api/uploadFile",
            "/api/idcode/execute",
            "/api/item!list.action",
            "/api/item!listYd.action",
            "/api/item!queryBySymbol.action",
            "/api/item!queryBySymbolyd.action",
            "/api/item!relateStocks.action",
            "/api/public/uploadimg/!execute.action",
            "/api/websocket/**",
            "/api/projectBreed/getAll",
            "/api/projectBreed/getConstituentStockList",
            "/api/item/itemSummary/**",
            "/etf/klineConfig/**",
            "/api/hobi!getDepth.action",
            "/api/user/getUserNameVerifTarget",
            "/api/user/resetPsw",
            "/api/exchangerate!list.action",
            "/api/information!list.action",
            "/etf/robot/list",
            "/api/banner!list.action",
            "/api/news!list_v2_popup.action",
            "/api/news!list.action"
    );


    @Override
    public List<String> excludePathPatterns() {
//        if(StringUtils.isNotEmpty(env) && env.contains("local")){
//            return Lists.newArrayList("/**");
//        }
        return EXCLUDE_PATH;
    }

    @Override
    public List<String> maybeAuthUri() {
        return Arrays.asList(
                "/api/contractApplyOrder!openview.action",
                "/api/contractApplyOrder!closeview.action",
                "/api/assets!getContractBySymbolType.action",
                "/api/futuresOrder!openview.action",
                "/api/newOnlinechat**",
                "/api/exchangerateuserconfig!get.action",
                "/api/item/itemUserOptionalList/isItemHasAddGlobal",
                "/api/item/itemUserOptionalList/list",
                "/api/wallet/getUsdt"
        );
    }
}
