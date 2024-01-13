package com.yami.trading.common.domain;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.Min;

@Data
@ApiModel
public class PageRequest {

    @Min(5)
    protected long size;
    @Min(1)
    protected long current;

}
