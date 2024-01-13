package com.yami.trading.service.purchasing.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.trading.bean.purchasing.ProjectBreed;
import com.yami.trading.bean.purchasing.ProjectVariety;
import com.yami.trading.bean.purchasing.dto.RelatedStockDto;
import com.yami.trading.dao.purchasing.ProjectVarietyMapper;
import com.yami.trading.service.purchasing.ProjectVarietyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProjectVarietyServiceImpl  extends ServiceImpl<ProjectVarietyMapper, ProjectVariety> implements ProjectVarietyService {
    @Override
    public List<ProjectVariety> findByProjectBreedId(String projectBreedId) {
        return list( Wrappers.<ProjectVariety>query().lambda().eq(ProjectVariety::getProjectBreedId,projectBreedId));
    }

    @Override
    public void deleteByProjectBreedId(String projectBreedId) {
        remove(Wrappers.<ProjectVariety>query().lambda().eq(ProjectVariety::getProjectBreedId,projectBreedId));
    }
}
