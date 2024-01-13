package com.yami.trading.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.user.dto.AgentUserDto;
import com.yami.trading.bean.user.dto.UserDataDto;
import com.yami.trading.bean.user.dto.UserDto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface UserService   extends IService<User> {
    User registerMobile(String userMobile, String password, String userCode,boolean robot);

    User registerMail(String userMail, String password, String userCode,boolean robot);

    void setSafeword(String userId,String safePassword);

    User findByEmail(String email);

    User findByUserName(String userName);

    User findByuserPhone(String userPhone);

    User findByUserMobile(String mobile);

    public void saveResetLock(String partyId,double moneyRevise,String safeword,String operatorName,String resetType,String ip,String coinType) ;
    /**
     * 登录
     */
    public User login(String username, String password);

    public boolean isOnline(String partyId);
    /**
     * 设置玩家在线
     */
    public void online(String partyId);

    public void saveUser(String username, String password, boolean login_authority, boolean enabled, String remarks,String operatorUsername,String ip,String parents_usercode);

    /**
     * 获取PAT_PARTY 根据已验证的电话号码
     */
    public User findPartyByVerifiedPhone(String phone);

    /**
     * 获取用户等级
     * @param user
     * @return
     */
     int getUserLevelByAuth(User user);

    /**
     * 修改账户余额
     * @param userId
     * @param moneyRevise
     * @param accountType
     * @param coinType
     */
     void updateWallt(String userId, BigDecimal moneyRevise,  int accountType, String coinType);

    /**
     * 修改提现限制流水
     * @param userId
     */
    void updateWithdrawalLimitFlow(String userId, BigDecimal moneyWithdraw);

    /**
     * 重置密码
     * @param userId
     * @param password
     */
    void  restLoginPasswrod(String userId,
                            String password );

    /**
     * 解绑用户谷歌验证器
     * @param userId
     * @param googleAuthCode
     * @param loginSafeword
     */
    void  deleteGooleAuthCode(String  userId,String googleAuthCode,String loginSafeword);

    /**
     * 重置资金密码
     * @param userId
     * @param googleAuthCode
     * @param loginSafeword
     */
    void restSafePassword(String userId,String newSafeword);

    /**
     * 检查用户资金密码是否正确  true 正确
     * @param userId
     * @param loginSafeword
     * @return
     */
    boolean checkLoginSafeword(String userId,String loginSafeword);

    /**
     * 检查用户资金密码是否正确  true 正确
     * @param userId
     * @param loginSafeword
     * @return
     */

    boolean checkLoginSafeword(User  user,String loginSafeword);


    Page<UserDto> listUser(Page page, List<String> roleNames, String userCode, String userName,List<String> checkedList);

    /**
     * 获取基础数据
     * @param page
     * @param roleNames
     * @param userCode
     * @param userName
     * @param ip
     * @return
     */
    Page<UserDataDto> listUserAndRecom(Page page,  List<String> roleNames, String userCode, String userName,
                                      String lastIp,List<String> checkedList);

    void  updateAgent(String userId,boolean operaAuthority,boolean loginAuthority);
    User findUserByUserCode(String userCode);
    User saveAgentUser(String userName, String password, String s, String roleName, String remarks,String userCode,boolean loginAuthority);

    User cacheUserBy(String userId);

    long countToDay();
    long countToDays(String userCode);

    /**
     * 获取PAT_PARTY 根据已验证的邮箱
     */
    User findPartyByVerifiedEmail(String email);

    void saveRegister(String username, String password, String usercode, String safeword, String verifcode);

    void logout(String userId);

    void  saveRegisterUsername(String username, String password, String recoUserCode, String safeword);

    Page  getAgentAllStatistics(long current,long size,String startTime, String endTime,String userName,
                                String userId);
}
