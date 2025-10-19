package com.example.order_service.service.impl;

import com.example.order_service.dto.*;
import com.example.order_service.dto.client.CartDto;
import com.example.order_service.dto.client.ProductDto;
import com.example.order_service.dto.client.StockUpdateRequest;
import com.example.order_service.exception.AccessDeniedException;
import com.example.order_service.exception.ConflictException;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderItem;
import com.example.order_service.model.OrderStatus;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.IOrderService;
import com.example.order_service.service.client.CartFeignClient;
import com.example.order_service.service.client.ProductFeignClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final CartFeignClient cartFeignClient;
    private final ProductFeignClient productFeignClient;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            CartFeignClient cartFeignClient,
            ProductFeignClient productFeignClient
    ) {
        this.orderRepository = orderRepository;
        this.cartFeignClient = cartFeignClient;
        this.productFeignClient = productFeignClient;
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Cannot find order with id: " + orderId));
    }

    @Transactional
    public OrderDetailsDto createOrder(String userId) {
        CartDto cart = cartFeignClient.getUserCart(userId);

        if (cart == null || cart.items().isEmpty()) {
            throw new IllegalArgumentException("Cannot create order: cart is empty.");
        }

        Order order = new Order();
        order.setUserId(userId);

        List<OrderItem> orderItems = cart.items().stream().map(cartItem -> {
            ProductDto product = productFeignClient.getProductById(cartItem.productId());

            productFeignClient.reduceStock(
                    cartItem.productId(),
                    new StockUpdateRequest(cartItem.quantity())
            );

            OrderItem item = new OrderItem();
            item.setProductId(product.id());
            item.setProductName(product.name());
            item.setQuantity(cartItem.quantity());
            item.setPriceAtPurchase(product.price());
            item.setOrder(order);
            return item;
        }).toList();

        BigDecimal totalPrice = orderItems.stream()
                .map(i -> i.getPriceAtPurchase().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(totalPrice);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        cartFeignClient.clearCart(userId);

        return OrderMapper.toDetailsDTO(savedOrder);
    }

    @Override
    public OrderDetailsDto payOrder(String userId, Long orderId) {
        Order order = getOrder(orderId);

        if (!order.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to pay for this order.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ConflictException("Order cannot be paid. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.PAID);
        Order completedOrder = orderRepository.save(order);

        return OrderMapper.toDetailsDTO(completedOrder);
    }

    @Override
    public List<OrderSummaryDto> getCurrentUserOrders(String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(OrderMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    public OrderDetailsDto getOrderDetails(String userId, Boolean isAdmin, Long orderId) {
        Order order = getOrder(orderId);

        if (!order.getUserId().equals(userId) && !Boolean.TRUE.equals(isAdmin)) {
            throw new AccessDeniedException("You cannot access this order.");
        }

        return OrderMapper.toDetailsDTO(order);
    }

    @Override
    public OrderDetailsDto cancelOrder(String userId, Boolean isAdmin, Long orderId) {
        Order order = getOrder(orderId);

        if (!order.getUserId().equals(userId) && !Boolean.TRUE.equals(isAdmin)) {
            throw new AccessDeniedException("You do not have permission to cancel this order.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Order cannot be cancelled. Current status: " + order.getStatus()
            );
        }

        for (OrderItem item : order.getItems()) {
            productFeignClient.increaseStock(
                    item.getProductId(),
                    new StockUpdateRequest(item.getQuantity())
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);

        return OrderMapper.toDetailsDTO(cancelledOrder);
    }

    @Override
    public List<OrderSummaryDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();

        return orders.stream()
                .map(OrderMapper::toSummaryDTO)
                .toList();
    }

    @Override
    public OrderDetailsDto completeOrderAsAdmin(Long orderId) {
        Order order = getOrder(orderId);

        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalArgumentException(
                    "Only paid orders can be marked as completed. Current status: " + order.getStatus()
            );
        }

        order.setStatus(OrderStatus.COMPLETED);
        Order updatedOrder = orderRepository.save(order);

        return OrderMapper.toDetailsDTO(updatedOrder);
    }
}
