package com.yami.trading.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.model.MoneyLog;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.MoneyLogService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class ApiMoneyLogController {
    @Autowired
    protected MoneyLogService moneyLogService;
    @Autowired
    UserService userService;
    @Autowired
    ItemService itemService;

    @RequestMapping("api/moneylog!list.action")
    public Result list(HttpServletRequest request) throws IOException {
        String partyId = SecurityUtils.getUser().getUserId();
        String page_no = request.getParameter("page_no");
        String symbolType = request.getParameter("symbolType");
        if (StringUtils.isNullOrEmpty(page_no)
                || !StringUtils.isInteger(page_no) || Integer.valueOf(page_no) <= 0) {
            page_no = "1";
        }
        double amount = 0;
        int pageNo = Integer.valueOf(page_no);
        List<String> symbols = itemService.findByType(symbolType).stream().map(Item::getSymbol).collect(Collectors.toList());
        symbols.add("-1");
        Page<MoneyLog> page = new Page<>(pageNo, 20);
        LambdaQueryWrapper<MoneyLog> lambdaQueryWrapper = Wrappers.<MoneyLog>query().lambda()
                .eq(MoneyLog::getUserId, partyId);
        log.info(symbols+"==========");
        lambdaQueryWrapper.orderByDesc(MoneyLog::getCreateTime);
        moneyLogService.page(page, lambdaQueryWrapper);
        for (MoneyLog log : (List<MoneyLog>) page.getRecords()) {
            log.setContent_type(log.getContentType());
            log.setWallet_type(log.getWalletType());
            log.setAmount_after(log.getAmountAfter().setScale(4, RoundingMode.HALF_UP));
            log.setAmount_before(log.getAmountBefore().setScale(4, RoundingMode.HALF_UP));
            if (Constants.MONEYLOG_CONTENT_FINANCE_PROFIT.equals(log.getContentType())) {
                log.setAmount(log.getAmount().add(new BigDecimal(amount)));
                log.setCreateTimeStr(DateUtils.format(log.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
            }
        }
        return Result.succeed(page.getRecords());
    }
}
