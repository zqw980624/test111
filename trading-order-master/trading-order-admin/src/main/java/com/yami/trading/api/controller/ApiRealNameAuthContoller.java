package com.yami.trading.api.controller;

import cn.hutool.core.util.StrUtil;
import com.yami.trading.admin.facade.MachineTranslationService;
import com.yami.trading.api.dto.RealNameAuthRecordDto;
import com.yami.trading.api.model.ApplyRealNameAuthModel;
import com.yami.trading.api.service.UserCacheService;
import com.yami.trading.bean.model.RealNameAuthRecord;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.constants.TipConstants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.service.AwsS3OSSFileService;
import com.yami.trading.service.RealNameAuthRecordService;
import com.yami.trading.service.system.TipService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/realNameAuth")
@Api(tags = "安全认证")
public class ApiRealNameAuthContoller {

    @Autowired
    RealNameAuthRecordService realNameAuthRecordService;

    @Autowired
    UserCacheService userCacheService;

    @Autowired
    TipService tipService;

    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;

    @Autowired
     MachineTranslationService translationService;

    @PostMapping("/apply")
    @ApiOperation(value = "实名认证申请")
    public Result<User> applyRealNameAuth(@Valid ApplyRealNameAuthModel model) {

        User user = userCacheService.currentUser();
        RealNameAuthRecord realNameAuthRecord = realNameAuthRecordService.getByUserId(user.getUserId());
        String msg="";
        if (realNameAuthRecord != null) {
            switch (realNameAuthRecord.getStatus()) {
                case 0:
                    msg = "已经提交申请，请等待审核";
                    break;
                case 1:
                    msg = "审核中";
                    break;
                case 2:
                    msg = "审核已通过";
                    break;
                case 3:
                    realNameAuthRecord.setStatus(1);
                    tipService.deleteTip(realNameAuthRecord.getUuid());
                    realNameAuthRecordService.removeById(realNameAuthRecord.getUuid());
                    realNameAuthRecord=null;
            }
            if (StrUtil.isNotBlank(msg)) {
                throw new YamiShopBindException(msg);
            }
        }
        if (realNameAuthRecord==null){
            realNameAuthRecord=new RealNameAuthRecord();
        }

        if (model.getIdNumber().length()>50){
            throw new YamiShopBindException("证件号码长度超过50");
        }
        if (model.getName().length()>50){
            throw new YamiShopBindException("实名姓名长度超过50");
        }
        BeanUtils.copyProperties(model, realNameAuthRecord);
        realNameAuthRecord.setUserId(user.getUserId());
        realNameAuthRecord.setCreateTime(new Date());
        realNameAuthRecord.setStatus(1);

        realNameAuthRecordService.saveOrUpdate(realNameAuthRecord);
        if (Constants.SECURITY_ROLE_MEMBER.equals(user.getRoleName())) {
            tipService.saveTip(realNameAuthRecord.getUuid(), TipConstants.KYC);
        }
        return Result.succeed(null);
    }

    @ApiOperation(value = "获取认证信息")
    @GetMapping("get")
    public Result get() {
        User user = userCacheService.currentUser();
        RealNameAuthRecord record = realNameAuthRecordService.getByUserId(user.getUserId());
        if (record == null) {
            return Result.succeed(new RealNameAuthRecordDto());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("id", record.getUuid());
        map.put("nationality", record.getNationality());
        map.put("idName", record.getIdName());
        map.put("idNumber", record.getIdNumber());
        map.put("name", record.getName());
        map.put("idFrontImg", record.getIdFrontImg());
        map.put("idBackImg", record.getIdBackImg());
        map.put("status", record.getStatus());
        map.put("kyc_status", record.getStatus());
        map.put("msg", record.getMsg());
        map.put("handheldPhoto", record.getHandheldPhoto());
        map.put("idname", record.getIdName());
        map.put("idnumber", record.getIdNumber());
        /*map.put("idimg_1_path", awsS3OSSFileService.getUrl(record.getIdFrontImg()));
        map.put("idimg_2_path", awsS3OSSFileService.getUrl(record.getIdBackImg()));
        map.put("idimg_3_path", awsS3OSSFileService.getUrl(record.getHandheldPhoto()));*/
        map.put("idimg_1_path", record.getIdFrontImg());
        map.put("idimg_2_path", record.getIdBackImg());
        map.put("idimg_3_path", record.getHandheldPhoto());

        map.put("idimg_1", record.getIdFrontImg());
        map.put("idimg_2", record.getIdBackImg());
        map.put("idimg_3", record.getHandheldPhoto());
        return Result.succeed(map);
    }

}
