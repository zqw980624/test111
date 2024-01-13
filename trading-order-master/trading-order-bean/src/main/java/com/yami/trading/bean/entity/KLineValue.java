package com.yami.trading.bean.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class KLineValue implements Serializable {
    String datetime;
    Double open;
    Double close;
    Double high;
    Double low;
    Double volume;

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public Double getOpen() {
        return open;
    }

    public void setOpen(Double open) {
        this.open = open;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "KLineValue{" +
                "datetime='" + datetime + '\'' +
                ", open=" + open +
                ", close=" + close +
                ", high=" + high +
                ", low=" + low +
                ", volume=" + volume +
                '}';
    }
}