package com.yami.trading.admin.controller.item;
import com.yami.trading.admin.task.summary.SummaryCrawl;
import com.yami.trading.bean.item.domain.ItemSummary;
import com.yami.trading.common.domain.Result;
import javax.validation.Valid;
import com.google.common.collect.Lists;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.query.QueryWrapperGenerator;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.yami.trading.bean.item.dto.ItemSummaryDTO;
import com.yami.trading.bean.item.mapstruct.ItemSummaryWrapper;
import com.yami.trading.service.item.ItemSummaryService;
import com.yami.trading.bean.item.query.ItemSummaryQuery;




/**
 * 简况Controller
 * @author lucas
 * @version 2023-05-01
 */

@Api(tags ="简况")
@RestController
@RequestMapping(value = "/item/itemSummary")
public class AdminItemSummaryController {

	@Autowired
	private ItemSummaryService itemSummaryService;

	@Autowired
	private SummaryCrawl summaryCrawl;
	/**
	 * 根据Id获取简况数据
	 */
	@ApiOperation(value = "根据symbol获取简况数据")
	@GetMapping("queryBySymbol")
	public Result<ItemSummary> queryById(String symbol) {
		return Result.ok (itemSummaryService.getOrNewOne ( symbol ) );
	}

	@ApiOperation(value = "根据symbol获取简况数据")
	@GetMapping("crawl")
	public Result<String> crawl(String symbol) {
		summaryCrawl.crawl();
		return Result.ok("success");
	}
	/**
	 * 保存简况
	 */
	@ApiOperation(value = "修改简况")
	@PostMapping("save")
	public  Result <String> save(@Valid @RequestBody ItemSummary itemSummaryDTO) {
		String uuid = itemSummaryDTO.getUuid();
		ItemSummaryDTO byId = itemSummaryService.findById(uuid);
		if(byId == null){
			throw  new YamiShopBindException("简况不存在");
		}
		//新增或编辑表单保存
		itemSummaryService.updateById (itemSummaryDTO);
        return Result.ok ( "保存简况成功" );
	}


}
