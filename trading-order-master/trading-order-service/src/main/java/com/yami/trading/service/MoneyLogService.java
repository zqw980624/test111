package com.yami.trading.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.model.MoneyLog;
import com.yami.trading.bean.user.dto.SumBSalanceDto;

import java.util.Date;
import java.util.Map;

public interface MoneyLogService extends IService<MoneyLog> {
    Page pageMoneyLog(String userCode, Page page, String rolename, Date startTime,
                      Date endTime,
                      String userName,String log,String category);


    SumBSalanceDto sumBSalance(String category, String contentType, String partyId);

}
