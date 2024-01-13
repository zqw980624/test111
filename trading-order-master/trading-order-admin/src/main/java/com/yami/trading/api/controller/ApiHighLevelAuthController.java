package com.yami.trading.api.controller;

import cn.hutool.core.util.StrUtil;
import com.yami.trading.admin.facade.MachineTranslationService;
import com.yami.trading.api.dto.HighLevelAuthRecordDto;
import com.yami.trading.api.model.ApplyHighLevelAuthModel;
import com.yami.trading.api.service.UserCacheService;
import com.yami.trading.bean.model.HighLevelAuthRecord;
import com.yami.trading.bean.model.RealNameAuthRecord;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.constants.TipConstants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.service.HighLevelAuthRecordService;
import com.yami.trading.service.RealNameAuthRecordService;
import com.yami.trading.service.system.TipService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.text.MessageFormat;
import java.util.Date;

@RestController
@RequestMapping("api/highLevelAuth")
@Api(tags = "高级认证")
public class ApiHighLevelAuthController {
    @Autowired
    HighLevelAuthRecordService highLevelAuthRecordService;
    @Autowired
    UserCacheService userCacheService;
    @Autowired
    RealNameAuthRecordService realNameAuthRecordService;

    @Autowired
    TipService tipService;



    @PostMapping("/apply")
    @ApiOperation(value = "高级认证申请")
    public Result applyHighLevelAuth(@Valid  ApplyHighLevelAuthModel model) {
        User user = userCacheService.currentUser();
        if (!realNameAuthRecordService.isPass(user.getUserId())) {
            throw new YamiShopBindException("实名认证未通过，无法进行高级认证");
        }
        HighLevelAuthRecord highLevelAuthRecord = highLevelAuthRecordService.findByUserId(user.getUserId());
        if (highLevelAuthRecord != null) {
            String msg = "";
            switch (highLevelAuthRecord.getStatus()) {
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
                    highLevelAuthRecord.setStatus(1);
                    tipService.deleteTip(highLevelAuthRecord.getUuid());
                    highLevelAuthRecordService.removeById(highLevelAuthRecord);
                    highLevelAuthRecord=null;
                   // msg = MessageFormat.format("审核未通过,原因:{0}", record.getMsg());
                    break;
                default:
                    msg = "审核状态异常请联系客服";
                    break;
            }
            if (StrUtil.isNotBlank(msg)) {
                throw new YamiShopBindException(msg);
            }
        }

        if (highLevelAuthRecord==null){
            highLevelAuthRecord=new HighLevelAuthRecord();
        }

        if (StrUtil.isNotBlank(model.getWork_place())){
            if ( model.getWork_place().length()>245){
                throw new YamiShopBindException("工作地址长度超过245");
            }
        }

        if (StrUtil.isNotBlank(model.getHome_place())){
            if ( model.getHome_place().length()>245){
                throw new YamiShopBindException("家庭地址长度超过245");
            }
        }
        if (StrUtil.isNotBlank(model.getRelatives_relation())){
            if ( model.getRelatives_relation().length()>32){
                throw new YamiShopBindException("亲属电话长度超过32");
            }
        }
        if (StrUtil.isNotBlank(model.getRelatives_name())){
            if ( model.getRelatives_name().length()>32){
                throw new YamiShopBindException("亲属名称长度超过32");
            }
        }
        if (StrUtil.isNotBlank(model.getRelatives_place())){
            if ( model.getRelatives_place().length()>245){
                throw new YamiShopBindException("亲属地址长度超过245");
            }
        }

        if (StrUtil.isNotBlank(model.getRelatives_phone())){
            if ( model.getRelatives_phone().length()>32){
                throw new YamiShopBindException("亲属电话长度超过32");
            }
        }

        highLevelAuthRecord.setWorkPlace(model.getWork_place());
        highLevelAuthRecord.setHomePlace(model.getHome_place());
        highLevelAuthRecord.setRelativesRelation(model.getRelatives_relation());
        highLevelAuthRecord.setRelativesName(model.getRelatives_name());
        highLevelAuthRecord.setRelativesPlace(model.getRelatives_place());
        highLevelAuthRecord.setRelativesPhone(model.getRelatives_phone());
        highLevelAuthRecord.setStatus(1);
        highLevelAuthRecord.setUserId(user.getUserId());
        highLevelAuthRecord.setCreateTime(new Date());
        highLevelAuthRecordService.saveOrUpdate(highLevelAuthRecord);
        if (Constants.SECURITY_ROLE_MEMBER.equals(user.getRoleName())) {
            tipService.saveTip(highLevelAuthRecord.getUuid(), TipConstants.KYC_HIGH_LEVEL);
        }

        return Result.succeed(null);
    }

    @ApiOperation(value = "获取高级认证信息")
    @RequestMapping("get")
    public Result<HighLevelAuthRecordDto> get() {
        User user = userCacheService.currentUser();
        HighLevelAuthRecord highLevelAuthRecord = highLevelAuthRecordService.findByUserId(user.getUserId());
        RealNameAuthRecord realNameAuthRecord = realNameAuthRecordService.getByUserId(user.getUserId());
        HighLevelAuthRecordDto dto = new HighLevelAuthRecordDto();
        if(null != realNameAuthRecord) {
            dto.setRealName(realNameAuthRecord.getName());
        }
        if (highLevelAuthRecord==null){
            return  Result.succeed(dto);
        }

        BeanUtils.copyProperties(highLevelAuthRecord, dto);
        dto.setWork_place(highLevelAuthRecord.getWorkPlace());
        dto.setHome_place(highLevelAuthRecord.getHomePlace());
        dto.setRelatives_relation(highLevelAuthRecord.getRelativesRelation());
        dto.setRelatives_name(highLevelAuthRecord.getRelativesName());
        dto.setRelatives_place(highLevelAuthRecord.getRelativesPlace());
        dto.setRelatives_phone(highLevelAuthRecord.getRelativesPhone());
        return Result.succeed(dto);
    }
}
