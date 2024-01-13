package com.yami.trading.admin.controller.purchasing;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.trading.admin.controller.purchasing.model.AddTradeLeverageModel;
import com.yami.trading.admin.controller.purchasing.model.ListTradeLeverageModel;
import com.yami.trading.admin.controller.purchasing.model.PurchasingListModel;
import com.yami.trading.admin.model.IdModel;
import com.yami.trading.admin.model.log.LogListModel;
import com.yami.trading.admin.model.purchasing.PurchasingAddModel;
import com.yami.trading.admin.model.purchasing.PurchasingUpdateModel;
import com.yami.trading.bean.item.domain.Item;
import com.yami.trading.bean.log.dto.LogDto;
import com.yami.trading.bean.purchasing.Purchasing;
import com.yami.trading.bean.purchasing.TradeLeverage;
import com.yami.trading.common.domain.PageRequest;
import com.yami.trading.common.domain.Result;
import com.yami.trading.common.exception.YamiShopBindException;
import com.yami.trading.common.util.DateUtil;
import com.yami.trading.service.PurchasingService;
import com.yami.trading.service.item.ItemService;
import com.yami.trading.service.purchasing.TradeLeverageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("purchasing")
@Api(tags = "申购管理")
@Slf4j
public class PurchasingController {


    @Autowired
    PurchasingService purchasingService;

    @Autowired
    ItemService itemService;

    @Autowired
    TradeLeverageService tradeLeverageService;


    @ApiOperation(value = "列表")
    @PostMapping("list")
    public Result<Page<Purchasing>> list(@RequestBody @Valid PurchasingListModel model){
        Page<Purchasing> page=new Page(model.getCurrent(),model.getSize());
        LambdaQueryWrapper<Purchasing> wrapper= Wrappers.<Purchasing>query().lambda();
        if (!StrUtil.isEmpty(model.getProjectName())){
            wrapper .like(Purchasing::getProjectName,model.getProjectName());
        }
       if(model.getStatus()>0){
           if (model.getStatus()==1){
               Date now =new Date();
               wrapper.ge(Purchasing::getSubscriptionStartTime,now);
               wrapper.le(Purchasing::getSubscriptionEndTime,now);
           }
           if (model.getStatus()==2){
               Date now =new Date();
               wrapper.ge(Purchasing::getSubscriptionEndTime,now);
           }
       }
        wrapper.orderByDesc(Purchasing::getCreateTime);
        purchasingService.page(page,wrapper);
        for (Purchasing purchasing:page.getRecords()){
            Item item= itemService.findBySymbol(purchasing.getProjectTypeSymbol());
            if (item!=null){
                purchasing.setProjectTypeName(item.getName());
            }
        }
        return  Result.ok(page);
    }

    @ApiOperation(value = "新增")
    @PostMapping("add")
    public Result add(@RequestBody @Valid PurchasingAddModel addModel){
        Purchasing purchasing=new Purchasing();
        BeanUtils.copyProperties(addModel,purchasing);
        purchasingService.save(purchasing);
        return  Result.ok(null);
    }

    @ApiOperation(value = "更新")
    @PostMapping("update")
    public Result update(@RequestBody @Valid PurchasingUpdateModel updateModel){
       Purchasing purchasing=  purchasingService.getById(updateModel.getId());
       if (purchasing==null){
           throw  new YamiShopBindException("参数错误!");
       }
        BeanUtils.copyProperties(updateModel,purchasing);
        purchasingService.updateById(purchasing);
        return  Result.ok(null);
    }

    @ApiOperation(value = "删除")
    @PostMapping("delete")
    public Result delete(@RequestBody @Valid IdModel idModel){
        Purchasing purchasing=  purchasingService.getById(idModel.getId());
        if (purchasing==null){
            throw  new YamiShopBindException("参数错误!");
        }
        purchasingService.removeById(purchasing);
        return  Result.ok(null);
    }
}
