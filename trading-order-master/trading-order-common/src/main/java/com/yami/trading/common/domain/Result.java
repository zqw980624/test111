package com.yami.trading.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
    private T data;
    private Integer code;
    private String msg;
    private Long total;

    public static <T> Result<T> succeed() {
        return of(null, CodeEnum.SUCCESS.getCode(), null);
    }

    public static <T> Result<T> succeed(T model, String msg) {
        return of(model, CodeEnum.SUCCESS.getCode(), msg);
    }

    public static <T> Result<T> succeed(T model) {
        return of(model, CodeEnum.SUCCESS.getCode(), "");
    }


    public static <T> Result<T> ok(T model) {
        return of(model, CodeEnum.SUCCESS.getCode(), "");
    }



    public static <T> Result<T> of(T datas, Integer code, String msg) {
        return new Result<>(datas, code, msg, 0L);
    }

    public static <T> Result<T> failed(String msg) {
        return of(null, CodeEnum.BUSINESS_ERROR.getCode(), msg);
    }

    public static <T> Result<T> failed(CodeEnum code) {
        return of(null, code.getCode(), code.getMessage());
    }

    public static <T> Result<T> failed(CodeEnum code, String msg) {
        return of(null, code.getCode(), msg);
    }

    public static <T> Result<T> failed(T model, String msg) {
        return of(model, CodeEnum.BUSINESS_ERROR.getCode(), msg);
    }

    public boolean isSucceed() {
        return 0 == code;
    }
}
