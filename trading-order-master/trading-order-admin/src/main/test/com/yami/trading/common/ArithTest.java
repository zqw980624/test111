package com.yami.trading.common;

import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.util.Arith;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ArithTest {

    @Test
    public void testAdd() {
        String add = Arith.add("1.111", "2.222");
        assertEquals("3.333", add);
    }

    @Test
    public void testAddWithDecimals() {
        String add = Arith.add("1.111", "2.222", 2);
        assertEquals("3.33", add);
    }

    @Test
    public void testStr() {
        String add = Arith.str(new Double("1.89234"),3 );
        assertEquals("1.892", add);
    }
}
