package com.yami.trading.admin.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.model.UserSafewordApplyExamineModel;
import com.yami.trading.admin.model.UserSafewordApplyModel;
import com.yami.trading.bean.user.dto.UserSafewordApplyDto;
import com.yami.trading.common.domain.Result;
import com.yami.trading.service.AwsS3OSSFileService;
import com.yami.trading.service.RealNameAuthRecordService;
import com.yami.trading.service.user.UserSafewordApplyService;
import com.yami.trading.sys.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("userSafewordApply")
@Api(tags = "资金密码人工重置")
public class UserSafewordApplyController {

    @Autowired
    UserSafewordApplyService userSafewordApplyService;

    @Autowired
    RealNameAuthRecordService realNameAuthRecordService;


    @Autowired
    SysUserService sysUserService;

    @Autowired
    AwsS3OSSFileService awsS3OSSFileService;



    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page<UserSafewordApplyDto>> list(@RequestBody @Valid UserSafewordApplyModel request){
        Page<UserSafewordApplyDto> page=new Page(request.getCurrent(),request.getSize());
        userSafewordApplyService.listRecord(page,request.getRoleName(),
                request.getStatus(),request.getUserCode(),request.getUserName(),request.getOperate());

        for(UserSafewordApplyDto safewordApplyDto:page.getRecords()){
            safewordApplyDto.setIdcardPathFront(
                    awsS3OSSFileService.getUrl(safewordApplyDto.getIdcardPathBack()));
            safewordApplyDto.setIdcardPathBack(
                    awsS3OSSFileService.getUrl(safewordApplyDto.getIdcardPathBack()));
            safewordApplyDto.setIdcardPathHold(
                    awsS3OSSFileService.getUrl(safewordApplyDto.getIdcardPathHold()));
        }
        return  Result.ok(page);
    }

    @ApiOperation(value = "审核")
    @PostMapping("examine")

    public Result<?> examine(@RequestBody @Valid UserSafewordApplyExamineModel model){
        if (model.getType()==1){
            sysUserService.checkSafeWord(model.getLoginSafeword());
        }
        userSafewordApplyService.examine(model.getId(),model.getContent(),model.getType());
        return  Result.ok(null);
    }
}
