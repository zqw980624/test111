package com.yami.trading.admin.controller.user.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WithdrawRemarksModel {

    @NotBlank
    private String id;

    private  String remarks;
}
