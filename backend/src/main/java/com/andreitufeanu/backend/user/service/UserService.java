package com.andreitufeanu.backend.user.service;

import com.andreitufeanu.backend.exceptions.ConflictException;
import com.andreitufeanu.backend.user.dto.RegisterDto;
import com.andreitufeanu.backend.user.dto.UserResponseDto;
import com.andreitufeanu.backend.user.entity.User;
import com.andreitufeanu.backend.user.enums.UserRole;
import com.andreitufeanu.backend.user.mapper.UserMapper;
import com.andreitufeanu.backend.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto registerUser(@Valid RegisterDto dto) {
        userRepository.findByUsername(dto.username()).ifPresent(user -> {
            throw new ConflictException("Username already exists.");
        });

        User user = userMapper.toEntity(dto);
        user.setRole(UserRole.ROLE_USER);
        user.setPasswordHash(passwordEncoder.encode(dto.password()));

        User createdUser = userRepository.save(user);

        return userMapper.toDto(createdUser);
    }

}