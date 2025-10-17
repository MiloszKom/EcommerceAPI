package com.example.user_service.controller;

import com.example.user_service.dto.*;
import com.example.user_service.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody RegisterRequestDto request) {
        TokenResponse token = userService.register(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequestDto request) {
        TokenResponse token = userService.login(request);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDetailsResponseDto> getCurrentUserDetails(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Username") String username,
            @RequestHeader("X-User-Email") String email
    ) {
        UserDetailsResponseDto details = userService.getCurrentUserDetails(userId, username, email);
        return ResponseEntity.ok(details);
    }
}
