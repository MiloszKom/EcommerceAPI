package com.example.user_service.controller;

import com.example.user_service.dto.*;
import com.example.user_service.service.IUserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody RegisterRequestDto request) {
        log.debug("Starting registration for email: {}", request.getEmail());;
        TokenResponse token = userService.register(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("User login attempt for email: {}", request.getEmail());;
        TokenResponse token = userService.login(request);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDetailsResponseDto> getCurrentUserDetails(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Username") String username,
            @RequestHeader("X-User-Email") String email
    ) {
        log.debug("Fetching current user details for userId: {}", userId);
        UserDetailsResponseDto details = userService.getCurrentUserDetails(userId, username, email);
        return ResponseEntity.ok(details);
    }
}
