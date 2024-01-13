package com.yami.trading.service.purchasing;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.purchasing.ProjectBreed;
import com.yami.trading.bean.purchasing.ProjectVariety;
import com.yami.trading.bean.purchasing.dto.RelatedStockDto;

import java.util.List;

public interface  ProjectVarietyService   extends IService<ProjectVariety> {
    List<ProjectVariety> findByProjectBreedId(String projectBreedId);

    void deleteByProjectBreedId(String projectBreedId);
}
