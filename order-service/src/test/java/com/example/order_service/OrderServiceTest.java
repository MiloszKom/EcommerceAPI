package com.example.order_service;

import com.example.order_service.dto.*;
import com.example.order_service.exception.types.*;
import com.example.order_service.model.*;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.CartClientService;
import com.example.order_service.service.OrderService;
import com.example.order_service.service.ProductClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartClientService cartClientService;

    @Mock
    private ProductClientService productClientService;

    private OrderService underTest;

    @BeforeEach
    void setUp() {
        underTest = new OrderService(orderRepository, cartClientService, productClientService);
    }

    @Test
    void createOrder_whenCartIsEmpty_throwsException() {
        when(cartClientService.getUserCart()).thenReturn(new CartDetailsDTO(1L, List.of(), 0));

        assertThatThrownBy(() -> underTest.createOrder(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cart is empty");
    }

    @Test
    void createOrder_withItems_createsOrderAndClearsCart() {
        CartItemDTO cartItem = new CartItemDTO(10L, 2);
        CartDetailsDTO cartDTO = new CartDetailsDTO(1L, List.of(cartItem), 1);

        when(cartClientService.getUserCart()).thenReturn(cartDTO);

        ProductDTO product = new ProductDTO(10L, "Yamaha Guitar","Description","guitars", BigDecimal.valueOf(500), 10);
        when(productClientService.getProductById(10L)).thenReturn(product);

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order order = inv.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 100L);
            // Set IDs for order items
            order.getItems().forEach(item -> ReflectionTestUtils.setField(item, "id", 50L));
            return order;
        });

        OrderDetailsDTO result = underTest.createOrder(1L);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.items()).hasSize(1);
        assertThat(result.totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(1000));

        verify(productClientService).reduceProductStock(eq(10L), any(StockUpdateRequest.class));
        verify(cartClientService).clearCart();
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void payOrder_whenUserIsNotOwner_throwsUnauthorizedException() {
        Order order = new Order();
        order.setUserId(1L);
        ReflectionTestUtils.setField(order, "id", 100L);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> underTest.payOrder(2L, 100L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void payOrder_whenStatusNotPending_throwsConflictException() {
        Order order = new Order();
        order.setUserId(1L);
        order.setStatus(OrderStatus.PAID);
        ReflectionTestUtils.setField(order, "id", 100L);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> underTest.payOrder(1L, 100L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void payOrder_whenValid_changesStatusToPaid() {
        Order order = new Order();
        order.setUserId(1L);
        order.setStatus(OrderStatus.PENDING);
        ReflectionTestUtils.setField(order, "id", 100L);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderDetailsDTO result = underTest.payOrder(1L, 100L);

        assertThat(result.status()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_whenNotOwnerAndNotAdmin_throwsAccessDenied() {
        Order order = new Order();
        order.setUserId(1L);
        ReflectionTestUtils.setField(order, "id", 100L);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> underTest.cancelOrder(2L, "ROLE_USER", 100L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void cancelOrder_whenStatusNotPending_throwsIllegalArgumentException() {
        Order order = new Order();
        order.setUserId(1L);
        order.setStatus(OrderStatus.PAID);
        ReflectionTestUtils.setField(order, "id", 100L);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> underTest.cancelOrder(1L, "ROLE_USER", 100L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cancelOrder_whenValid_increasesStockAndCancels() {
        Order order = new Order();
        order.setUserId(1L);
        OrderItem item = new OrderItem();
        item.setProductId(10L);
        item.setQuantity(2);
        item.setOrder(order);
        ReflectionTestUtils.setField(item, "id", 50L); // <- important
        order.setItems(List.of(item));
        ReflectionTestUtils.setField(order, "id", 100L);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderDetailsDTO result = underTest.cancelOrder(1L, "ROLE_USER", 100L);

        assertThat(result.status()).isEqualTo(OrderStatus.CANCELLED);
        verify(productClientService).increaseProductStock(eq(10L), any(StockUpdateRequest.class));
        verify(orderRepository).save(order);
    }

    @Test
    void completeOrderAsAdmin_whenStatusPaid_setsCompleted() {
        Order order = new Order();
        order.setStatus(OrderStatus.PAID);
        order.setUserId(1L); // <- important
        ReflectionTestUtils.setField(order, "id", 100L);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderDetailsDTO result = underTest.completeOrderAsAdmin(100L);

        assertThat(result.status()).isEqualTo(OrderStatus.COMPLETED);
    }
}
