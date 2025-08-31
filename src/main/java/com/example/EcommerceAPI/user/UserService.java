package com.example.EcommerceAPI.user;

import com.example.EcommerceAPI.auth.AuthService;
import com.example.EcommerceAPI.user.dto.UserDetailsDTO;
import com.example.EcommerceAPI.user.dto.UserSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    public UserDetailsDTO getCurrentUser() {
        User user = authService.getCurrentUser();
        return UserMapper.toDetailsDTO(user);
    }

    public List<UserSummaryDTO> findAll() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(UserMapper::toSummaryDTO)
                .toList();
    }

}
