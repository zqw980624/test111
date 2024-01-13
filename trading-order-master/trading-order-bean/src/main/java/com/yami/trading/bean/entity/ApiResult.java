package com.yami.trading.bean.entity;



import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResult {
    private int pageCount;
    private int pageSize;
    private int pageIndex;
    private int totalCount;
    Object data;
}
