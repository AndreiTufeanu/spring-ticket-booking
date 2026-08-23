package com.andreitufeanu.backend.event.mapper;

import com.andreitufeanu.backend.event.dto.*;
import com.andreitufeanu.backend.event.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto toResponse(Category category);

    @Mapping(target = "id", ignore = true)
    Category toEntity(CreateCategoryDto dto);
}
