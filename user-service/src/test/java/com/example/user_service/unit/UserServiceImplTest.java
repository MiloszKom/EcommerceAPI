package com.example.user_service.unit;

import com.example.user_service.dto.UserDetailsResponseDto;
import com.example.user_service.dto.UserUpdateRequestDto;
import com.example.user_service.entity.User;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl service;

    private String userId;
    private String username;
    private String email;
    private UserUpdateRequestDto updateRequest;

    @BeforeEach
    void setUp() {
        userId = "user-123";
        username = "milosz";
        email = "milosz@example.com";
        updateRequest = new UserUpdateRequestDto();
        updateRequest.setAddress("Example address");
        updateRequest.setPhoneNumber("123456789");
    }

    @Test
    void getCurrentUserDetails_ShouldReturnUserDetails_WhenUserExists() {
        // given
        User user = new User();
        user.setId(userId);
        user.setAddress("Example address");
        user.setPhoneNumber("123456789");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        UserDetailsResponseDto result = service.getCurrentUserDetails(userId, username, email);

        // then
        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        assertEquals("Example address", result.getAddress());
        assertEquals("123456789", result.getPhoneNumber());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getCurrentUserDetails_ShouldCreateAndReturnUserDetails_WhenUserDoesNotExist() {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        User newUser = new User();
        newUser.setId(userId);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // when
        UserDetailsResponseDto result = service.getCurrentUserDetails(userId, username, email);

        // then
        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        assertNull(result.getAddress());
        assertNull(result.getPhoneNumber());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserDetails_ShouldUpdateAndReturnUserDetails_WhenUserExists() {
        // given
        User user = new User();
        user.setId(userId);
        user.setAddress("Old Address");
        user.setPhoneNumber("987654321");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        UserDetailsResponseDto result = service.updateUserDetails(userId, username, email, updateRequest);

        // then
        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        assertEquals(updateRequest.getAddress(), result.getAddress());
        assertEquals(updateRequest.getPhoneNumber(), result.getPhoneNumber());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserDetails_ShouldCreateAndUpdateUserDetails_WhenUserDoesNotExist() {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        User newUser = new User();
        newUser.setId(userId);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // when
        UserDetailsResponseDto result = service.updateUserDetails(userId, username, email, updateRequest);

        // then
        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        assertEquals(updateRequest.getAddress(), result.getAddress());
        assertEquals(updateRequest.getPhoneNumber(), result.getPhoneNumber());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(userRepository).findById(userId);
        verify(userRepository, times(2)).save(any(User.class));
    }
}
