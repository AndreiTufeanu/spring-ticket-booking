package com.andreitufeanu.backend.event.service;

import com.andreitufeanu.backend.event.dto.CategoryDto;
import com.andreitufeanu.backend.event.dto.CreateCategoryDto;
import com.andreitufeanu.backend.event.entity.Category;
import com.andreitufeanu.backend.event.mapper.CategoryMapper;
import com.andreitufeanu.backend.event.repository.CategoryRepository;
import com.andreitufeanu.backend.event.repository.EventRepository;
import com.andreitufeanu.backend.exceptions.ConflictException;
import com.andreitufeanu.backend.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryDto> getCategories() {
        return categoryRepository
                .findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional
    public CategoryDto createCategory(CreateCategoryDto dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.name())) {
            throw new ConflictException(
                    "Category already exists: " + dto.name()
            );
        }

        Category category = categoryRepository.save(
                categoryMapper.toEntity(dto)
        );

        log.info("Category {} created successfully", category.getId());

        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void deleteCategoryById(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Category not found: " + id);
        }

        if (eventRepository.existsByCategoriesId(id)) {
            throw new ConflictException(
                    "Category cannot be deleted because it is used by one or more events: " + id
            );
        }

        categoryRepository.deleteById(id);

        log.info("Category {} deleted successfully", id);
    }
}
