package com.example.user_service.service;

import com.example.user_service.dto.UserDetailsDTO;
import com.example.user_service.dto.UserSummaryDTO;
import com.example.user_service.exception.types.UsernameNotFoundException;
import com.example.user_service.mapper.UserMapper;
import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private User returnCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public UserDetailsDTO getCurrentUser() {
        User user = returnCurrentUser();
        return UserMapper.toDetailsDTO(user);
    }

    public List<UserSummaryDTO> findAll() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(UserMapper::toSummaryDTO)
                .toList();
    }
}
