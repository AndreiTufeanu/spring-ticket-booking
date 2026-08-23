package com.andreitufeanu.backend.event.controller;

import com.andreitufeanu.backend.event.dto.CategoryDto;
import com.andreitufeanu.backend.event.dto.CreateCategoryDto;
import com.andreitufeanu.backend.event.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto> createCategory(@RequestBody @Valid CreateCategoryDto createCategoryDto) {
        var createdCategory = categoryService.createCategory(createCategoryDto);

        return ResponseEntity
                .created(URI.create("/categories/" + createdCategory.id()))
                .body(createdCategory);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategoryById(id);

        return ResponseEntity.noContent().build();
    }
}
