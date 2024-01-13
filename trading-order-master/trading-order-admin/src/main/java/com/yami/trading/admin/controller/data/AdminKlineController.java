package com.yami.trading.admin.controller.data;


import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.MarketOpenChecker;
import com.yami.trading.common.util.UTCDateUtils;
import com.yami.trading.huobi.data.internal.KlineService;
import com.yami.trading.service.item.ItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("normal/adminItemAction!")
@Api(tags = "行情数据")
@Slf4j
public class AdminKlineController {
    @Autowired
    private ItemService itemService;
    @Autowired
    private KlineService klineService;

    @ApiOperation(value = "kline清理")
    @GetMapping(value = {"clean.action"})
    public Result<String> clean() throws Exception {
        klineService.clean();;
        return Result.succeed ("kline清理完成");
    }
}
