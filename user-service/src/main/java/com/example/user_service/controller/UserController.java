package com.example.user_service.controller;

import com.example.user_service.config.SecurityUtils;
import com.example.user_service.dto.LoginRequest;
import com.example.user_service.dto.RegisterRequest;
import com.example.user_service.dto.UserDetailsDTO;
import com.example.user_service.dto.UserSummaryDTO;
import com.example.user_service.service.AuthService;
import com.example.user_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController (UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryDTO>> getUsers() {
        List<UserSummaryDTO> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserDetailsDTO> getCurrentUser(HttpServletRequest request) {
        Long userId = SecurityUtils.getCurrentUserId(request);
        UserDetailsDTO user = userService.getCurrentUser(userId);
        return ResponseEntity.ok(user);
    }
}
