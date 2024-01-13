package com.yami.trading.admin.controller.future;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.facade.PermissionFacade;
import com.yami.trading.admin.facade.ProfitAndLossConfigServiceFacade;
import com.yami.trading.bean.future.domain.ProfitLossConfig;
import com.yami.trading.bean.future.dto.ProfitLossConfigAdd;
import com.yami.trading.bean.future.dto.ProfitLossConfigDTO;
import com.yami.trading.bean.future.dto.ProfitLossConfigUpdate;
import com.yami.trading.bean.future.mapstruct.ProfitLossConfigWrapper;
import com.yami.trading.bean.future.query.ProfitLossConfigQuery;
import com.yami.trading.bean.model.User;
import com.yami.trading.common.constants.Constants;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.BusinessException;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.security.common.util.SecurityUtils;
import com.yami.trading.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;


/**
 * 交割合约Controller
 *
 * @author lucas
 * @version 2023-04-08
 */
@Slf4j
@Api(tags = "【管理后台】交割场控设置")
@RestController
@RequestMapping(value = "normal/adminProfitAndLossConfigAction!")
public class ProfitLossConfigController {

    @Autowired
    private ProfitAndLossConfigServiceFacade profitAndLossConfigServiceFacade;
	@Autowired
    private UserService userService;
	@Autowired
	private PermissionFacade permissionFacade;
	@Autowired
	private ProfitLossConfigWrapper wrapper;
    /**
     * 交割合约列表数据
     */
    @ApiOperation(value = "获取 交割场控设置 列表")
    @GetMapping("list.action")
    public Result<IPage<ProfitLossConfigDTO>> list(ProfitLossConfigQuery profitLossConfigQuery, Page<ProfitLossConfig> page) throws Exception {
		profitLossConfigQuery.setChildren(permissionFacade.getOwnerUserIds());

		IPage<ProfitLossConfigDTO> result = profitAndLossConfigServiceFacade.getProfitLossConfigService().listRecord(page, profitLossConfigQuery);
        return Result.ok(result);
    }


    /**
     * 交割合约列表数据
     */
    @ApiOperation(value = "新增 交割场控设置 下拉配置")
    @GetMapping("config.action")
    public Result<Map<String, String>> list() {
        return Result.ok(Constants.PROFIT_LOSS_TYPE);
    }

	/**
	 * 交割合约列表数据
	 */
	@ApiOperation(value = "获取交割场控设置详情")
	@GetMapping("get.action")
	public Result<ProfitLossConfigDTO> findById(@RequestParam String id) {
		ProfitLossConfig profitLossConfig = profitAndLossConfigServiceFacade.getProfitLossConfigService().getById(id);
		User party = this.userService.getById(profitLossConfig.getPartyId());
		ProfitLossConfigDTO profitLossConfigDTO = wrapper.toDTO(profitLossConfig);
		profitLossConfigDTO.setUserCode(party.getUserCode());
		profitLossConfigDTO.setUserName(party.getUserName());
		return Result.ok(profitLossConfigDTO);
	}
    /**
     *
     */
    @ApiOperation(value = "新增 交割场控设置")
    @PostMapping("add.action")
    public Result<String> add(@RequestBody @Valid ProfitLossConfigAdd profitLossConfigAdd) {
        String usercode = profitLossConfigAdd.getUserCode();
        String type = profitLossConfigAdd.getType();
        String remark = profitLossConfigAdd.getRemark();



        try {
            User party = userService.findUserByUserCode(usercode);
            if (null == party) {
                throw new YamiShopBindException("用户不存在");
            }

            // todo代理上逻辑
			List<String> ownerUserIds = permissionFacade.getOwnerUserIds();

            if(ownerUserIds!=null &&!ownerUserIds.contains(usercode)){
				throw new BusinessException("用户不存在或者不属于登录用户名下");
			}

            ProfitLossConfig profitAndLossConfig = new ProfitLossConfig();
            profitAndLossConfig.setType(type);
            profitAndLossConfig.setRemark(remark);
            profitAndLossConfig.setPartyId(party.getUserId());
			String opName = SecurityUtils.getSysUser().getUsername();
            this.profitAndLossConfigServiceFacade.save(profitAndLossConfig, opName);

        } catch (Exception e) {
        	log.error("保存场控失败", e);
        	throw new YamiShopBindException("保存场控失败:"+ e.getMessage());
        }
		return Result.ok("操作成功");
    }

    /**
     * 保存交割合约
     */
    @ApiOperation(value = "修改 交割场控设置")
    @PostMapping("update.action")
    public Result<String> update(@Valid @RequestBody ProfitLossConfigUpdate update) {
		ProfitLossConfig profitAndLossConfig = this.profitAndLossConfigServiceFacade.getProfitLossConfigService().getById(update.getUuid());
		User party = userService.getById(profitAndLossConfig.getPartyId());
		if (null == party) {
			throw new YamiShopBindException("用户不存在");
		}
		String opName = SecurityUtils.getSysUser().getUsername();
		// todo 代理商数据权限验证
		/**
		 * List<String> childrens = this.userRecomService.findChildren(party_login.getId());
		 *
		 * 				double isChildren = 0;
		 * 				if (childrens != null) {
		 * 					for (String children : childrens) {
		 * 						if (party.getId().equals(children)) {
		 * 							isChildren = 1;
		 *                                                }* 					}
		 * 				}
		 *
		 */
		profitAndLossConfig.setRemark(update.getRemark());
		profitAndLossConfig.setType(update.getType());
		profitAndLossConfigServiceFacade.update(profitAndLossConfig, opName);

		return Result.ok("保存交割合约成功");
    }


	/**
	 * 保存交割合约
	 */
	@ApiOperation(value = "删除 交割场控设置")
	@GetMapping("toDelete.action")
	public Result<String> toDelete(@RequestParam String uuid) {
		ProfitLossConfig profitAndLossConfig = this.profitAndLossConfigServiceFacade.getProfitLossConfigService().getById(uuid);
		User party = userService.getById(profitAndLossConfig.getPartyId());
		if (null == party) {
			throw new YamiShopBindException("用户不存在");
		}
		String opName = SecurityUtils.getSysUser().getUsername();
		// todo 代理商数据权限验证
		/**
		 * List<String> childrens = this.userRecomService.findChildren(party_login.getId());
		 *
		 * 				double isChildren = 0;
		 * 				if (childrens != null) {
		 * 					for (String children : childrens) {
		 * 						if (party.getId().equals(children)) {
		 * 							isChildren = 1;
		 *                                                }* 					}
		 * 				}
		 *
		 */
		profitAndLossConfigServiceFacade.delete(uuid, opName);

		return Result.ok("保存交割合约成功");
	}


}
