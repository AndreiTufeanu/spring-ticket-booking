package com.andreitufeanu.backend.event.repository;

import com.andreitufeanu.backend.event.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        category1 = new Category();
        category1.setName("Conference");
        entityManager.persist(category1);

        category2 = new Category();
        category2.setName("Workshop");
        entityManager.persist(category2);

        entityManager.flush();
    }

    @Test
    void findAll_ShouldReturnAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        assertThat(categories).hasSize(2);
        assertThat(categories).extracting("name").containsExactlyInAnyOrder("Conference", "Workshop");
    }

    @Test
    void existsByNameIgnoreCase_ShouldReturnTrue_WhenCategoryExists() {
        boolean exists = categoryRepository.existsByNameIgnoreCase("conference");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByNameIgnoreCase_ShouldReturnFalse_WhenCategoryDoesNotExist() {
        boolean exists = categoryRepository.existsByNameIgnoreCase("NonExistent");
        assertThat(exists).isFalse();
    }

    @Test
    void save_ShouldPersistCategory() {
        Category newCategory = new Category();
        newCategory.setName("Webinar");

        Category saved = categoryRepository.save(newCategory);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Webinar");

        Category found = entityManager.find(Category.class, saved.getId());
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Webinar");
    }

    @Test
    void deleteById_ShouldRemoveCategory() {
        UUID id = category1.getId();
        categoryRepository.deleteById(id);

        Category deleted = entityManager.find(Category.class, id);
        assertThat(deleted).isNull();

        Category remaining = entityManager.find(Category.class, category2.getId());
        assertThat(remaining).isNotNull();
    }
}