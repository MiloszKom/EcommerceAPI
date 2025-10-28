package com.example.user_service.service.impl;

import com.example.user_service.controller.UserController;
import com.example.user_service.dto.*;
import com.example.user_service.entity.User;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.IUserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.user_service.mapper.UserMapper.mapToUserDetailsResponseDto;

@Service
@Transactional
public class UserServiceImpl implements IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;

    public UserServiceImpl(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetailsResponseDto getCurrentUserDetails(String userId, String username, String email) {
        log.debug("Fetching user details for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseGet(() -> createNewUser(userId));

        log.info("Returning details for userId: {}", userId);
        return mapToUserDetailsResponseDto(user, username, email);
    }

    @Override
    @Transactional
    public UserDetailsResponseDto updateUserDetails(String userId, String username, String email, UserUpdateRequestDto updateDto) {
        log.debug("Updating user details for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseGet(() -> createNewUser(userId));

        updateUserFields(user, updateDto);
        userRepository.save(user);

        log.info("Updated details for userId: {}", userId);
        return mapToUserDetailsResponseDto(user, username, email);
    }

    private User createNewUser(String userId) {
        log.info("Creating new user with ID: {}", userId);
        User newUser = new User();
        newUser.setId(userId);

        return userRepository.save(newUser);
    }

    private void updateUserFields(User user, UserUpdateRequestDto updateDto) {
        if (updateDto.getAddress() != null) {
            user.setAddress(updateDto.getAddress());
        }
        if (updateDto.getPhoneNumber() != null) {
            user.setPhoneNumber(updateDto.getPhoneNumber());
        }
    }
}