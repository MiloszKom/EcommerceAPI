package com.example.EcommerceAPI.order;

import com.example.EcommerceAPI.order.entity.Order;
import com.example.EcommerceAPI.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o " +
            "WHERE (:status IS NULL OR o.status = :status)")
    List<Order> findOrders(@Param("status") OrderStatus status);
}
