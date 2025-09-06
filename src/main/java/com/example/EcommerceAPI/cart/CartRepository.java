package com.example.EcommerceAPI.cart;

import com.example.EcommerceAPI.cart.entity.Cart;
import com.example.EcommerceAPI.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);

    Optional<Cart> findByUserId(Long userId);
}
