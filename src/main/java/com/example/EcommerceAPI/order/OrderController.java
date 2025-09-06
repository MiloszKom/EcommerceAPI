package com.example.EcommerceAPI.order;

import com.example.EcommerceAPI.order.dto.OrderDetailsDTO;
import com.example.EcommerceAPI.order.dto.OrderItemDTO;
import com.example.EcommerceAPI.order.dto.OrderSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    OrderService service;

    @PostMapping("/")
    public ResponseEntity<OrderDetailsDTO> createOrder() {
        OrderDetailsDTO newOrder = service.createOrder();
        URI location = URI.create("/api/orders/" + newOrder.id());
        return ResponseEntity.created(location).body(newOrder);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<OrderDetailsDTO> payOrder(@PathVariable Long id) {
        OrderDetailsDTO order = service.payOrder(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderSummaryDTO>> getUserOrders() {
        List<OrderSummaryDTO> orders = service.getUserOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailsDTO> getOrderDetails(@PathVariable Long id) {
        OrderDetailsDTO order = service.getOrderDetails(id);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderDetailsDTO> cancelOrder(@PathVariable Long id) {
        OrderDetailsDTO order = service.cancelOrder(id);
        return ResponseEntity.ok(order);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<OrderSummaryDTO>> getAllOrders() {
        List<OrderSummaryDTO> orders = service.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/complete")
    public ResponseEntity<OrderDetailsDTO> completeOrderAsAdmin(@PathVariable Long id) {
        OrderDetailsDTO order = service.completeOrderAsAdmin(id);
        return ResponseEntity.ok(order);
    }
}
