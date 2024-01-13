package com.yami.trading.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.model.IpMenu;

public interface IpMenuService  extends IService<IpMenu> {


    void saveIpMenu(IpMenu entity);

    void updateIpMenu(IpMenu entity);

    void deleteIpMenu(IpMenu entity);

    IpMenu cacheByIp(String ip);

    /**
     * 新增ip到白名单
     *
     * @param ip
     */
    public void saveIpMenuWhite(String ip);
}
