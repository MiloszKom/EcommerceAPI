package com.example.EcommerceAPI.user;

import com.example.EcommerceAPI.user.dto.UserDetailsDTO;
import com.example.EcommerceAPI.user.dto.UserSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService service;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<UserSummaryDTO>> getUsers() {
        List<UserSummaryDTO> users = service.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDetailsDTO> getCurrentUser() {
        UserDetailsDTO user = service.getCurrentUser();
        return ResponseEntity.ok(user);
    }
}
