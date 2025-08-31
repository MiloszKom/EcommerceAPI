package com.example.EcommerceAPI.user;

import com.example.EcommerceAPI.user.dto.UserDetailsDTO;
import com.example.EcommerceAPI.user.dto.UserSummaryDTO;

public class UserMapper {

    public static UserSummaryDTO toSummaryDTO(User user) {
        return new UserSummaryDTO(user.getId(), user.getUsername());
    }

    public static UserDetailsDTO toDetailsDTO(User user) {
        return new UserDetailsDTO(user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt());
    }

}
