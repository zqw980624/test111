package com.yami.trading.api.controller;

import com.yami.trading.common.domain.Result;
import com.yami.trading.common.util.IPHelper;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.IdentifyingCodeService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("api/idcode")
@RestController
@Api(tags = "idcode")
public class ApiIdentifyingCodeController {

    @Autowired
    private IdentifyingCodeService identifyingCodeService;
    //短信发送
    @RequestMapping("execute")
    public Result execute(@RequestParam String target) {
            identifyingCodeService.send(target, IPHelper.getIpAddr());
        return Result.succeed(null);
    }
    //短信发送
   /* @RequestMapping("execute")
    public Result execute(@RequestParam String target) {
        identifyingCodeService.send(target, IPHelper.getIpAddr());
        return Result.succeed(null);
    }*/
}
