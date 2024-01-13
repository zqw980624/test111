package com.yami.trading.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.controller.user.model.AgentAllStatisticsModel;
import com.yami.trading.admin.controller.user.model.WalletExtendsAllModel;
import com.yami.trading.admin.dto.UserAllDto;
import com.yami.trading.admin.model.AllStatisticsModel;
import com.yami.trading.admin.model.UserStatisticsModel;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.util.DateUtil;
import com.yami.trading.common.util.DateUtils;
import com.yami.trading.common.util.StringUtils;
import com.yami.trading.service.user.UserDataService;
import com.yami.trading.service.user.UserRecomService;
import com.yami.trading.service.user.UserService;
import com.yami.trading.service.user.UserStatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import java.util.*;

@RestController
@Api(tags = "报表")
@RequestMapping("statistics")
public class UserStatisticsController {
    @Autowired
    UserDataService userDataService;
    @Autowired
    UserStatisticsService userStatisticsService;

    @Autowired
    UserService userService;

    @Autowired
    UserRecomService userRecomService;

    @PostMapping(value = "userList")
    @ApiOperation("用户收益报表")
    public Result<Page> listTotal(@RequestBody @Valid UserStatisticsModel request) {
        Page page = new Page(request.getCurrent(), request.getSize());
        List children=null;
        if (!StringUtils.isNullOrEmpty(request.getUserId())) {
            children= userRecomService.findChildren(request.getUserId());
            if (children.size() == 0) {
                return Result.succeed(new Page());
            }
        }
        if (request.getStartTime()==null&&request.getEndTime()!=null){
            request.setStartTime(DateUtil.minDate(request.getStartTime()));
        }
        if (request.getEndTime()==null&&request.getStartTime()!=null){
            request.setEndTime(DateUtil.maxDate(request.getEndTime()));
        }
        userDataService.listUserGenefits(page, request.getStartTime(), request.getEndTime(), request.getUser_name(),children);
        return Result.ok(page);
    }

    /**
     *
     */
    @ApiOperation("用户钱包 登录者只能看自己下面的用户钱包")
    @PostMapping("walletExtendsAll")
    public Result<Map<String, Object>> walletExtendsAll(@RequestBody WalletExtendsAllModel walletExtendsAllModel) {

        User user=userService.findUserByUserCode(walletExtendsAllModel.getUserId());
        List<Map<String, Object>> wallet_data = userStatisticsService.getWalletExtends(null, user.getUserId());
        List<String> types = new ArrayList<String>();
        Map<String, Object> map = new HashMap<>();
        map.put("wallet_data", wallet_data);
        if (ObjectUtils.isNotEmpty(wallet_data)) {
            for (Map<String, Object> walletMap : wallet_data) {
                types.add(walletMap.get("wallettype").toString());
            }
        }
        String wallettype = String.join(",", types);
        map.put("wallet_type_arr", wallettype);
        return Result.succeed(map);
    }

    @PostMapping(value = "userAll")
    @ApiOperation("总充提报表")
    public Result<UserAllDto> userAll(@RequestBody @Valid AllStatisticsModel request) {
        Page page = new Page(request.getCurrent(), request.getSize());
        String paraTime = request.getParaTime();
        Date startTime = null;
        Date endTime=null;
        Date now=new Date();
        System.out.printf(request.getStartTime()+"========"+request.getEndTime());
        if (request.getStartTime()!=null ||request.getEndTime()!=null ){
            endTime=DateUtil.maxDate(request.getEndTime());
            startTime=DateUtil.minDate(request.getStartTime());
        }
        else {
            if ("day".equals(paraTime)) {
                // 当天
                endTime = DateUtil.maxDate(now);
                startTime = DateUtil.minDate(now);
            } else if ("week".equals(paraTime)) {
                // 往前推7天
                endTime = DateUtil.maxDate(now);
                startTime = DateUtil.minDate(DateUtils.addDate(new Date(), -7));
            } else if ("month".equals(paraTime)) {
                // 往前推一月
                endTime = DateUtil.maxDate(now);
                startTime= DateUtil.minDate(DateUtils.addMonth(new Date(), -1));
            } else if ("all".equals(paraTime)) {
                // 所有数据
                endTime = null;
                startTime = null;
            }
        }

        UserAllDto userAllDto=new UserAllDto();
        userAllDto.setList(userDataService.userAll(page,startTime,endTime));
        userAllDto.setSumData( userDataService.sumAll(startTime,endTime));
        return Result.ok(userAllDto);
    }

    @PostMapping(value = "agentAllStatistics")
    @ApiOperation("代理商充提报表")
    public Result<Page> agentAllStatistics(@RequestBody AgentAllStatisticsModel model){
        return  Result.succeed(
                userService.getAgentAllStatistics(model.getCurrent(),model.getSize(),model.getStartTime(),
                        model.getEndTime(),model.getUsername(),model.getUserId()));
    }

}
