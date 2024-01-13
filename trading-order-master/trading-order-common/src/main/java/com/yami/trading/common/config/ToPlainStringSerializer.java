package com.yami.trading.common.config;

import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializerBase;

import java.math.BigDecimal;

@JacksonStdImpl
public class ToPlainStringSerializer extends ToStringSerializerBase {
    public static final ToPlainStringSerializer instance = new ToPlainStringSerializer();

    public ToPlainStringSerializer() {
        super(Object.class);
    }

    public ToPlainStringSerializer(Class<?> handledType) {
        super(handledType);
    }

    public final String valueToString(Object value) {
        if(value instanceof BigDecimal){
            return ((BigDecimal) value).toPlainString();
        }
        return value.toString();
    }
}
