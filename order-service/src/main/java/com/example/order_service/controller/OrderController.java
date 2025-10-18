package com.example.order_service.controller;

import com.example.order_service.dto.OrderDetailsDto;
import com.example.order_service.dto.OrderSummaryDto;
import com.example.order_service.service.IOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final IOrderService orderService;

    public OrderController(IOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDetailsDto> createOrder(
            @RequestHeader("X-User-Id") String userId
    ) {
        OrderDetailsDto newOrder = orderService.createOrder(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<OrderDetailsDto> payOrder(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("orderId") Long orderId) {
        OrderDetailsDto order = orderService.payOrder(userId, orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/me")
    public ResponseEntity<List<OrderSummaryDto>> getCurrentUserOrders(
            @RequestHeader("X-User-Id") String userId
    ){
        List<OrderSummaryDto> orders = orderService.getCurrentUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailsDto> getOrderDetails(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Is-Admin") Boolean isAdmin,
            @PathVariable Long orderId) {
        OrderDetailsDto order = orderService.getOrderDetails(userId, isAdmin, orderId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDetailsDto> cancelOrder(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Is-Admin") Boolean isAdmin,
            @PathVariable Long orderId) {
        OrderDetailsDto order = orderService.cancelOrder(userId, isAdmin, orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderSummaryDto>> getAllOrders() {
        List<OrderSummaryDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{orderId}/complete")
    public ResponseEntity<OrderDetailsDto> completeOrderAsAdmin(@PathVariable Long orderId) {
        OrderDetailsDto order = orderService.completeOrderAsAdmin(orderId);
        return ResponseEntity.ok(order);
    }
}
