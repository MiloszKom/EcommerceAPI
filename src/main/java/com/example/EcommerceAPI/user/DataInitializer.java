package com.example.EcommerceAPI.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Value("${user.username}")
    private String userUsername;

    @Value("${user.email}")
    private String userEmail;

    @Value("${user.password}")
    private String userPassword;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
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
