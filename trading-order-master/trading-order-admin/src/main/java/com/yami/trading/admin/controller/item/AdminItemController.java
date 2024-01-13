package com.yami.trading.admin.controller.item;

import javax.validation.Valid;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.item.dto.ItemConfig;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.lang.LangUtils;
import com.yami.trading.common.query.QueryWrapperGenerator;

import com.yami.trading.common.util.StringUtils;
import com.yami.trading.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.yami.trading.bean.item.dto.ItemDTO;
import com.yami.trading.bean.item.mapstruct.ItemWrapper;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.bean.item.query.ItemQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 产品Controller
 * @author lucas
 * @version 2023-03-10
 */

@Api(tags ="永续合约管理和行情品种")
@RestController
@RequestMapping(value = "normal/adminItemAction!")
public class AdminItemController {

	@Autowired
	private ItemService itemService;

	@Autowired
	private ItemWrapper itemWrapper;
	@Autowired
	private UserService userService;

	/**
	 * 产品列表数据
	 */
	@ApiOperation(value = "永续合约列表，配置列表")
	@GetMapping("list")
	public Result<IPage<ItemDTO>> list(ItemQuery itemQuery, Page<ItemDTO> page) throws Exception {
		QueryWrapper queryWrapper = QueryWrapperGenerator.buildQueryCondition (itemQuery, ItemQuery.class);
		IPage<ItemDTO> result = itemService.findPage (page, queryWrapper);
		return Result.ok (result);
	}

	@ApiOperation(value = "设置前端显示状态,返回修改后的状态")
	@GetMapping("setShowStatus")
	public Result<String> setShowStatus(String symbol, String showStatus) throws Exception {
		Map<String, Object> data = new HashMap<String, Object>();
		/*QueryWrapper<Item> itemSummaryQueryWrapper = new QueryWrapper<>();
		itemSummaryQueryWrapper.eq("symbol", symbol);
		List<Item> list = itemService.list(itemSummaryQueryWrapper);
		Item bySymbol = itemService.findBySymbol(list.get(0).getSymbol());*/
		Item bySymbol = itemService.findBySymbol(symbol);
		if(bySymbol == null){
			throw new YamiShopBindException( symbol + "不存在");
		}
		Item update = new Item();
		update.setUuid(bySymbol.getUuid());
		update.setShowStatus(showStatus);

		itemService.updateById(update);
		return Result.ok(showStatus);
	}

	@ApiOperation(value = "设置交易状态, 返回修改后的状态")
	@GetMapping("setTradeStatus")
	public Result<String> setTradeStatus(String symbol, String tradeStatus) throws Exception {
		/*QueryWrapper<Item> itemSummaryQueryWrapper = new QueryWrapper<>();
		itemSummaryQueryWrapper.eq("symbol", symbol);
		List<Item> list = itemService.list(itemSummaryQueryWrapper);
		Item bySymbol = itemService.findBySymbol(list.get(0).getSymbol());*/
		Item bySymbol = itemService.findBySymbol(symbol);
		if(bySymbol == null){
			throw new YamiShopBindException( symbol + "不存在");
		}
		Item update = new Item();
		update.setUuid(bySymbol.getUuid());
		update.setTradeStatus(tradeStatus);

		itemService.updateById(update);
		return Result.ok(tradeStatus);
	}
	/**
	 * 根据Id获取产品数据
	 */
	@ApiOperation(value = "根据Id获取产品数据")
	@GetMapping("queryById")
	public Result<ItemDTO> queryById(String id) {
		return Result.ok ( itemService.findById ( id ) );
	}

	/**
	 * 保存产品
	 */
	@ApiOperation(value = "保存产品")
	@PostMapping("save")
	public  Result <String> save(@Valid @RequestBody Item item) {
		//新增或编辑表单保存
		itemService.saveOrUpdate (item);
        return Result.ok ( "保存产品成功" );
	}

