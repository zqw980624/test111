package com.yami.trading.admin.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class IdModel {

    @NotBlank
    private  String id;
}
