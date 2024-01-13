package com.yami.trading.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class KLine implements Serializable {
    KLineMeta meta;
    String status;
    List<KLineValue> values;

    @Override
    public String toString() {
        return "KLine{" +
                "meta=" + meta +
                ", status='" + status + '\'' +
                ", values=" + values +
                '}';
    }
}
