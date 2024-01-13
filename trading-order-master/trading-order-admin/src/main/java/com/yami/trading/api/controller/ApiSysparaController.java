package com.yami.trading.api.controller;

import com.yami.trading.common.domain.Result;
import com.yami.trading.service.syspara.LocalSysparaService;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Data
@ApiModel
public class ApiSysparaController {



    @Autowired
    LocalSysparaService localSysparaService;

    /**
     * 可逗号相隔，查询多个参数值。 exchange_rate_out 兑出货币和汇率; exchange_rate_in
     * 兑入货币和汇率;withdraw_fee 提现手续费，type=fixed是单笔固定金额，=rate是百分比，结果到小数点2位。
     * index_top_symbols 首页显示的4个品种。customer_service_url 在线客服URL
     */

    @RequestMapping("api/syspara!getSyspara.action")
    public Result getSyspara(HttpServletRequest request) {

            String code = request.getParameter("code");
            Map<String, Object> data = localSysparaService.find(code);

        return Result.succeed(data);
    }
}
