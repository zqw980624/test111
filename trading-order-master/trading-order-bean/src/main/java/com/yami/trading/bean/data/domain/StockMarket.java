package com.yami.trading.bean.data.domain;

import lombok.Data;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class StockMarket {
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");

    private String symbol;
    private String status_id;
    private String region;
    private String status;
    private String time_zone;
    private String time_str;

    public void calculate() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(time_zone));
        // 格式化日期和时间
        String formattedDateTime = now.format(formatter);
        this.time_str = formattedDateTime;
    }

}
