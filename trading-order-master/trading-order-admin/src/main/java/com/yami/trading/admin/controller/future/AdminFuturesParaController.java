package com.yami.trading.admin.controller.future;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.facade.FuturesParaFacade;
import com.yami.trading.bean.future.domain.FuturesPara;
import com.yami.trading.bean.future.dto.FuturesParaDTO;
import com.yami.trading.bean.future.mapstruct.TFuturesParaWrapper;
import com.yami.trading.bean.future.query.FuturesParaQuery;
import com.yami.trading.bean.item.dto.ItemDTO;
import com.yami.trading.bean.item.query.ItemQuery;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.query.QueryWrapperGenerator;
import com.yami.trading.common.util.IPHelper;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.future.FuturesParaService;
import com.yami.trading.service.item.ItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;


/**
 * 交割合约管理Controller
 *
 * @author lucas
 * @version 2023-04-08
 */

@Api(tags = "【管理后台】交割合约 交易参数管理")
@RestController
@RequestMapping(value = "normal/adminContractManageAction!")
public class AdminFuturesParaController {

    @Autowired
    private FuturesParaFacade futuresParaFacade;

    @Autowired
    private TFuturesParaWrapper tFuturesParaWrapper;
    @Autowired
    private ItemService itemService;

    /**
     * 获取 交易对 列表
     */
    @ApiOperation(value = "获取 交易对 列表")
    @GetMapping("list.action")
    public Result<IPage<ItemDTO>> list(ItemQuery itemQuery, Page<ItemDTO> page) throws Exception {
        QueryWrapper queryWrapper = QueryWrapperGenerator.buildQueryCondition(itemQuery, ItemQuery.class);
        IPage<ItemDTO> result = itemService.findPage(page, queryWrapper);
        return Result.ok(result);
    }

    /**
     * 交割合约管理列表数据
     */
    @ApiOperation(value = "获取交易参数列表")
    @GetMapping("listPara.action")
    public Result<IPage<FuturesPara>> list(FuturesParaQuery futuresParaQuery, Page<FuturesPara> page) throws Exception {
        QueryWrapper<FuturesPara> queryWrapper = new QueryWrapper();
        queryWrapper.eq(StrUtil.isNotBlank(futuresParaQuery.getSymbol()),"symbol", futuresParaQuery.getSymbol());
        IPage<FuturesPara> result = futuresParaFacade.getFuturesParaService().page(page, queryWrapper);
        result.getRecords().forEach(f -> {
            f.setProfitRatio(f.getProfitRatio().multiply(BigDecimal.valueOf(100)));
            f.setProfitRatioMax(f.getProfitRatioMax().multiply(BigDecimal.valueOf(100)));
            f.setUnitFee(f.getUnitFee().multiply(BigDecimal.valueOf(100)));
        });

        return Result.ok(result);
    }


    /**
     * 根据Id获取交割合约管理数据
     */
    @ApiOperation(value = "根据Id获取交割合约管理数据")
    @GetMapping("queryById.action")
    public Result<FuturesParaDTO> queryById(String id) {
        FuturesParaService futuresParaService = futuresParaFacade.getFuturesParaService();
        FuturesParaDTO body = tFuturesParaWrapper.toDTO(futuresParaService.getById(id));
        body.mutiply();
        return Result.ok(body);
    }

    /**
     * 根据Id获取交割合约管理数据
     */
    @ApiOperation(value = "新增,修改交易参数")
    @PostMapping("addFutures.action")
    public Result<String> addFutures(@Valid @RequestBody FuturesParaDTO futuresParaDTO) {
        if (futuresParaDTO.getUnitMaxAmount() == null) {
            futuresParaDTO.setUnitMaxAmount(BigDecimal.ZERO);
        }
        BigDecimal unitAmount = futuresParaDTO.getUnitAmount();
        BigDecimal unitMaxAmount = futuresParaDTO.getUnitMaxAmount();
        if (unitMaxAmount.compareTo(BigDecimal.ZERO) != 0 && unitMaxAmount.compareTo(unitAmount) < 0) {
            throw new YamiShopBindException("最高购买金额需大于最低购买金额");
        }
        futuresParaDTO.divide();
        FuturesPara futuresPara = tFuturesParaWrapper.toEntity(futuresParaDTO);
        String username = SecurityUtils.getSysUser().getUsername();
        futuresParaFacade.addFutures(futuresPara, IPHelper.getIpAddr(), username, futuresParaDTO.getLoginSafeword());
        return Result.ok("success");
    }

    /**
     * 删除 交易参数
     */
    @GetMapping("toDeleteFuturesPara.action")
    public Result<String> toDeleteFuturesPara(@RequestParam String futuresId,
                                                      @RequestParam String loginSafeword,
                                                      @RequestParam String superGoogleAuthCode
    ) {
        String username = SecurityUtils.getSysUser().getUsername();
        futuresParaFacade.deleteFuturesPara(futuresId, IPHelper.getIpAddr(), username, loginSafeword, superGoogleAuthCode);
        return Result.ok("success");

    }


}
