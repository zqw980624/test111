package com.yami.trading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.model.RealNameAuthRecord;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.model.UserSafewordApply;
import com.yami.trading.bean.user.dto.UserSafewordApplyDto;
import com.yami.trading.common.constants.TipConstants;
import com.yami.trading.common.domain.BaseEntity;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.common.util.Strings;
import com.yami.trading.dao.user.UserSafewordApplyMapper;
import com.yami.trading.service.AwsS3OSSFileService;
import com.yami.trading.service.RealNameAuthRecordService;
import com.yami.trading.service.system.TipService;
import com.yami.trading.service.user.UserSafewordApplyService;
import com.yami.trading.service.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserSafewordApplyServiceImpl extends ServiceImpl<UserSafewordApplyMapper, UserSafewordApply> implements UserSafewordApplyService {

    @Autowired
    RealNameAuthRecordService realNameAuthRecordService;

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    TipService tipService;

    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;

    public UserSafewordApply findByUserIdNoPass(String userId, int operate) {

        List<UserSafewordApply> list = list(Wrappers.<UserSafewordApply>query().lambda()
                .eq(UserSafewordApply::getUserId, userId)
                .eq(UserSafewordApply::getOperate, operate)
                .notIn(UserSafewordApply::getStatus, 2));
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    @Override
    public Page<UserSafewordApplyDto> listRecord(Page page, String rolename, String status, String userCode, String userName, String operate) {

        return baseMapper.listRecord(page, rolename, status, userCode, userName, operate);
    }



    /**
     * 尚未通过的申请
     */
    public UserSafewordApply findByPartyIdNoPass(String partyId, Integer operate) {
        List<UserSafewordApply> list= list(Wrappers.<UserSafewordApply>query().lambda().
                eq(UserSafewordApply::getUserId,partyId).notIn(UserSafewordApply::getStatus,2).eq(UserSafewordApply::getOperate,operate));
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    public UserSafewordApply  get(String partyId) {
        List<UserSafewordApply> list= list(Wrappers.<UserSafewordApply>query().lambda().eq(UserSafewordApply::getUserId,partyId));
        return list.size()>0?list.get(0):null;
    }

    @Override
    public List<UserSafewordApply> findByUserId(String userId) {
        LambdaQueryWrapper<UserSafewordApply> lambdaQueryWrapper= Wrappers.<UserSafewordApply>query().lambda().eq(UserSafewordApply::getUserId,userId);
        lambdaQueryWrapper.orderByDesc(BaseEntity::getCreateTime);
        return list(lambdaQueryWrapper);
    }

    public Map<String, Object> bindOne(UserSafewordApply apply) {

        Map<String, Object> result = new HashMap<String, Object>();

        String idcard_path_front_path = "";
        String idcard_path_back_path = "";
        String idcard_path_hold_path = "";

        if (!StringUtils.isNullOrEmpty(apply.getIdcardPathFront())) {
            idcard_path_front_path = awsS3OSSFileService.getUrl(apply.getIdcardPathFront());
        }
        result.put("idcard_path_front_path", idcard_path_front_path);

        if (!StringUtils.isNullOrEmpty(apply.getIdcardPathBack())) {
            idcard_path_back_path =awsS3OSSFileService.getUrl(apply.getIdcardPathBack());
        }

        result.put("idcard_path_back_path", idcard_path_back_path);

        if (!StringUtils.isNullOrEmpty(apply.getIdcardPathHold())) {
            idcard_path_hold_path = awsS3OSSFileService.getUrl(apply.getIdcardPathHold());
        }
        result.put("idcard_path_hold_path", idcard_path_hold_path);

        result.put("id", apply.getUuid());
        result.put("create_time", DateUtils.format(apply.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
        result.put("msg", apply.getMsg());
        result.put("apply_time", DateUtils.format(apply.getApplyTime(), DateUtils.DF_yyyyMMddHHmmss));
        result.put("status", apply.getStatus());
        result.put("operate", apply.getOperate());
        result.put("remark", apply.getRemark());

        return result;
    }

    /**
     * 人工重置  操作类型 operate:	 0/修改资金密码；1/取消谷歌绑定；2/取消手机绑定；3/取消邮箱绑定；
     */
    public void saveApply(String partyId, String idcard_path_front, String idcard_path_back, String idcard_path_hold, String safeword,
                          String safeword_confirm, Integer operate, String remark) {

        if (null == operate || !Arrays.asList(0, 1, 2, 3).contains(operate)) {
            throw new YamiShopBindException("操作类型不正确");
        }

        // 操作类型 operate:	 0/修改资金密码；
        if (0 == operate.intValue()) {

            if (StringUtils.isEmptyString(safeword)) {
                throw new YamiShopBindException("资金密码不能为空");
            }

            if (safeword.length() != 6 || !Strings.isNumber(safeword)) {
                throw new YamiShopBindException("资金密码不符合设定");
            }

            if (StringUtils.isEmptyString(safeword_confirm)) {
                throw new YamiShopBindException("资金密码确认不能为空");
            }

            if (!safeword.equals(safeword_confirm)) {
                throw new YamiShopBindException("两次输入的资金密码不相同");
            }
        }
//		// 操作类型 operate:	 0/修改资金密码；
//		if (0 == operate.intValue()) {
        RealNameAuthRecord  kyc = realNameAuthRecordService.getByUserId(partyId);
        if (null == kyc || kyc.getStatus() != 2) {
            throw new YamiShopBindException("实名认证尚未通过，无法重置");
        }
//		}

        UserSafewordApply apply =findByPartyIdNoPass(partyId, operate);
        if (null == apply) {
            apply = new UserSafewordApply();
            apply.setCreateTime(new Date());
        } else if (apply.getStatus() != 3) {
            throw new YamiShopBindException("您的申请之前已提交过");
        }

        // 操作类型 operate:	 0/修改资金密码；
        if (0 == operate.intValue()) {
            String safewordMd5 = passwordEncoder.encode(safeword);
            apply.setSafeword(safewordMd5);
        } else {
            apply.setSafeword("");
        }

        apply.setIdcardPathFront(idcard_path_front);
        apply.setIdcardPathBack(idcard_path_back);
        apply.setIdcardPathHold(idcard_path_hold);
        apply.setOperate(operate);
        apply.setRemark(remark);
        apply.setStatus(1);

        if (null == apply.getUserId()) {
            apply.setUserId(partyId);
            save(apply);
        } else {
            updateById(apply);
        }
        tipService.saveTip(apply.getUuid(), TipConstants.USER_SAFEWORD_APPLY+"-"+ operate);
    }


    @Override
    @Transactional
    public void examine(String id ,String content,int type) {
        UserSafewordApply apply = getById(id);
        if (null == apply) {
            throw new YamiShopBindException("申请不存在，或请刷新重试");
        }
        RealNameAuthRecord kyc = realNameAuthRecordService.getByUserId(apply.getUserId());
        if (null == kyc || kyc.getStatus() != 2) {
            throw new YamiShopBindException("认证尚未通过，无法重置");
        }
//        if (apply.getStatus()!=2){
//            throw  new YamiShopBindException("记录已操作过了");
//        }
        apply.setApplyTime(new Date());
        if (type==1){
            apply.setStatus(2);
            User user = userService.getById(apply.getUserId());
            // 操作类型 operate:	 0/修改资金密码；1/取消谷歌绑定；2/取消手机绑定；3/取消邮箱绑定；
            switch (apply.getOperate()) {
                case 0:
                    user.setSafePassword(passwordEncoder.encode(apply.getSafeword()));
                    break;
                case 1:
//                    if (!user.isGoogleAuthBind()) {
//                        throw new YamiShopBindException("用户未绑定，无需解绑");
//                    }
                    user.setGoogleAuthBind(false);
                case 2:
                    user.setUserMobileBind(false);
                    break;
                case 3:
                    user.setMailBind(false);
                    break;
            }
            userService.updateById(user);
        }
        else {
            apply.setStatus(3);
            apply.setMsg(content);
        }
        updateById(apply);
        tipService.deleteTip(apply.getUuid());

    }

}
