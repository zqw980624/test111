package com.yami.trading.service.purchasing.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.model.UserDataSum;
import com.yami.trading.bean.purchasing.ProjectBreed;
import com.yami.trading.bean.purchasing.ProjectVariety;
import com.yami.trading.bean.purchasing.dto.RelatedStockDto;
import com.yami.trading.dao.purchasing.ProjectBreedMapper;
import com.yami.trading.dao.user.UserDataSumMapper;
import com.yami.trading.service.purchasing.ProjectBreedService;
import com.yami.trading.service.purchasing.ProjectVarietyService;
import com.yami.trading.service.user.UserDataSumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProjectBreedServiceImpl extends ServiceImpl<ProjectBreedMapper, ProjectBreed> implements ProjectBreedService {
    @Autowired
    ProjectVarietyService projectVarietyService;

    @Override
    @Transactional
    public void saveProjectBreed(ProjectBreed projectBreed, List<RelatedStockDto> relatedStockVarieties) {
        save(projectBreed);
        for (RelatedStockDto dto : relatedStockVarieties) {
            ProjectVariety projectVariety = new ProjectVariety();
            projectVariety.setProjectBreedId(projectBreed.getUuid());
            projectVariety.setInitPrice(new BigDecimal(0));
            projectVariety.setStatus(1);
            projectVariety.setRelatedStockSymbol(dto.getSymbol());
            projectVariety.setRelatedStockSymbolName(dto.getName());
            projectVarietyService.save(projectVariety);
        }
    }

    @Override
    @Transactional
    public void updateProjectBreed(ProjectBreed projectBreed, List<RelatedStockDto> relatedStockVarieties) {
        updateById(projectBreed);
        projectVarietyService.deleteByProjectBreedId(projectBreed.getUuid());
        if (CollectionUtils.isNotEmpty(relatedStockVarieties)){
            for (RelatedStockDto dto : relatedStockVarieties) {
                ProjectVariety projectVariety = new ProjectVariety();
                projectVariety.setProjectBreedId(projectBreed.getUuid());
                projectVariety.setInitPrice(new BigDecimal(0));
                projectVariety.setStatus(1);
                projectVariety.setRelatedStockSymbol(dto.getSymbol());
                projectVariety.setRelatedStockSymbolName(dto.getName());
                projectVarietyService.save(projectVariety);
            }
        }

    }

    @Override
    @Transactional
    public void deleteProjectBreed(String id) {
        removeById(id);
        projectVarietyService.deleteByProjectBreedId(id);
    }
}
