package com.andreitufeanu.backend.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryDto(
        @NotBlank(message = "Category name is required")
        @Size(max = 50, message = "Category name can't have more than {max} characters")
        String name
) {}