package com.example.user_service.service;

import com.example.user_service.config.SecurityUtils;
import com.example.user_service.dto.UserDetailsDTO;
import com.example.user_service.dto.UserSummaryDTO;
import com.example.user_service.exception.types.UsernameNotFoundException;
import com.example.user_service.mapper.UserMapper;
import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HttpServletRequest request;

    private User returnCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId(request);

        return userRepository.findById(userId)
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
