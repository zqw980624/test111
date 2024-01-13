package com.yami.trading.admin.facade;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.customer.CustomerDto;
import com.yami.trading.bean.model.Customer;
import com.yami.trading.bean.model.Log;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.ApplicationContextUtils;
import com.yami.trading.common.util.IPHelper;
import com.yami.trading.dao.customer.CustomerMapper;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.chat.online.OnlineChatMessageService;
import com.yami.trading.service.customer.CustomerService;
import com.yami.trading.service.system.LogService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.sys.model.SysUser;
import com.yami.trading.sys.service.SysUserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    @Autowired
    SysUserService userService;
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        init();
    }
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private LogService logService;


    private Map<String, Customer> cache = new ConcurrentHashMap<String, Customer>();

    public void init() {
        List<Customer> list =  list();
        for (Customer customer : list) {
            cache.put(customer.getUserName(), customer);
        }
    }

    @Autowired
    PasswordEncoder passwordEncoder;



    @Override
    public Page<CustomerDto> listData(Page page, String userName) {

        return baseMapper.listPage(userName,page);
    }

    @Override
    @Transactional
    public void updateCustomer(String autoAnswer, String remarks, int status, String id,String operator) {
        Customer customer = getById(id);
        if (customer == null) {
            throw new YamiShopBindException("参数错误!");
        }
        SysUser sysUser = sysUserService.getByUserName(customer.getUserName());
        if (sysUser == null) {
            throw new YamiShopBindException("客服不存在!");
        }
        sysUser.setStatus(status);
        sysUser.setRemarks(remarks);
        sysUserService.updateById(sysUser);
        customer.setAutoAnswer(autoAnswer);
        updateById(customer);
        String ip = IPHelper.getIpAddr();
        saveLog(sysUser,operator,"ip:"+ip+"修改了客服["+sysUser.getUsername()+"]自动回复,原自动回复["+autoAnswer+"],新自动回复["+autoAnswer+"]");
    }

    @Override
    @Transactional
    public void updateCustomerPassword(String password, String id) {

        Customer customer = getById(id);
        if (customer == null) {
            throw new YamiShopBindException("参数错误!");
        }
        SysUser sysUser = sysUserService.getByUserName(customer.getUserName());
        SysUser userDB = BeanUtil.copyProperties(sysUser, SysUser.class);
        if (sysUser == null) {
            throw new YamiShopBindException("客服不存在!");
        }
        sysUser.setPassword(passwordEncoder.encode(password));
        updateById(customer);
        sysUserService.updateById(sysUser);

        String ip = IPHelper.getIpAddr();
        saveLog(sysUser,sysUser.getUsername(),"ip:"+ip+"管理员修改系统用户,修改前角色为["+userDB.getRoleName()+"],邮箱为["+userDB.getEmail()+"],"
                + "修改后角色为["+sysUser.getRoleName()+"],邮箱为["+sysUser.getEmail()+"]");
    }
    public void saveLog(SysUser secUser, String operator,String context) {
        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_OPERATION);
        log.setOperator(operator);
        log.setUsername(secUser.getUsername());
        log.setUserId(secUser.getUserId().toString());
        log.setLog(context);
        log.setCreateTime(new Date());
        logService.save(log);
    }
    @Override
    public void updateCustomerSafePassword(String safePassword, String id) {
        Customer customer = getById(id);
        if (customer == null) {
            throw new YamiShopBindException("参数错误!");
        }
        SysUser sysUser = sysUserService.getByUserName(customer.getUserName());
        if (sysUser == null) {
            throw new YamiShopBindException("客服不存在!");
        }
        sysUser.setSafePassword(passwordEncoder.encode(safePassword));
        sysUserService.updateById(sysUser);
    }

    @Override
    public void forceOffline(String id) {
        Customer customer = getById(id);
        if (customer == null) {
            throw new YamiShopBindException("参数错误!");
        }
        SysUser sysUser = sysUserService.getByUserName(customer.getUserName());
        if (sysUser == null) {
            throw new YamiShopBindException("客服不存在!");
        }
        customer.setOnlineState(0);
        customer.setLastOfflineTime(new Date());
        updateById(customer);

        Log log = new Log();
        log.setCategory(Constants.LOG_CATEGORY_OPERATION);
        log.setOperator(SecurityUtils.getSysUser().getUsername());
        log.setUsername(sysUser.getUsername());
        log.setUserId(sysUser.getUserId()+"");
        log.setLog("ip:"+ IPHelper.getIpAddr() +"管理员强制下线客服["+sysUser.getUsername()+"]");
        log.setCreateTime(new Date());
        logService.save(log);
    }



    @Override
    public Customer cacheByUsername(String username) {
        return getOne(new LambdaQueryWrapper<Customer>().eq(Customer::getUserName, username));
    }

    /**
     * 更新
     *
     * @param entity
     * @param isOnline true:必须在线才更新，false：都能更新
     */
    public boolean update(Customer entity, boolean isOnline) {
        if (isOnline && cacheByUsername(entity.getUserName()).getOnlineState() != 1) {
            return false;
        }
        updateById(entity);
        cache.put(entity.getUserName(), entity);
        return true;
    }

    @Override
    public void saveCustomer(String userName, String remarks, String password, int status, String safePassword,String autoAnswer,String operator) {

        Date now = new Date();
        SysUser sysUser = sysUserService.getByUserName(userName);
        if (null != sysUser) {
            throw new YamiShopBindException("系统存在相同[系统登录名]！");
        }
        sysUser = new SysUser();
        sysUser.setUsername(userName);
        sysUser.setRemarks(remarks);
//        sysUser.setPassword(passwordEncoder.encode(passwordManager.decryptPassword(password)));
        sysUser.setPassword(passwordEncoder.encode(password));
        //sysUser.setPassword(password);
        sysUser.setStatus(status);
        sysUser.setSafePassword(passwordEncoder.encode(safePassword));
        sysUserService.save(sysUser);
        String ip = IPHelper.getIpAddr();

        saveLog(sysUser,operator,"ip:"+ip+"管理员新增系统用户,角色为["+sysUser.getRoleName()+"],邮箱为["+sysUser.getEmail()+"]");
        Customer customer = new Customer();
        customer.setUserName(sysUser.getUsername());
        customer.setOnlineState(0);
        customer.setCreateTime(now);
        customer.setAutoAnswer(autoAnswer);
        save(customer);

    }

    /**
     * 分配一个在线客服给用户
     *
     * @return
     */
    @Override
    public Customer cacheOnlineOne() {
        List<Customer> list = new ArrayList<Customer>(cache.values());

        CollectionUtils.filter(list, new Predicate() {// 在线客服
            @Override
            public boolean evaluate(Object arg0) {
                // TODO Auto-generated method stub
                return ((Customer) arg0).getOnlineState() == 1;
            }
        });
        if (CollectionUtils.isEmpty(list))
            return null;
        Collections.sort(list, new Comparator<Customer>() {
            @Override
            public int compare(Customer arg0, Customer arg1) {
                // TODO Auto-generated method stub
                if (arg0.getLastCustomerTime() == null) {
                    return -1;
                } else if (arg1.getLastCustomerTime() == null) {
                    return 1;
                }
                return (int) (arg0.getLastCustomerTime().getTime() - arg1.getLastCustomerTime().getTime());
            }
        });
        return list.get(0);
    }

    public void offline(String username) {
        Customer customer = cacheByUsername(username);
        if(customer==null) {
            throw new YamiShopBindException("客服不存在");
        }
        customer.setOnlineState(0);
        customer.setLastOfflineTime(new Date());
        updateById(customer);
    }
    public void online(String username) {
        Customer customer = cacheByUsername(username);
        if(customer==null) {
            throw new YamiShopBindException("客服不存在");
        }
        customer.setOnlineState(1);
        customer.setLastOnlineTime(new Date());
        updateById(customer);
        OnlineChatMessageService onlineChatMessageService = ApplicationContextUtils.getBean(OnlineChatMessageService.class);
        onlineChatMessageService.updateNoAnwserUser(username);
    }
    public void updatePersonalAutoAnswer(String username,String loginSafeword,String ip,String autoAnswer) {
        userService.checkSafeWord(loginSafeword);
        SysUser user = userService.getByUserName(username);
        updateAutoAnswer(user,username,ip,autoAnswer);
    }
    public void updateAutoAnswer(SysUser user,String operatorUsername,String ip,String autoAnswer) {
//		this.adminSystemUserService.update(user,newPassword,type,operatorUsername,loginSafeword,code,ip,superGoogleAuthCode);
        Customer customer = cacheByUsername(user.getUsername());
        if (customer==null){
            throw  new YamiShopBindException("客服不存在!");
        }
        String sourceAutoAnswer = customer.getAutoAnswer();
        customer.setAutoAnswer(autoAnswer);
        updateById(customer);
        saveLog(user,operatorUsername,"ip:"+ip+"修改了客服["+user.getUsername()+"]自动回复,原自动回复["+sourceAutoAnswer+"],新自动回复["+autoAnswer+"]");
    }
}

