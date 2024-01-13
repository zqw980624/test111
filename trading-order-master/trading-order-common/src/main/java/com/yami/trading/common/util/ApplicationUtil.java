package com.yami.trading.common.util;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.regex.Pattern;

public class ApplicationUtil {
    /**
     * 空白正则式
     */
    private static final Pattern BLANK_REGEX=Pattern.compile("\\s+");

    /**
     * 八字节缓冲
     */
    private static final ByteBuffer EIGHT_BUFFER = ByteBuffer.allocate(8);

    /**
     * 简单类型
     */
    private static LinkedHashSet<Class<?>> SIMPLE_CLASSES=new LinkedHashSet<Class<?>>();

    /**
     * 增删改DML操作
     */
    private static final HashSet<String> DML_OPTIONS=new HashSet<String>(Arrays.asList("DELETE","UPDATE","INSERT"));


    /**
     * 根据当前系统时间获取UUID字串序列
     * @return UUID字串序列
     */
    public static final String getCurrentTimeUUID(String... prexfixs) {
        EIGHT_BUFFER.clear();
        EIGHT_BUFFER.putLong(System.currentTimeMillis());
        EIGHT_BUFFER.flip();

        byte[] b=new byte[8];
        EIGHT_BUFFER.get(b);
        EIGHT_BUFFER.clear();

        String prefix=(null==prexfixs || 0==prexfixs.length)?"":prexfixs[0];
        prefix=(null==prefix || (prefix=prefix.trim()).isEmpty())?"":prefix;

        return prefix+ UUID.nameUUIDFromBytes(b).toString().replace("-", "");
    }
}
