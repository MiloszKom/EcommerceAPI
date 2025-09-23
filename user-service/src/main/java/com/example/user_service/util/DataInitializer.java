package com.example.user_service.util;

import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Value("${app.user.username}")
    private String userUsername;

    @Value("${app.user.email}")
    private String userEmail;

    @Value("${app.user.password}")
    private String userPassword;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername(userUsername) && !userRepository.existsByEmail(userEmail)){

                User normalUser = new User();
                normalUser.setUsername(userUsername);
                normalUser.setEmail(userEmail);
                normalUser.setPassword(passwordEncoder.encode(userPassword));
                normalUser.setRole(User.Role.CUSTOMER);

                userRepository.save(normalUser);
            }

            if (!userRepository.existsByUsername(adminUsername) && !userRepository.existsByEmail(adminEmail)) {

                User adminUser = new User();
                adminUser.setUsername(adminUsername);
                adminUser.setEmail(adminEmail);
                adminUser.setPassword(passwordEncoder.encode(adminPassword));
                adminUser.setRole(User.Role.ADMIN);

                userRepository.save(adminUser);
            }
        };
    }
}
