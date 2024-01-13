package com.yami.trading.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.model.UserDataSum;

import java.util.List;

public interface UserDataSumService   extends IService<UserDataSum> {
    List getByUserId(String partyId);
}
