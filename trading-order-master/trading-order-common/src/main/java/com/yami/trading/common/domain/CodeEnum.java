package com.yami.trading.common.domain;

public enum CodeEnum {
    SUCCESS(0, "操作成功"),
    BUSINESS_ERROR(1, "业务逻辑错误");
    private int code;
    private String message;

    private CodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
