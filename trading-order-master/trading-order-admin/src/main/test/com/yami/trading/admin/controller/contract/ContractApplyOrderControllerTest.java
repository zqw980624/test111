package com.yami.trading.admin.controller.contract;

import com.yami.trading.WebApplication;
import com.yami.trading.service.contract.ContractApplyOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(classes = WebApplication.class)
class ContractApplyOrderControllerTest {

    @Autowired
    private ContractApplyOrderService service;

    @Test
    public void listRecord(){

    }


}