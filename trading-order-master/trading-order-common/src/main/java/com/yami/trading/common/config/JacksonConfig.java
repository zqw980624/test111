package com.yami.trading.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;

//@Configuration
public class JacksonConfig {

    @Bean

    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {

        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

// 全局配置序列化返回 JSON 处理

        SimpleModule simpleModule = new SimpleModule();

// 将使用String来序列化BigDecimal类型

        simpleModule.addSerializer(BigDecimal.class, ToPlainStringSerializer.instance);

        objectMapper.registerModule(simpleModule);

        return objectMapper;

    }

}