package com.yami.trading.service.purchasing;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.trading.bean.purchasing.ProjectBreed;
import com.yami.trading.bean.purchasing.dto.RelatedStockDto;

import java.util.List;

public interface ProjectBreedService extends IService<ProjectBreed> {
    void saveProjectBreed(ProjectBreed projectBreed, List<RelatedStockDto> relatedStockVarieties);

    void updateProjectBreed(ProjectBreed projectBreed, List<RelatedStockDto> relatedStockVarieties);

    void deleteProjectBreed(String id);
}
