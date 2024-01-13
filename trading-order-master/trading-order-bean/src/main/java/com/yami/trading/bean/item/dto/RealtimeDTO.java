package com.yami.trading.bean.item.dto;

import lombok.Data;

@Data
public class RealtimeDTO {

    private String pid;
    private String last;
    private String high;
    private String low;
    private String time;
    private String pc;
    private String pcp;

}

 /*{
         "pid": "39419",
         "last_dir": "greenBg",
         "last_numeric": 670.6,
         "last": "670.60",
         "bid": "0.00",
         "ask": "0.00",
         "high": "694.90",
         "low": "662.85",
         "last_close": "667.60",
         "pc": "+3.00",
         "pcp": "+0.45%",
         "pc_col": "greenFont",
         "turnover": "162.54K",
         "turnover_numeric": 162540,
         "time": "10:26:32",
         "timestamp": 1701425491
         },*/