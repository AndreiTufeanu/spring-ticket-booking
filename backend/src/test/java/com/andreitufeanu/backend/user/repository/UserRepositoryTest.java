package com.andreitufeanu.backend.user.repository;

import com.andreitufeanu.backend.user.entity.User;
import com.andreitufeanu.backend.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPasswordHash("encoded123");
        testUser.setRole(UserRole.ROLE_USER);
        testUser.setRefreshToken("refresh123");
        testUser.setRefreshTokenExpiry(Instant.now().plusSeconds(3600));
        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenExists() {
        Optional<User> found = userRepository.findByUsername("testuser");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenNotExists() {
        Optional<User> found = userRepository.findByUsername("unknown");
        assertThat(found).isEmpty();
    }

    @Test
    void findByRefreshToken_ShouldReturnUser_WhenExists() {
        Optional<User> found = userRepository.findByRefreshToken("refresh123");
        assertThat(found).isPresent();
        assertThat(found.get().getRefreshToken()).isEqualTo("refresh123");
    }

    @Test
    void findByRefreshToken_ShouldReturnEmpty_WhenNotExists() {
        Optional<User> found = userRepository.findByRefreshToken("invalid");
        assertThat(found).isEmpty();
    }
}

