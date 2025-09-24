package com.example.order_service.controller;

import com.example.order_service.client.ProductClient;
import com.example.order_service.config.SecurityUtils;
import com.example.order_service.dto.OrderDetailsDTO;
import com.example.order_service.dto.OrderSummaryDTO;
import com.example.order_service.model.OrderStatus;
import com.example.order_service.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController (OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrderDetailsDTO> createOrder(HttpServletRequest request) {
        Long userId = SecurityUtils.getCurrentUserId(request);
        OrderDetailsDTO newOrder = service.createOrder(userId);
        URI location = URI.create("/api/orders/" + newOrder.id());
        return ResponseEntity.created(location).body(newOrder);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<OrderDetailsDTO> payOrder(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId(request);
        OrderDetailsDTO order = service.payOrder(userId, id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/me")
    public ResponseEntity<List<OrderSummaryDTO>> getUserOrders(HttpServletRequest request) {
        Long userId = SecurityUtils.getCurrentUserId(request);
        List<OrderSummaryDTO> orders = service.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailsDTO> getOrderDetails(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId(request);
        String userRole = SecurityUtils.getCurrentUserRole(request);
        OrderDetailsDTO order = service.getOrderDetails(userId, userRole, id);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderDetailsDTO> cancelOrder(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId(request);
        String userRole = SecurityUtils.getCurrentUserRole(request);
        OrderDetailsDTO order = service.cancelOrder(userId, userRole, id);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderSummaryDTO>> getAllOrders(
            @RequestParam(required = false) String status
    ) {
        OrderStatus orderStatus = null;

        if (status != null) {
            try {
                orderStatus = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                orderStatus = null;
            }
        }

        List<OrderSummaryDTO> orders = service.getAllOrders(orderStatus);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<OrderDetailsDTO> completeOrderAsAdmin(@PathVariable Long id) {
        OrderDetailsDTO order = service.completeOrderAsAdmin(id);
        return ResponseEntity.ok(order);
    }
}
