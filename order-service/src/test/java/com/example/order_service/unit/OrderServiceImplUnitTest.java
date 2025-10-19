package com.example.order_service.unit;

import com.example.order_service.dto.*;
import com.example.order_service.dto.client.CartDto;
import com.example.order_service.dto.client.CartItemDto;
import com.example.order_service.dto.client.ProductDto;
import com.example.order_service.dto.client.StockUpdateRequest;
import com.example.order_service.exception.AccessDeniedException;
import com.example.order_service.exception.ConflictException;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderItem;
import com.example.order_service.model.OrderStatus;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.client.CartFeignClient;
import com.example.order_service.service.client.ProductFeignClient;
import com.example.order_service.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceImplUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartFeignClient cartFeignClient;

    @Mock
    private ProductFeignClient productFeignClient;

    @InjectMocks
    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder_ShouldThrowException_WhenCartIsEmpty() {
        when(cartFeignClient.getUserCart("user1")).thenReturn(new CartDto(1L, "user1", List.of()));

        assertThrows(IllegalArgumentException.class, () -> service.createOrder("user1"));
    }

    @Test
    void payOrder_ShouldMarkAsPaid_WhenPendingAndOwned() {
        Order order = createOrder(1L, "user1", OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        OrderDetailsDto result = service.payOrder("user1", 1L);

        assertEquals(OrderStatus.PAID, result.status());
        verify(orderRepository).save(order);
    }

    @Test
    void payOrder_ShouldThrowAccessDenied_WhenDifferentUser() {
        Order order = createOrder(1L, "anotherUser", OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> service.payOrder("user1", 1L));
    }

    @Test
    void payOrder_ShouldThrowConflict_WhenNotPending() {
        Order order = createOrder(1L, "user1", OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(ConflictException.class, () -> service.payOrder("user1", 1L));
    }

    @Test
    void getCurrentUserOrders_ShouldReturnOrders() {
        Order o1 = createOrder(1L, "user1", OrderStatus.PAID);
        Order o2 = createOrder(2L, "user1", OrderStatus.COMPLETED);

        when(orderRepository.findByUserId("user1")).thenReturn(List.of(o1, o2));

        List<OrderSummaryDto> result = service.getCurrentUserOrders("user1");

        assertEquals(2, result.size());
        verify(orderRepository).findByUserId("user1");
    }

    @Test
    void getOrderDetails_ShouldReturn_WhenUserOwnsOrder() {
        Order order = createOrder(1L, "user1", OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderDetailsDto result = service.getOrderDetails("user1", false, 1L);
        assertEquals("user1", result.userId());
    }

    @Test
    void getOrderDetails_ShouldAllowAdminAccess() {
        Order order = createOrder(1L, "anotherUser", OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderDetailsDto result = service.getOrderDetails("admin", true, 1L);
        assertEquals("anotherUser", result.userId());
    }

    @Test
    void getOrderDetails_ShouldThrowAccessDenied_WhenNotOwnerOrAdmin() {
        Order order = createOrder(1L, "other", OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> service.getOrderDetails("user1", false, 1L));
    }

    @Test
    void cancelOrder_ShouldIncreaseStockAndCancel_WhenPending() {
        OrderItem item = createOrderItem(1L, 1L, "Laptop", 2, BigDecimal.valueOf(100));
        Order order = createOrder(1L, "user1", OrderStatus.PENDING, List.of(item));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        OrderDetailsDto result = service.cancelOrder("user1", false, 1L);

        assertEquals(OrderStatus.CANCELLED, result.status());
        verify(productFeignClient).increaseStock(eq(1L), any(StockUpdateRequest.class));
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_ShouldThrowAccessDenied_WhenUserNotOwnerOrAdmin() {
        Order order = createOrder(1L, "other", OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> service.cancelOrder("user1", false, 1L));
    }

    @Test
    void cancelOrder_ShouldThrowIllegalArgument_WhenNotPending() {
        Order order = createOrder(1L, "user1", OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> service.cancelOrder("user1", false, 1L));
    }

    @Test
    void completeOrderAsAdmin_ShouldMarkCompleted_WhenPaid() {
        Order order = createOrder(1L, "user1", OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        OrderDetailsDto result = service.completeOrderAsAdmin(1L);

        assertEquals(OrderStatus.COMPLETED, result.status());
        verify(orderRepository).save(order);
    }

    @Test
    void completeOrderAsAdmin_ShouldThrowIllegalArgument_WhenNotPaid() {
        Order order = createOrder(1L, "user1", OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> service.completeOrderAsAdmin(1L));
    }

    @Test
    void getAllOrders_ShouldReturnList() {
        when(orderRepository.findAll()).thenReturn(List.of(createOrder(1L, "u1", OrderStatus.PAID)));

        List<OrderSummaryDto> result = service.getAllOrders();

        assertEquals(1, result.size());
        verify(orderRepository).findAll();
    }

    // --- Helper methods ---
    private static Order createOrder(Long id, String userId, OrderStatus status) {
        return createOrder(id, userId, status, List.of());
    }

    private static Order createOrder(Long id, String userId, OrderStatus status, List<OrderItem> items) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(status);
        order.setItems(items);
        order.setTotalPrice(BigDecimal.TEN);

        if (id != null) {
            try {
                Field idField = Order.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(order, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return order;
    }

    private static OrderItem createOrderItem(Long id, Long productId, String name, int qty, BigDecimal price) {
        OrderItem item = new OrderItem();
        item.setProductId(productId);
        item.setProductName(name);
        item.setQuantity(qty);
        item.setPriceAtPurchase(price);

        if (id != null) {
            try {
                Field idField = OrderItem.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(item, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return item;
    }
}
