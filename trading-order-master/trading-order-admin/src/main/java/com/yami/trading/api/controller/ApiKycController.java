package com.yami.trading.api.controller;

import cn.hutool.core.util.StrUtil;
import com.yami.trading.api.service.UserCacheService;
import com.yami.trading.bean.model.RealNameAuthRecord;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.constants.TipConstants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.AwsS3OSSFileService;
import com.yami.trading.service.RealNameAuthRecordService;
import com.yami.trading.service.system.TipService;
import com.yami.trading.service.user.UserService;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@RestController
public class ApiKycController {



    @Autowired
    private RealNameAuthRecordService realNameAuthRecordService;

    @Autowired
    private TipService tipService;

    @Autowired
    private UserService partyService;

    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;

    private final String action = "/api/kyc!";


    /**
     * 获取实名认证信息
     */
    @RequestMapping(action + "get.action")
    public Result get() throws IOException {
            String partyId = SecurityUtils.getCurrentUserId();
            RealNameAuthRecord kyc = realNameAuthRecordService.getByUserId(partyId);
        Map<String,Object> map=new HashMap<>();
        map.put("partyId",kyc.getUserId());
        map.put("idname",kyc.getIdName());
        map.put("idnumber",kyc.getIdNumber());
        map.put("name",kyc.getName());
        map.put("idimg_1_path",awsS3OSSFileService.getUrl(kyc.getIdFrontImg()));
        map.put("idimg_2_path",awsS3OSSFileService.getUrl(kyc.getIdBackImg()));
        map.put("idimg_3_path", awsS3OSSFileService.getUrl(kyc.getHandheldPhoto()));
        map.put("idimg_1",kyc.getIdFrontImg());
        map.put("idimg_2",kyc.getIdBackImg());
        map.put("idimg_3",kyc.getHandheldPhoto());
        map.put("status",kyc.getStatus());
        map.put("msg",kyc.getMsg());
        map.put("nationality",kyc.getNationality());
        map.put("apply_time",kyc.getCreateTime());
        map.put("operation_time",kyc.getOperationTime());
        return Result.succeed(map);
    }

//    /**
//     * 实名认证申请
//     */
//    @RequestMapping(action + "apply.action")
//    public Object apply(HttpServletRequest request) throws IOException {
//        String idimg_1 = request.getParameter("idimg_1");
//        String idimg_2 = request.getParameter("idimg_2");
//        String idimg_3 = request.getParameter("idimg_3");
//        String idname = request.getParameter("idname");
//        String name = request.getParameter("name");
//        String idnumber = request.getParameter("idnumber");
//        String nationality = request.getParameter("nationality");
//
//
//        User user = userCacheService.currentUser();
//        RealNameAuthRecord realNameAuthRecord = realNameAuthRecordService.getByUserId(user.getUserId());
//        String msg="";
//        if (realNameAuthRecord != null) {
//            switch (realNameAuthRecord.getStatus()) {
//                case 0:
//                    msg = "已经提交申请，请等待审核";
//                    break;
//                case 1:
//                    msg = "审核中";
//                    break;
//                case 2:
//                    msg = "审核已通过";
//                    break;
//                case 3:
//                    realNameAuthRecord.setStatus(1);
//                    tipService.deleteTip(realNameAuthRecord.getUuid());
//                    realNameAuthRecordService.removeById(realNameAuthRecord.getUuid());
//                    realNameAuthRecord=null;
//                    //   return Result.succeed();
////                    msg = MessageFormat.format("审核未通过,原因:{0}", realNameAuthRecord.getMsg());
////                    break;
////                default:
////                    msg = "审核状态异常请联系客服";
////                    break;
//            }//
//            if (StrUtil.isNotBlank(msg)) {
//
//                throw new YamiShopBindException(msg);
//            }
//        }
//        if (realNameAuthRecord==null){
//            realNameAuthRecord=new RealNameAuthRecord();
//        }
//
//        if (model.getIdNumber().length()>50){
//            throw new YamiShopBindException("证件号码长度超过50");
//        }
//        if (model.getName().length()>50){
//            throw new YamiShopBindException("实名姓名长度超过50");
//        }
//        BeanUtils.copyProperties(model, realNameAuthRecord);
//        realNameAuthRecord.setUserId(user.getUserId());
//        realNameAuthRecord.setCreateTime(new Date());
//        realNameAuthRecord.setStatus(1);
//        realNameAuthRecordService.saveOrUpdate(realNameAuthRecord);
//        if (Constants.SECURITY_ROLE_MEMBER.equals(user.getRoleName())) {
//            tipService.saveTip(realNameAuthRecord.getUuid(), TipConstants.KYC);
//        }
//        return Result.succeed(null);
//        return resultObject;
//    }

}
