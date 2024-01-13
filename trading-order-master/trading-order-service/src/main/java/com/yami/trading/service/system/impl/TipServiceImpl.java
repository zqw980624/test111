package com.yami.trading.service.system.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.yami.trading.bean.Tip;
import com.yami.trading.bean.model.Log;
import com.yami.trading.bean.model.RealNameAuthRecord;
import com.yami.trading.common.constants.RedisKeys;
import com.yami.trading.common.constants.TipConstants;
import com.yami.trading.dao.log.LogMapper;
import com.yami.trading.dao.tip.TipMapper;
import com.yami.trading.service.RealNameAuthRecordService;
import com.yami.trading.service.syspara.SysparaService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.service.system.TipService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TipServiceImpl   extends ServiceImpl<TipMapper, Tip>  implements TipService  {
    private static Logger logger = LoggerFactory.getLogger(TipServiceImpl.class);

    public final static String TIPLIST = "TIPLIST_";
    /**
     * key：业务id
     */

    @Autowired
    private SysparaService sysparaService;

    @Autowired
    RedisTemplate redisTemplate;
@Autowired
    RealNameAuthRecordService realNameAuthRecordService;

    /**
     * 初始化数据
     */
    public void init() {

//       HashOperations hashOps = redisTemplate.opsForHash();
        redisTemplate.delete(TIPLIST);

        List<Tip> list = this.list();
        for (Tip tip : list) {
            System.out.println(tip.getBusinessId()+"->"+tip.getModel());
//            if (tip.getModel().equals(TipConstants.KYC)){
//                RealNameAuthRecord realNameAuthRecord= realNameAuthRecordService.getById(tip.getBusinessId());
//                if (realNameAuthRecord==null){
//                        deleteTip(tip.getBusinessId());
//                }
//                else {
//                    put(tip.getBusinessId(), tip);
//                }
//            }//else {

                put(tip.getBusinessId(), tip);
            //}
        }
    }

    /**
     * 新增通知
     *
     * @param businessId 业务id(唯一性)
     * @param model      模块
     */
    public void saveTip(String businessId, String model) {
        try {
            Tip tip = get(businessId);
            if (null == tip) {
                tip = new Tip();
            }

            tip.setBusinessId(businessId);
            tip.setCreateTime(new Date());
            tip.setModel(model);
            tip.setTimeStamp(new Date().getTime());

            saveOrUpdate(tip);
            put(businessId, tip);
        } catch (Exception e) {
            logger.error("fail put tip businessId:{" + businessId + "},model:{" + model + "},e:{}", e);
        }
    }

    public  void  put(String businessId,Tip tip){
        try {
            HashOperations hashOps = redisTemplate.opsForHash();
            hashOps.put(TIPLIST,businessId,tip);
        }catch (Exception e){
        }
    }

    public  void  remove(String businessId){
        try {
            HashOperations hashOps = redisTemplate.opsForHash();
            hashOps.delete(TIPLIST,businessId);
        }catch (Exception e){

        }
    }

    public  Tip  get(String businessId){

        try {
            HashOperations hashOps = redisTemplate.opsForHash();
            return (Tip) hashOps.get(TIPLIST,businessId);
        }catch (Exception e){

        }
        return  null;
    }

    public  List<Tip>  values(){
        try {
            List<Tip> list = redisTemplate.opsForHash().values(TIPLIST);
            return list;
        }catch (Exception e){

        }
        return null;
    }

    /**
     * 新增通知
     *
     * @param tip 消息通知
     */
    public void saveTip(Tip tip) {
        try {
            tip.setCreateTime(new Date());
            tip.setTimeStamp(new Date().getTime());
            save(tip);
            put(tip.getBusinessId(), tip);
        } catch (Exception e) {
            logger.error("fail put tip businessId:{" + tip.getBusinessId() + "},model:{" + tip.getModel() + "},e:{}", e);
        }
    }

    /**
     * 移除通知
     *
     * @param businessId
     */
    public void deleteTip(String businessId) {
        try {
            Tip tip = get(businessId);
            if (tip != null) {
                removeById(tip);
                remove(businessId);
            }
        } catch (Exception e) {
            logger.error("fail remove tip businessId:{" + businessId + "},e:{}", e);
        }
    }

    /**
     * 批量移除通知
     *
     */
    public void deleteTip(List<String> businessIds) {
        deleteBatchTip(businessIds);// 解决幂等性性问题
        for (String id : businessIds) {
            remove(id);
        }
    }

    /**
     * 批量更新订单收益
     *
     */
    protected void deleteBatchTip(final List<String> idList) {
        remove(Wrappers.<Tip>query().lambda().in(Tip::getBusinessId,idList));
    }

    /**
     * 获取总数 数据
     *
     * @param username
     * @return
     */
    public List<Map<String, Object>> cacheSumTips(String username) {

        List<Map<String, Object>> result = new LinkedList<Map<String, Object>>();

        // 根据权限列表，判定是否有模块的通知权限
        List<String> resourceIds = this.userAuth(username);
        List<Tip> filterTips = filterTips(username, null);
        if (CollectionUtils.isEmpty(filterTips) || CollectionUtils.isNotEmpty(filterTips)) {
            // logger.info("根据用户名获取的通知数据:{}", filterTips);
            return result;
        }
        // 构建模块的通知数据
        Map<String, List<Tip>> modelMap = modelData(filterTips);
        //logger.info("构建模块的通知数据:{}", modelMap);

        for (Map.Entry<String, List<Tip>> entry : modelMap.entrySet()) {
            if(resourceIds==null){
                Map<String, Object> map = tipData(entry.getKey(), entry.getValue());
                result.add(map);
            } else if (resourceIds.contains(entry.getKey())) {// 有权限则返回通知
                Map<String, Object> map = tipData(entry.getKey(), entry.getValue());
                result.add(map);
            }
        }
        // logger.info("返回数据:{}", result);
        return result;
    }

    /**
     * 获取指定模块的新通知数据
     *
     * @param username
     * @return
     */
    public List<Map<String, Object>> cacheNewTipsByModel(String username, Long lastTimeStamp, String model) {

        List<Map<String, Object>> result = new LinkedList<Map<String, Object>>();

        // 根据权限列表，判定是否有模块的通知权限
        List<String> resourceIds = this.userAuth(username);
        List<Tip> filterNewTips = filterTips(username, lastTimeStamp);

        if (CollectionUtils.isEmpty(filterNewTips)) {
            return result;
        }
        // 构建模块的通知数据
        Map<String, List<Tip>> modelMap = modelData(filterNewTips);
        List<Tip> tipList = modelMap.get(model);
        if (CollectionUtils.isEmpty(tipList)) {
            return result;
        } else {
            result.add(tipNewData(model, tipList));
        }
        return result;
    }


    /**
     * 新增通知
     *
     * @param businessId 业务id(唯一性)
     * @param model      模块
     */
    public void saveNewTip(String businessId, String model,String remark) {
        try {
            Tip tip = get(businessId);
            if (null == tip) {
                tip = new Tip();
            }

            tip.setBusinessId(businessId);
            tip.setCreateTime(new Date());
            tip.setModel(model);
            tip.setTimeStamp(new Date().getTime());
            tip.setTargetUsername(remark);
            saveOrUpdate(tip);
            put(businessId, tip);
        } catch (Exception e) {
            logger.error("fail put tip businessId22:{" + businessId + "},model:{" + model + "},e:{}", e);
        }
    }

    /**
     * 获取新通知数据
     *
     * @param username
     * @return
     */
    public List<Map<String, Object>> cacheNewTips(String username, Long lastTimeStamp) {

        List<Map<String, Object>> result = new LinkedList<Map<String, Object>>();

        // 根据权限列表，判定是否有模块的通知权限
        List<String> resourceIds = this.userAuth(username);
        List<Tip> filterNewTips = filterTips(username, lastTimeStamp);

//        System.out.println("username = " + username);
//        System.out.println("resourceIds = " + resourceIds);
//        System.out.println("filterNewTips = " + filterNewTips);

        if (CollectionUtils.isEmpty(filterNewTips)) {
            return result;
        }
        // 构建模块的通知数据
        Map<String, List<Tip>> modelMap = modelData(filterNewTips);

        for (Map.Entry<String, List<Tip>> entry : modelMap.entrySet()) {
            if(resourceIds==null){
                Map<String, Object> map = tipNewData(entry.getKey(), entry.getValue());
                result.add(map);
            }
            else if (resourceIds.contains(entry.getKey())) {// 有权限则返回通知
                Map<String, Object> map = tipNewData(entry.getKey(), entry.getValue());
                result.add(map);
            }
        }
        return result;
    }

    private List<Tip> filterTips(final String username, final Long lastTimeStamp) {
        ArrayList<Tip> tipList = new ArrayList<Tip>(values());
        CollectionUtils.filter(tipList, new Predicate() {// 过滤查找新生成的通知数据
            @Override
            public boolean evaluate(Object paramObject) {
                // TODO Auto-generated method stub
                Tip tip = (Tip) paramObject;
                if (TipConstants.MUST_USERNAME_MODEL.containsKey(tip.getModel())) {
                    if (StringUtils.isNotEmpty(tip.getTargetUsername()) && username.equals(tip.getTargetUsername())) {
                        return lastTimeStamp == null || tip.getTimeStamp() > lastTimeStamp;// 时间戳为空则直接返回
                    } else {
                        return false;
                    }
                }
                return lastTimeStamp == null || ((Tip) paramObject).getTimeStamp() > lastTimeStamp;
            }
        });
        return tipList;
    }

    private String getPath(HttpServletRequest request) {
        return String.format("%s://%s:%s%s", request.getScheme(), request.getServerName(), request.getServerPort(),
                request.getContextPath());
    }

    /**
     * 用户权限列表，根据权限判定对应的模块提醒
     *
     * @param username
     * @return
     */
    private List<String> userAuth(String username) {
        // 这块可做成缓存
//        SecUser user = this.secUserService.findUserByLoginName(username);
//        Role role = user.getRoles().toArray(new Role[0])[0];
//        Set<Resource> resources = role.getResources();
//        List<String> resourceIds = new ArrayList<String>();
//        for (Resource r : resources) {
//            resourceIds.add(r.getId().toString());
//        }
//        return resourceIds;
        return null;
    }

    /**
     * 每个模块对应的提醒数据
     *
     * @return
     */
    private Map<String, List<Tip>> modelData(Collection<Tip> tips) {
        // 构建需要返回对应权限的数据
        ArrayList<Tip> tipList = new ArrayList<Tip>(tips);
        Map<String, List<Tip>> modelMap = new HashMap<String, List<Tip>>();
        // 构建各个模块的数据
        for (Tip tip : tipList) {
            List<Tip> list = modelMap.get(tip.getModel());
            if (list == null) {
                list = new ArrayList<Tip>();
            }
            list.add(tip);
            modelMap.put(tip.getModel(), list);
        }
        return modelMap;
    }

    /**
     * 构建模块的通知数据
     *
     * @param model    模块
     * @param list     模块列表数据
     * @return
     */
    private Map<String, Object> tipNewData(String model, final List<Tip> list) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        // 模块新增数
        resultMap.put("tip_content_num", list.size());
        // 生成html通知
//        String htmlMessage = MessageFormat.format(TipConstants.MESSAGE_MAP.get(model),
//                MessageFormat.format("<span class=\"label label-danger\">{0}</span>", list.size()));
        String htmlMessage = TipConstants.MESSAGE_MAP.get(model);
        if(list.size() > 0 && htmlMessage != null){
            htmlMessage = htmlMessage.replace("{0}","<span style='color:#E05561'>"+list.size()+"</span>");
        }
        // 模块提示消息
        resultMap.put("tip_message", list.size() > 0 ? htmlMessage : "");
        // 模块请求url
        resultMap.put("tip_url", list.size() > 0 ? TipConstants.ACTION_MAP.get(model) : "");
        // 是否右下角提示
        resultMap.put("tip_show",isShowTip(model));
        // 提示类型
        resultMap.put("tip_type",TipConstants.MESSAGE_TYPE.get(model));
        return resultMap;
    }
    /**
     * 是否右下角提示，true：是，false：否
     * @param model
     * @return
     */
    private boolean isShowTip(String model) {
        String value = sysparaService.find("tip_noshow_models").getSvalue();
        return value.indexOf(model)==-1;
    }
    private Map<String, Object> tipData(String model, final List<Tip> list) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        // 页面上对应的classname(可自定义)
        resultMap.put("tip_dom_name", TipConstants.DOM_MAP.get(model));
        // 模块总数
        resultMap.put("tip_content_sum", list.size());
        // 模块新增数
        return resultMap;
    }

}
