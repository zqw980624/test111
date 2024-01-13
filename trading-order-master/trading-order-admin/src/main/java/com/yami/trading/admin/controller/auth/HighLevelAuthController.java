package com.yami.trading.admin.controller.auth;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.model.RealNameExamineModel;
import com.yami.trading.admin.model.UserHighLevelAuthListModel;
import com.yami.trading.bean.model.HighLevelAuthRecord;
import com.yami.trading.bean.model.User;
import com.yami.trading.bean.user.dto.HighLevelAuthRecordDto;
import com.yami.trading.common.annotation.SysLog;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.service.HighLevelAuthRecordService;
import com.yami.trading.service.system.TipService;
import com.yami.trading.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;

@RestController
@RequestMapping("highLevelAuth")
@Api(tags = "高级认证")
public class HighLevelAuthController {
    @Autowired
    HighLevelAuthRecordService highLevelAuthRecordService;

    @Autowired
    UserService userService;

    @Autowired
    TipService tipService;

    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page<HighLevelAuthRecordDto>> list(@RequestBody @Valid UserHighLevelAuthListModel request){
        Page<HighLevelAuthRecordDto> page=new Page(request.getCurrent(),request.getSize());
        highLevelAuthRecordService.pageRecord(page,request.getRoleName(),
                request.getStatus(),request.getUserName());
        return  Result.ok(page);
    }

    @ApiOperation("查询用户信息")
    @GetMapping("getById/{id}")
    public Result<HighLevelAuthRecordDto> getById(@PathVariable String id) {
        if(StringUtils.isEmpty(id)) {
            return Result.failed("记录ID不能为空");
        }

        HighLevelAuthRecord highLevelAuthRecord = highLevelAuthRecordService.getOne(Wrappers.<HighLevelAuthRecord>lambdaQuery().eq(HighLevelAuthRecord::getUuid, id));

        HighLevelAuthRecordDto highLevelAuthRecordDto = new HighLevelAuthRecordDto();

        if(null != highLevelAuthRecord) {
            highLevelAuthRecordDto.setUuid(highLevelAuthRecord.getUuid());
            highLevelAuthRecordDto.setWorkPlace(highLevelAuthRecord.getWorkPlace());
            highLevelAuthRecordDto.setHomePlace(highLevelAuthRecord.getHomePlace());
            highLevelAuthRecordDto.setStatus(highLevelAuthRecord.getStatus());
            highLevelAuthRecordDto.setRelativesRelation(highLevelAuthRecord.getRelativesRelation());
            highLevelAuthRecordDto.setRelativesName(highLevelAuthRecord.getRelativesName());
            highLevelAuthRecordDto.setRelativesPhone(highLevelAuthRecord.getRelativesPhone());
            highLevelAuthRecordDto.setRelativesPlace(highLevelAuthRecord.getRelativesPlace());
            highLevelAuthRecordDto.setMsg(highLevelAuthRecord.getMsg());
        }
        return Result.succeed(highLevelAuthRecordDto, "查询成功");
    }

    @ApiOperation(value = "修改")
    @PutMapping("edit")
    public Result<?> edit(@RequestBody @Valid HighLevelAuthRecord record) {

        if(StringUtils.isEmpty(record.getUuid())) {
            return Result.failed("记录ID不能为空");
        }

        highLevelAuthRecordService.updateById(record);
        return Result.ok("修改成功");
    }

    @ApiOperation(value = "审核")
    @PostMapping("examine")
    @SysLog("用户高级认证-审核")
    public Result<?> examine(@RequestBody @Valid RealNameExamineModel model){
        HighLevelAuthRecord highLevelAuthRecord= highLevelAuthRecordService.getById(model.getId());
        if (highLevelAuthRecord==null){
            throw  new YamiShopBindException("参数错误");
        }
        int status=  highLevelAuthRecord.getStatus();
        if (model.getType()==1){
            if (status < 1){
                throw  new YamiShopBindException("认证记录已操作过了");
            }
            highLevelAuthRecord.setStatus(2);
            highLevelAuthRecord.setOperationTime(new Date());
            highLevelAuthRecordService.updateById(highLevelAuthRecord);

            User user= userService.getById(highLevelAuthRecord.getUserId());
            user.setHighlevelAuthority(true);
            // 获取用户系统等级：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证； 4/用户高级认证；
            int userLevelSystem = userService.getUserLevelByAuth(user);
            // 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
            // 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
            // 如：级别11表示：新注册的前端显示为VIP1；
            int userLevel = user.getUserLevel();
//            user.setUserLevel(((int) Math.floor(userLevel / 10)) * 10 + userLevelSystem);
            userService.updateById(user);
        }
        if (model.getType()==2){
            if (status > 1){
                throw  new YamiShopBindException("认证记录已操作过了");
            }
            highLevelAuthRecord.setStatus(3);
            highLevelAuthRecord.setMsg(model.getContent());
            highLevelAuthRecord.setOperationTime(new Date());
            highLevelAuthRecordService.updateById(highLevelAuthRecord);
        }

        tipService.deleteTip(highLevelAuthRecord.getUuid());
        return  Result.ok(null);
    }
}
