package com.yami.trading.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.model.RealNameAuthRecord;

import java.util.List;

public interface RealNameAuthRecordService    extends IService<RealNameAuthRecord> {
    RealNameAuthRecord getByUserId(String userId);

    /**
     * 审核是否通过
     * @param userId
     * @return
     */
    boolean isPass(String userId);

    Page pageRecord(Page page, String rolename, String idNumber, String status,String userCode);
    Page pageRecords(Page page, List<String> roleNames, String idNumber, String status, String userCode, List<String> checkedList);

    long waitCount();

}
