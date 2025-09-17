package com.example.order_service.repository;

import com.example.order_service.model.Order;
import com.example.order_service.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(OrderStatus status);
}
