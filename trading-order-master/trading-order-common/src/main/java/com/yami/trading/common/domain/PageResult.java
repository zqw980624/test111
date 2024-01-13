package com.yami.trading.common.domain;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel
public class PageResult<T> {

    private List<T> records;
    private long total;
    private long size;
    private long current;
}
