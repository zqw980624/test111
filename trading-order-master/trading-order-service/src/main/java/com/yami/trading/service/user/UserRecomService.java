package com.yami.trading.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.model.UserRecom;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public interface UserRecomService extends IService<UserRecom> {

    Page list(Page page,
               String useName,
              String recomUserName);

    public List<String> findChildren(String  userId);

    public List<UserRecom> getParents(String partyId);


    public UserRecom findByPartyId(String  userId);
    /**
     * 查找直推 partyId
     *
     */
    public List<String> findRecomsToPartyId(String partyId);

    /**
     * 查找直推
     *
     * @param partyId
     * @return
     */
    public List<UserRecom> findRecoms(String partyId);

}