	/**
	 * 保存产品
	 */
	@ApiOperation(value = "保存配置")
	@PostMapping("addConfig.action")
	public  Result <String> addConfig(@Valid @RequestBody ItemConfig itemConfig) {
		//userService.checkLoginSafeword(SecurityUtils.getSysUser().getUserId().toString(), itemConfig.getLoginSafeword());
		if(StrUtil.isNotBlank(itemConfig.getUuid())){
			throw new YamiShopBindException("新增配置不要传入uuid");
		}
		Item bySymbol = itemService.findBySymbol(itemConfig.getSymbol());
		if(bySymbol != null){
			throw new YamiShopBindException("代码已经存在");
		}
		Item item = itemWrapper.toEntity(itemConfig);
		//新增或编辑表单保存
		itemService.save (item);
		// todo log
		return Result.ok ( "保存行情品种成功" );
	}


	/**
	 * 保存永续合约
	 */
	@ApiOperation(value = "保存永续合约")
	@PostMapping("add.action")
	public  Result <String> addConfig(@Valid @RequestBody ItemDTO itemDTO) {
		//userService.checkLoginSafeword(SecurityUtils.getSysUser().getUserId().toString(), itemDTO.getLoginSafeword());
		if(StrUtil.isNotBlank(itemDTO.getUuid())){
			throw new YamiShopBindException("新增配置不要传入uuid");
		}
		Item bySymbol = itemService.findBySymbol(itemDTO.getSymbol());
		if(bySymbol != null){
			throw new YamiShopBindException("代码已经存在");
		}
		Item item = itemWrapper.toEntity(itemDTO);
		//新增或编辑表单保存
		itemService.save (item);
		// todo log
		return Result.ok ( "保存永续合约成功" );
	}

	/**
	 * 保存产品
	 */
	@ApiOperation(value = "更新产品/修改交易对/修改状态")
	@PostMapping("update.action")
	public  Result <String> updateConfig(@Valid @RequestBody ItemDTO itemDTO) {
	 //	userService.checkLoginSafeword(SecurityUtils.getSysUser().getUserId().toString(), itemDTO.getLoginSafeword());
		if(StrUtil.isBlank(itemDTO.getUuid())){
			throw new YamiShopBindException("更新数据时候uuid不能为空");
		}
		Item byId = itemService.getById(itemDTO.getUuid());
		if(byId == null){
			throw new YamiShopBindException("更新永续合约不存在");
		}
		// 新老代码不等
		if (!byId.getSymbol().equalsIgnoreCase(itemDTO.getSymbol())) {
			// 且老的名称已经存在
			Item bySymbol = itemService.findBySymbol(itemDTO.getSymbol());
			if(bySymbol != null ){
				throw new YamiShopBindException("被更新的永续合约已经存在");
			}
		}
		Item item = itemWrapper.toEntity(itemDTO);
		//新增或编辑表单保存
		itemService.saveOrUpdate (item);
		// todo log
		return Result.ok ( "保存行情永续合约成功" );
	}

	/**
	 * 保存产品
	 */
	@ApiOperation(value = "更新配置")
	@PostMapping("updateConfig.action")
	public  Result <String> updateConfig(@Valid @RequestBody ItemConfig itemConfig) {
		//userService.checkLoginSafeword(SecurityUtils.getSysUser().getUserId().toString(), itemConfig.getLoginSafeword());
		if(StrUtil.isBlank(itemConfig.getUuid())){
			throw new YamiShopBindException("更新数据时候uuid不能为空");
		}
		Item byId = itemService.getById(itemConfig.getUuid());
		if(byId == null){
			throw new YamiShopBindException("更新品种不存在");
		}
		// 新老代码不等
		if (!byId.getSymbol().equalsIgnoreCase(itemConfig.getSymbol())) {
			// 且老的名称已经存在
			Item bySymbol = itemService.findBySymbol(itemConfig.getSymbol());
			if(bySymbol != null ){
				throw new YamiShopBindException("被更新的品种已经存在");
			}
		}
		Item item = itemWrapper.toEntity(itemConfig);
		//新增或编辑表单保存
		itemService.saveOrUpdate (item);
		// todo log
		return Result.ok ( "保存行情品种成功" );
	}
	/**
	 * 删除产品
	 */
	@ApiOperation(value = "删除产品")
	@DeleteMapping("delete")
	public Result <String> delete(String ids) {
		String idArray[] = ids.split(",");
        itemService.removeByIds ( Lists.newArrayList ( idArray ) );
		return Result.ok( "删除产品成功" );
	}

}
