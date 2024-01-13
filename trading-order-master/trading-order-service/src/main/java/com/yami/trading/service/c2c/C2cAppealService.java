package com.yami.trading.service.c2c;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.c2c.C2cAppeal;
import com.yami.trading.bean.c2c.C2cOrder;

public interface C2cAppealService  extends IService<C2cAppeal> {

    Page pagedQuery(Page page, String status, String orderNo, String userCode, String roleName, String c2cUserCode, String c2cUserType, String c2cUserPartyCode);


    C2cAppeal findByOrderNo(String orderNo);

    void handled(C2cAppeal appeal, String username, String partyId);

    /*
     * 查询未处理申诉数量，根据广告ID
     */
    public Long findNoHandleAppealsCountByAdvertId(String c2cAdvertId);
}
