package com.yami.trading.service.c2c;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.c2c.C2cAppeal;
import com.yami.trading.bean.c2c.C2cUser;

import java.util.List;
import java.util.Map;

public interface C2cUserService  extends IService<C2cUser> {
    void pagedQuery(Page page, String c2cUserId, String c2cUserType, String c2cUserPartyId, String c2cManagerName);
    /*
     * 获取所有C2C管理员
     */
    public Map<String, Object> getAllC2cManager();

    /*
     * 获取C2C管理员下级承兑商
     */
    public List<Map<String, Object>> getC2cManagerC2cUser(String manager_id);

    public C2cUser findByUsercode(String usercode);

    public C2cUser getByPartyId(String partyId);

}
