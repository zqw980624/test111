package com.yami.trading.service.purchasing;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.purchasing.UserPurchasingRecord;
import com.yami.trading.bean.purchasing.dto.UserPurchasingRecordDto;
import org.apache.ibatis.annotations.Param;

public interface UserPurchasingRecordService extends IService<UserPurchasingRecord> {

    Page<UserPurchasingRecordDto> listPage(Page page,   String rolename,
                                            String userName);
}
