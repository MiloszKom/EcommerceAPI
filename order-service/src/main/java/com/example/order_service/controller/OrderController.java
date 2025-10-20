package com.example.order_service.controller;

import com.example.order_service.dto.OrderDetailsDto;
import com.example.order_service.dto.OrderSummaryDto;
import com.example.order_service.exception.GlobalExceptionHandler;
import com.example.order_service.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final IOrderService orderService;

    public OrderController(IOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDetailsDto> createOrder(
            @RequestHeader("X-User-Id") String userId
    ) {
        log.info("POST /api/orders called by userId={}", userId);

        OrderDetailsDto newOrder = orderService.createOrder(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<OrderDetailsDto> payOrder(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("orderId") Long orderId
    ) {
        log.info("POST /api/orders/{}/pay called by userId={}", orderId, userId);

        OrderDetailsDto order = orderService.payOrder(userId, orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/me")
    public ResponseEntity<List<OrderSummaryDto>> getCurrentUserOrders(
            @RequestHeader("X-User-Id") String userId
    ){
        log.info("GET /api/orders/me called by userId={}", userId);

        List<OrderSummaryDto> orders = orderService.getCurrentUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailsDto> getOrderDetails(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Is-Admin") Boolean isAdmin,
            @PathVariable Long orderId
    ) {
        log.info("GET /api/orders/{} called by userId={}, isAdmin={}", orderId, userId, isAdmin);

        OrderDetailsDto order = orderService.getOrderDetails(userId, isAdmin, orderId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDetailsDto> cancelOrder(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Is-Admin") Boolean isAdmin,
            @PathVariable Long orderId
    ) {
        log.info("POST /api/orders/{}/cancel called by userId={}, isAdmin={}", orderId, userId, isAdmin);

        OrderDetailsDto order = orderService.cancelOrder(userId, isAdmin, orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderSummaryDto>> getAllOrders() {
        log.info("GET /api/orders called to fetch all orders (admin only)");

        List<OrderSummaryDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{orderId}/complete")
    public ResponseEntity<OrderDetailsDto> completeOrderAsAdmin(@PathVariable Long orderId) {
        log.info("POST /api/orders/{}/complete called by admin", orderId);

        OrderDetailsDto order = orderService.completeOrderAsAdmin(orderId);
        return ResponseEntity.ok(order);
    }
}
