package com.example.order_service.unit;

import com.example.order_service.dto.*;
import com.example.order_service.dto.client.CartDto;
import com.example.order_service.dto.client.CartItemDto;
import com.example.order_service.dto.client.ProductDto;
import com.example.order_service.dto.client.StockUpdateRequest;
import com.example.order_service.exception.AccessDeniedException;
import com.example.order_service.exception.ConflictException;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderItem;
import com.example.order_service.model.OrderStatus;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.client.CartFeignClient;
import com.example.order_service.service.client.ProductFeignClient;
import com.example.order_service.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartFeignClient cartFeignClient;

    @Mock
    private ProductFeignClient productFeignClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    // Helper methods for reflection
    private void setOrderId(Order order, Long id) {
        try {
            Field idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set Order ID via reflection", e);
        }
    }

    private void setOrderItemId(OrderItem orderItem, Long id) {
        try {
            Field idField = OrderItem.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(orderItem, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set OrderItem ID via reflection", e);
        }
    }

    // Test data creation methods
    private CartDto createTestCartDto(String userId) {
        List<CartItemDto> cartItems = new ArrayList<>();
        cartItems.add(new CartItemDto(1L, 101L, 2));
        cartItems.add(new CartItemDto(2L, 102L, 1));
        return new CartDto(1L, userId, cartItems);
    }

    private ProductDto createTestProductDto(Long productId) {
        return new ProductDto(
                productId,
                "Test Product " + productId,
                "Test Description",
                BigDecimal.valueOf(19.99),
                10
        );
    }

    private Order createTestOrder(String userId, OrderStatus status) {
        Order order = new Order();
        setOrderId(order, 1L);
        order.setUserId(userId);
        order.setStatus(status);
        order.setTotalPrice(BigDecimal.valueOf(59.97));

        List<OrderItem> items = new ArrayList<>();
        OrderItem item1 = new OrderItem(101L, "Product 1", 2, BigDecimal.valueOf(19.99));
        OrderItem item2 = new OrderItem(102L, "Product 2", 1, BigDecimal.valueOf(19.99));
        setOrderItemId(item1, 1L);
        setOrderItemId(item2, 2L);
        item1.setOrder(order);
        item2.setOrder(order);
        items.add(item1);
        items.add(item2);

        order.setItems(items);
        return order;
    }

    @Test
    void createOrder_WithValidCart_ShouldCreateOrder() {
        // Arrange
        String userId = "user123";
        CartDto cartDto = createTestCartDto(userId);
        ProductDto product1 = createTestProductDto(101L);
        ProductDto product2 = createTestProductDto(102L);

        when(cartFeignClient.getUserCart(userId)).thenReturn(cartDto);
        when(productFeignClient.getProductById(101L)).thenReturn(product1);
        when(productFeignClient.getProductById(102L)).thenReturn(product2);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            setOrderId(order, 1L);

            if (order.getItems() != null) {
                long itemId = 1L;
                for (OrderItem item : order.getItems()) {
                    setOrderItemId(item, itemId++);
                }
            }
            return order;
        });

        // Act
        OrderDetailsDto result = orderService.createOrder(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.userId());
        assertEquals(2, result.items().size());
        // Verify that the items have non-null IDs
        assertTrue(result.items().stream().allMatch(item -> item.id() > 0));
        verify(cartFeignClient, times(1)).getUserCart(userId);
        verify(productFeignClient, times(2)).getProductById(anyLong());
        verify(productFeignClient, times(2)).reduceStock(anyLong(), any(StockUpdateRequest.class));
        verify(cartFeignClient, times(1)).clearCart(userId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_WithEmptyCart_ShouldThrowException() {
        // Arrange
        String userId = "user123";
        CartDto emptyCart = new CartDto(1L, userId, new ArrayList<>());

        when(cartFeignClient.getUserCart(userId)).thenReturn(emptyCart);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(userId));

        verify(cartFeignClient, times(1)).getUserCart(userId);
        verify(productFeignClient, never()).getProductById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void payOrder_WithValidOrder_ShouldUpdateStatus() {
        // Arrange
        String userId = "user123";
        Long orderId = 1L;
        Order order = createTestOrder(userId, OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderDetailsDto result = orderService.payOrder(userId, orderId);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.PAID, order.getStatus());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void payOrder_WithWrongUser_ShouldThrowAccessDeniedException() {
        // Arrange
        String userId = "user123";
        String wrongUserId = "user456";
        Long orderId = 1L;
        Order order = createTestOrder(userId, OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> orderService.payOrder(wrongUserId, orderId));

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void payOrder_WithNonPendingStatus_ShouldThrowConflictException() {
        // Arrange
        String userId = "user123";
        Long orderId = 1L;
        Order order = createTestOrder(userId, OrderStatus.PAID);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(ConflictException.class,
                () -> orderService.payOrder(userId, orderId));

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getCurrentUserOrders_ShouldReturnUserOrders() {
        // Arrange
        String userId = "user123";
        List<Order> orders = new ArrayList<>();
        orders.add(createTestOrder(userId, OrderStatus.PENDING));
        orders.add(createTestOrder(userId, OrderStatus.PAID));

        when(orderRepository.findByUserId(userId)).thenReturn(orders);

        // Act
        List<OrderSummaryDto> result = orderService.getCurrentUserOrders(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository, times(1)).findByUserId(userId);
    }

    @Test
    void getOrderDetails_WithOwnerAccess_ShouldReturnOrder() {
        // Arrange
        String userId = "user123";
        Long orderId = 1L;
        Order order = createTestOrder(userId, OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        OrderDetailsDto result = orderService.getOrderDetails(userId, false, orderId);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.id());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getOrderDetails_WithAdminAccess_ShouldReturnOrder() {
        // Arrange
        String userId = "user123";
        String adminUserId = "admin";
        Long orderId = 1L;
        Order order = createTestOrder(userId, OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        OrderDetailsDto result = orderService.getOrderDetails(adminUserId, true, orderId);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.id());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getOrderDetails_WithUnauthorizedAccess_ShouldThrowAccessDeniedException() {
        // Arrange
        String userId = "user123";
        String wrongUserId = "user456";
        Long orderId = 1L;
        Order order = createTestOrder(userId, OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> orderService.getOrderDetails(wrongUserId, false, orderId));

        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void cancelOrder_WithValidPendingOrder_ShouldCancelAndRestock() {
        // Arrange
        String userId = "user123";
        Long orderId = 1L;
        Order order = createTestOrder(userId, OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderDetailsDto result = orderService.cancelOrder(userId, false, orderId);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository, times(1)).findById(orderId);
        verify(productFeignClient, times(2)).increaseStock(anyLong(), any(StockUpdateRequest.class));
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void cancelOrder_WithAdminAccess_ShouldCancelOrder() {
        // Arrange
        String userId = "user123";
        String adminUserId = "admin";
        Long orderId = 1L;
        Order order = createTestOrder(userId, OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderDetailsDto result = orderService.cancelOrder(adminUserId, true, orderId);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void cancelOrder_WithNonPendingStatus_ShouldThrowException() {
        // Arrange
        String userId = "user123";
        Long orderId = 1L;
        Order order = createTestOrder(userId, OrderStatus.PAID);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(ConflictException.class,
                () -> orderService.cancelOrder(userId, false, orderId));

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        // Arrange
        List<Order> orders = new ArrayList<>();
        orders.add(createTestOrder("user1", OrderStatus.PENDING));
        orders.add(createTestOrder("user2", OrderStatus.PAID));

        when(orderRepository.findAll()).thenReturn(orders);

        // Act
        List<OrderSummaryDto> result = orderService.getAllOrders();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void completeOrderAsAdmin_WithPaidOrder_ShouldCompleteOrder() {
        // Arrange
        Long orderId = 1L;
        Order order = createTestOrder("user123", OrderStatus.PAID);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderDetailsDto result = orderService.completeOrderAsAdmin(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void completeOrderAsAdmin_WithNonPaidOrder_ShouldThrowException() {
        // Arrange
        Long orderId = 1L;
        Order order = createTestOrder("user123", OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(ConflictException.class,
                () -> orderService.completeOrderAsAdmin(orderId));

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrder_WithNonExistentOrder_ShouldThrowNotFoundException() {
        // Arrange
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> orderService.getOrderDetails("user123", false, orderId));

        verify(orderRepository, times(1)).findById(orderId);
    }
}
