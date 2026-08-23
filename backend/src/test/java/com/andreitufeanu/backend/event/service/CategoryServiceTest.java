package com.andreitufeanu.backend.event.service;

import com.andreitufeanu.backend.event.dto.CategoryDto;
import com.andreitufeanu.backend.event.dto.CreateCategoryDto;
import com.andreitufeanu.backend.event.entity.Category;
import com.andreitufeanu.backend.event.mapper.CategoryMapper;
import com.andreitufeanu.backend.event.repository.CategoryRepository;
import com.andreitufeanu.backend.event.repository.EventRepository;
import com.andreitufeanu.backend.exceptions.ConflictException;
import com.andreitufeanu.backend.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private UUID categoryId;
    private String categoryName;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        categoryName = "Conference";
        category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
    }

    @Test
    void getCategories_ShouldReturnListOfCategoryDtos() {
        Category anotherCategory = new Category();
        anotherCategory.setId(UUID.randomUUID());
        anotherCategory.setName("Workshop");
        List<Category> categories = List.of(category, anotherCategory);
        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toResponse(category)).thenReturn(new CategoryDto(categoryId, categoryName));
        when(categoryMapper.toResponse(anotherCategory)).thenReturn(new CategoryDto(anotherCategory.getId(), anotherCategory.getName()));

        List<CategoryDto> result = categoryService.getCategories();

        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactly("Conference", "Workshop");
        verify(categoryRepository).findAll();
        verify(categoryMapper, times(2)).toResponse(any(Category.class));
    }

    @Test
    void createCategory_ShouldSaveAndReturnCategoryDto() {
        CreateCategoryDto dto = new CreateCategoryDto("Tech");
        Category newCategory = new Category();
        newCategory.setName("Tech");
        when(categoryRepository.existsByNameIgnoreCase("Tech")).thenReturn(false);
        when(categoryMapper.toEntity(dto)).thenReturn(newCategory);
        Category savedCategory = new Category();
        savedCategory.setId(UUID.randomUUID());
        savedCategory.setName("Tech");
        when(categoryRepository.save(newCategory)).thenReturn(savedCategory);
        when(categoryMapper.toResponse(savedCategory)).thenReturn(new CategoryDto(savedCategory.getId(), "Tech"));

        CategoryDto result = categoryService.createCategory(dto);

        assertThat(result.name()).isEqualTo("Tech");
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Tech");
        verify(categoryRepository).existsByNameIgnoreCase("Tech");
    }

    @Test
    void createCategory_ShouldThrowConflictException_WhenNameAlreadyExists() {
        CreateCategoryDto dto = new CreateCategoryDto("Conference");
        when(categoryRepository.existsByNameIgnoreCase("Conference")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Category already exists");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteCategoryById_ShouldDelete_WhenCategoryExistsAndNotUsed() {
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(eventRepository.existsByCategoriesId(categoryId)).thenReturn(false);

        categoryService.deleteCategoryById(categoryId);

        verify(categoryRepository).deleteById(categoryId);
        verify(eventRepository).existsByCategoriesId(categoryId);
    }

    @Test
    void deleteCategoryById_ShouldThrowNotFoundException_WhenCategoryDoesNotExist() {
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.deleteCategoryById(categoryId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Category not found");
        verify(categoryRepository, never()).deleteById(any());
        verify(eventRepository, never()).existsByCategoriesId(any());
    }

    @Test
    void deleteCategoryById_ShouldThrowConflictException_WhenCategoryIsUsedByEvent() {
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(eventRepository.existsByCategoriesId(categoryId)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategoryById(categoryId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Category cannot be deleted because it is used by one or more events");
        verify(categoryRepository, never()).deleteById(any());
    }
}