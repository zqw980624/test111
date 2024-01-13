package com.yami.trading.admin.controller.tip;

import com.qiniu.util.StringUtils;
import com.yami.trading.admin.model.tip.NewTipsModel;
import com.yami.trading.common.domain.Result;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.system.TipService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags = "管理员消息通知")
@RequestMapping("tip")
public class TipController {
    @Autowired
    TipService tipService;

    @GetMapping("getNewTips")
    @ApiOperation("获取最新通知")
    public Result<List<Map<String, Object>>> getNewTips(@RequestBody @Valid NewTipsModel model) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (!StringUtils.isNullOrEmpty(model.getModel())) {
           // System.out.println("cacheNewTipsByModel");
            list = tipService.cacheNewTipsByModel(SecurityUtils.getSysUser().getUsername(), model.getTimeStamp(), model.getModel());
        } else {
           // System.out.println("cacheNewTips");
            list = tipService.cacheNewTips(SecurityUtils.getSysUser().getUsername(), model.getTimeStamp());
        }
//        List<NewTipsDto> result = new ArrayList<>();
//        for (Map<String, Object> map : list) {
//            map.put("null","");
//            //String s = JSON.toJSONString(map);
//            //System.out.println(s);
//            NewTipsDto newTipsDto=new NewTipsDto();
//            newTipsDto.setTipDomName(map.keySet().toString());
//            newTipsDto.setTipContentSum((String) map.getOrDefault("tip_content_sum","0"));
//            newTipsDto.setTipMessage((String) map.getOrDefault("tip_message",""));
//            newTipsDto.setTipShow(map.getOrDefault("tip_show","false").toString());
//            newTipsDto.setTipUrl((String) map.getOrDefault("tip_url",""));
//
//
//
//
//            result.add(newTipsDto);
//        }
        return Result.ok(list);
    }

    @GetMapping("getTips")
    @ApiOperation("获取通知")
    public Result<Map<String, Object>> getTips() {
//        if (!StringUtils.isNullOrEmpty(getLoginPartyId())) {
//            return "";
//        }
        String userName= SecurityUtils.getSysUser().getUsername();
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("tipList", tipService.cacheSumTips(userName));
        return Result.ok(result);
    }

}
