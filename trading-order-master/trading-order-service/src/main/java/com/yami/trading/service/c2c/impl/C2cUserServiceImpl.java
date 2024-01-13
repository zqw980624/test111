package com.yami.trading.service.c2c.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.c2c.C2cUser;
import com.yami.trading.dao.c2c.C2cUserMapper;
import com.yami.trading.service.c2c.C2cUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class C2cUserServiceImpl  extends ServiceImpl<C2cUserMapper, C2cUser> implements C2cUserService {


    @Override
    public void pagedQuery(Page page, String c2cUserId, String c2cUserType, String c2cUserPartyId, String c2cManagerName) {
        baseMapper.pagedQuery(page,c2cUserId,c2cUserType,c2cUserPartyId,c2cManagerName);
    }

    /*
     * 获取所有C2C管理员
     */
    @Override
    public Map<String, Object> getAllC2cManager() {

//        List<Map<String,Object>> list=baseMapper.getAllC2cManager("SECURITY_ROLE_C2C");
//        Map<String, Object> mapRet = new HashMap<String, Object>();
//        for (Map<String, Object> data : list) {
//            mapRet.put(data.get("id").toString(), data.get("user_name"));
//        }


        return null;
    }

    @Override
    public C2cUser getByPartyId(String partyId) {
       List<C2cUser >  list= list(Wrappers.<C2cUser>query().lambda().eq(C2cUser::getC2cUserPartyId,partyId));
       if (CollectionUtil.isNotEmpty(list)){
           return list.get(0);
       }
        return null;
    }

    @Override
    public C2cUser findByUsercode(String usercode) {
        List<C2cUser> list = list(Wrappers.<C2cUser>query().lambda().eq(C2cUser::getC2cUserCode,usercode));
        if (list.size() > 0)
            return list.get(0);
        return null;
    }

    /*
     * 获取C2C管理员下级承兑商
     */
    public List<Map<String, Object>> getC2cManagerC2cUser(String manager_id) {

        return baseMapper.getC2cManagerC2cUser(manager_id);

    }
}
