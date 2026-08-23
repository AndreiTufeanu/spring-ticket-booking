package com.andreitufeanu.backend.event.mapper;

import com.andreitufeanu.backend.event.dto.CategoryDto;
import com.andreitufeanu.backend.event.dto.CreateCategoryDto;
import com.andreitufeanu.backend.event.entity.Category;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryMapperTest {

    private final CategoryMapper mapper = Mappers.getMapper(CategoryMapper.class);

    @Test
    void toResponse_ShouldMapCategoryToDto() {
        Category category = new Category();
        UUID id = UUID.randomUUID();
        category.setId(id);
        category.setName("Conference");

        CategoryDto dto = mapper.toResponse(category);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo("Conference");
    }

    @Test
    void toEntity_ShouldMapCreateDtoToCategory_IgnoringId() {
        CreateCategoryDto dto = new CreateCategoryDto("Workshop");

        Category category = mapper.toEntity(dto);

        assertThat(category.getId()).isNull();
        assertThat(category.getName()).isEqualTo("Workshop");
    }
}