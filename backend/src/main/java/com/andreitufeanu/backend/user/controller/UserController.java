package com.andreitufeanu.backend.user.controller;

import com.andreitufeanu.backend.user.dto.RegisterDto;
import com.andreitufeanu.backend.user.dto.UserResponseDto;
import com.andreitufeanu.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@RequestBody RegisterDto user) {
        return ResponseEntity.ok().body(userService.registerUser(user));
    }

}
