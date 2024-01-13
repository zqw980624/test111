package com.yami.trading.common.mapstruct;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

public interface EntityWrapper<D, E> {

    E toEntity(D dto);

    D toDTO(E entity);

    List<E> toEntity(List <D> dtoList);


    List<D> toDTO(List <E> entityList);


    Page <E> toEntity(Page <D> page);

    Page <D> toDTO(Page <E> page);

}
