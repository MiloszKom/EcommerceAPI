package com.example.user_service.service;

import com.example.user_service.dto.UserDetailsDTO;
import com.example.user_service.dto.UserSummaryDTO;
import com.example.user_service.exception.types.UserNotFoundException;
import com.example.user_service.mapper.UserMapper;
import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    private User getUser(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public UserDetailsDTO getCurrentUser(Long userId) {
        User user = getUser(userId);
        return UserMapper.toDetailsDTO(user);
    }

    public List<UserSummaryDTO> findAll() {
        List<User> users = repository.findAll();

        return users.stream()
                .map(UserMapper::toSummaryDTO)
                .toList();
    }
}
