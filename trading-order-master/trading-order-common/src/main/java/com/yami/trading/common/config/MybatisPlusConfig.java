/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */

package com.yami.trading.common.config;

import cn.hutool.crypto.digest.MD5;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigInteger;
import java.util.Map;


/**
 *
 */
@Configuration
//@ConditionalOnBean(DataSource.class)
@MapperScan({"com.yami.trading.**.dao"})
@Slf4j
public class MybatisPlusConfig {


    public static int mod(String str) {
        BigInteger bigInt = new BigInteger(1, MD5.create().digest(str));
        return bigInt.mod(BigInteger.TEN).abs().intValue();
    }

    public static void main(String[] args) {
        System.out.println(mod("BTCUSD"));
    }

    @Bean
    public MySqlInjector sqlInjector() {
        return new MySqlInjector();
    }

    /**
     * 分页插件
     *
     * @return PaginationInterceptor
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandler((sql, tableName) -> {
            if (!tableName.equalsIgnoreCase("t_kline") && !tableName.equalsIgnoreCase("t_realtime")) {
                return tableName;
            }
            // 获取参数方法
            Map<String, Object> paramMap = RequestDataHelper.getRequestData();
            if (CollectionUtils.isNotEmpty(paramMap) && paramMap.containsKey("symbol")) {
                String symbol = paramMap.get("symbol").toString();
                int i = mod(symbol);
                log.debug("******************  {}->{}", symbol, i);
                // 10，并取绝对值
                return tableName + "_" + i;
            }
            return tableName;
        });
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);
        //分页插件
        interceptor.addInnerInterceptor(new
                PaginationInnerInterceptor(DbType.MYSQL));

        //添加内部拦截器(参数为创建一个OptimisticLockerInterceptor(乐观锁拦截器))
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());


        return interceptor;
    }

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
//            configuration.setUseDeprecatedExecutor(false);
            configuration.setJdbcTypeForNull(JdbcType.NULL);
        };
    }


}
