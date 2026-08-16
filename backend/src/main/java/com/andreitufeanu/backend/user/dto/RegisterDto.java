package com.andreitufeanu.backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDto(

        @NotBlank(message = "Username is required")
        @Size(max = 50, message = "Username cannot exceed {max} characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 500, message = "Password must be between {min} and {max} characters")
        String password)
{ }
