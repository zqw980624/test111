package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.context.annotation.Bean;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class RefusalApplyModel {

    @NotBlank
    @ApiModelProperty("驳回原因")
    private  String content;

    @ApiModelProperty
    @NotBlank
    private  String id;
}
