package com.yami.trading.service.c2c;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.c2c.C2cUserParamBaseSet;

public interface  C2cUserParamBaseSetService    extends IService<C2cUserParamBaseSet> {


    public C2cUserParamBaseSet getByPartyId(String c2c_user_party_id);

}
