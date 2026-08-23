package com.andreitufeanu.backend.event.controller;

import com.andreitufeanu.backend.event.dto.CategoryDto;
import com.andreitufeanu.backend.event.dto.CreateCategoryDto;
import com.andreitufeanu.backend.event.service.CategoryService;
import com.andreitufeanu.backend.exceptions.ConflictException;
import com.andreitufeanu.backend.exceptions.NotFoundException;
import com.andreitufeanu.backend.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(CategoryControllerTest.TestSecurityConfig.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtService jwtService;

    private final UUID categoryId = UUID.randomUUID();

    @Test
    void getCategories_ShouldReturnList() throws Exception {
        CategoryDto dto1 = new CategoryDto(categoryId, "Conference");
        CategoryDto dto2 = new CategoryDto(UUID.randomUUID(), "Workshop");
        when(categoryService.getCategories()).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(categoryId.toString()))
                .andExpect(jsonPath("$[0].name").value("Conference"))
                .andExpect(jsonPath("$[1].name").value("Workshop"));
    }

    @Test
    void createCategory_ShouldReturnCreated() throws Exception {
        CreateCategoryDto dto = new CreateCategoryDto("Tech");
        CategoryDto response = new CategoryDto(UUID.randomUUID(), "Tech");
        when(categoryService.createCategory(any(CreateCategoryDto.class))).thenReturn(response);

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Tech"));
    }

    @Test
    void createCategory_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        CreateCategoryDto invalid = new CreateCategoryDto("");

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_ShouldReturnConflict_WhenNameAlreadyExists() throws Exception {
        CreateCategoryDto dto = new CreateCategoryDto("Existing");
        when(categoryService.createCategory(any(CreateCategoryDto.class)))
                .thenThrow(new ConflictException("Category already exists"));

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteCategory_ShouldReturnNoContent_WhenSuccess() throws Exception {
        doNothing().when(categoryService).deleteCategoryById(categoryId);

        mockMvc.perform(delete("/categories/{id}", categoryId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCategory_ShouldReturnNotFound_WhenCategoryDoesNotExist() throws Exception {
        doThrow(new NotFoundException("Category not found"))
                .when(categoryService).deleteCategoryById(categoryId);

        mockMvc.perform(delete("/categories/{id}", categoryId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory_ShouldReturnConflict_WhenCategoryUsedByEvent() throws Exception {
        doThrow(new ConflictException("Category cannot be deleted because it is used by one or more events"))
                .when(categoryService).deleteCategoryById(categoryId);

        mockMvc.perform(delete("/categories/{id}", categoryId))
                .andExpect(status().isConflict());
    }

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }
}