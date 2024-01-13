package com.yami.trading.service.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.Tip;

import java.util.List;
import java.util.Map;

public interface  TipService   extends  IService<Tip> {

    public void init();

    /**
     * 新增通知
     *
     * @param businessId 业务id(唯一性)
     * @param model      模块
     */
    void saveTip(String businessId, String model);

    /**
     * 新增通知
     *
     * @param tip 消息通知
     */
    public void saveTip(Tip tip);

    /**
     * 移除通知
     *
     * @param businessId
     */
    void deleteTip(String businessId);

    /**
     * 批量移除通知
     *
     * @param businessId
     */
    public void deleteTip(List<String> businessIds);

    /**
     * 获取总数 数据
     *
     * @param username
     * @return
     */
    public List<Map<String, Object>> cacheSumTips(String username);

    /**
     * 获取通知数据
     *
     * @param username
     * @return
     */
    List<Map<String, Object>> cacheNewTips(String username, Long timeStamp);

    /**
     * 获取指定模块的新通知数据
     *
     * @param username
     * @return
     */
    public List<Map<String, Object>> cacheNewTipsByModel(String username, Long lastTimeStamp, String model);



    /**
     * 新增通知
     *
     * @param businessId 业务id(唯一性)
     * @param model      模块
     */
    void saveNewTip(String businessId, String model,String remark);

}
