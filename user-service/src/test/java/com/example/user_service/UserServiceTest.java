package com.example.user_service;

import com.example.user_service.dto.UserDetailsDTO;
import com.example.user_service.dto.UserSummaryDTO;
import com.example.user_service.exception.types.UserNotFoundException;
import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDetailsDTO userDetailsDTO;
    private UserSummaryDTO userSummaryDTO;
    private LocalDateTime createdAt;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(User.Role.CUSTOMER);
        createdAt = LocalDateTime.now();
        testUser.setCreatedAt(createdAt);

        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(testUser, 1L);

        userDetailsDTO = new UserDetailsDTO(1L, "testuser", "test@example.com", createdAt);
        userSummaryDTO = new UserSummaryDTO(1L, "testuser");
    }

    @Test
    void getCurrentUser_WhenUserExists_ReturnsUserDetailsDTO() {
        when(repository.findById(1L)).thenReturn(Optional.of(testUser));

        UserDetailsDTO result = userService.getCurrentUser(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("testuser", result.username());
        assertEquals("test@example.com", result.email());
        assertEquals(createdAt, result.createdAt());
        verify(repository).findById(1L);
    }

    @Test
    void getCurrentUser_WhenUserDoesNotExist_ThrowsUsernameNotFoundException() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getCurrentUser(1L)
        );

        assertEquals("User not found", exception.getMessage());
        verify(repository).findById(1L);
    }

    @Test
    void findAll_WhenUsersExist_ReturnsListOfUserSummaryDTO() {
        List<User> users = List.of(testUser);
        when(repository.findAll()).thenReturn(users);

        List<UserSummaryDTO> result = userService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("testuser", result.get(0).username());
        verify(repository).findAll();
    }

    @Test
    void findAll_WhenNoUsersExist_ReturnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<UserSummaryDTO> result = userService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findAll();
    }
}
