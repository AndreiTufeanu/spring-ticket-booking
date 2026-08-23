package com.andreitufeanu.backend.user.mapper;

import com.andreitufeanu.backend.user.dto.RegisterDto;
import com.andreitufeanu.backend.user.dto.UserResponseDto;
import com.andreitufeanu.backend.user.entity.User;
import com.andreitufeanu.backend.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toEntity_ShouldMapRegisterDtoToUser_IgnoringIdPasswordAndRole() {
        RegisterDto dto = new RegisterDto("john", "password123");

        User user = userMapper.toEntity(dto);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isNull();
        assertThat(user.getUsername()).isEqualTo("john");
        assertThat(user.getPasswordHash()).isNull();
        assertThat(user.getRole()).isEqualTo(UserRole.ROLE_USER);
    }

    @Test
    void toDto_ShouldMapUserToUserResponseDto() {
        User user = new User();
        UUID id = UUID.randomUUID();
        user.setId(id);
        user.setUsername("jane");
        user.setRole(UserRole.ROLE_USER);

        UserResponseDto dto = userMapper.toDto(user);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.username()).isEqualTo("jane");
        assertThat(dto.role()).isEqualTo(UserRole.ROLE_USER);
    }
}
