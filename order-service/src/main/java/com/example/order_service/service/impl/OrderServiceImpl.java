package com.example.order_service.service.impl;

import com.example.order_service.controller.OrderController;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements IOrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

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
        log.debug("Fetching order with id={}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found: id={}", orderId);
                    return new NotFoundException("Cannot find order with id: " + orderId);
                });
    }

    @Transactional
    public OrderDetailsDto createOrder(String userId) {
        log.info("Creating order for userId={}", userId);

        CartDto cart = cartFeignClient.getUserCart(userId);
        if (cart == null || cart.items().isEmpty()) {
            log.warn("Cannot create order: cart is empty for userId={}", userId);
            throw new IllegalArgumentException("Cannot create order: cart is empty.");
        }

        Order order = new Order();
        order.setUserId(userId);

        List<OrderItem> orderItems = cart.items().stream().map(cartItem -> {
            log.debug("Fetching product info for productId={}", cartItem.productId());
            ProductDto product = productFeignClient.getProductById(cartItem.productId());

            log.debug("Reducing stock for productId={} by quantity={}", cartItem.productId(), cartItem.quantity());
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
        log.info("Order created successfully for userId={}, orderId={}", userId, savedOrder.getId());

        cartFeignClient.clearCart(userId);
        log.debug("Cart cleared for userId={}", userId);

        return OrderMapper.toDetailsDTO(savedOrder);
    }

    @Override
    public OrderDetailsDto payOrder(String userId, Long orderId) {
        log.info("Paying orderId={} for userId={}", orderId, userId);
        Order order = getOrder(orderId);

        if (!order.getUserId().equals(userId)) {
            log.warn("Access denied: userId={} tried to pay orderId={}", userId, orderId);
            throw new AccessDeniedException("You do not have permission to pay for this order.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Cannot pay orderId={} with status={}", orderId, order.getStatus());
            throw new ConflictException("Order cannot be paid. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.PAID);
        Order completedOrder = orderRepository.save(order);
        log.info("Order paid successfully: orderId={}, userId={}", orderId, userId);

        return OrderMapper.toDetailsDTO(completedOrder);
    }

    @Override
    public List<OrderSummaryDto> getCurrentUserOrders(String userId) {
        log.info("Fetching orders for userId={}", userId);
        List<Order> orders = orderRepository.findByUserId(userId);

        log.debug("Fetched {} orders for userId={}", orders.size(), userId);
        return orders.stream()
                .map(OrderMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    public OrderDetailsDto getOrderDetails(String userId, Boolean isAdmin, Long orderId) {
        log.info("Fetching order details for orderId={}, userId={}, isAdmin={}", orderId, userId, isAdmin);
        Order order = getOrder(orderId);

        if (!order.getUserId().equals(userId) && !Boolean.TRUE.equals(isAdmin)) {
            log.warn("Access denied: userId={} tried to access orderId={}", userId, orderId);
            throw new AccessDeniedException("You cannot access this order.");
        }

        return OrderMapper.toDetailsDTO(order);
    }

    @Override
    public OrderDetailsDto cancelOrder(String userId, Boolean isAdmin, Long orderId) {
        log.info("Cancelling orderId={} requested by userId={}, isAdmin={}", orderId, userId, isAdmin);
        Order order = getOrder(orderId);

        if (!order.getUserId().equals(userId) && !Boolean.TRUE.equals(isAdmin)) {
            log.warn("Access denied: userId={} tried to cancel orderId={}", userId, orderId);
            throw new AccessDeniedException("You do not have permission to cancel this order.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Cannot cancel orderId={} with status={}", orderId, order.getStatus());
            throw new IllegalArgumentException(
                    "Order cannot be cancelled. Current status: " + order.getStatus()
            );
        }

        for (OrderItem item : order.getItems()) {
            log.debug("Restocking productId={} quantity={}", item.getProductId(), item.getQuantity());
            productFeignClient.increaseStock(
                    item.getProductId(),
                    new StockUpdateRequest(item.getQuantity())
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);
        log.info("Order cancelled successfully: orderId={}, userId={}", orderId, userId);

        return OrderMapper.toDetailsDTO(cancelledOrder);
    }

    @Override
    public List<OrderSummaryDto> getAllOrders() {
        log.info("Fetching all orders (admin view)");
        List<Order> orders = orderRepository.findAll();

        log.debug("Fetched {} total orders", orders.size());
        return orders.stream()
                .map(OrderMapper::toSummaryDTO)
                .toList();
    }

    @Override
    public OrderDetailsDto completeOrderAsAdmin(Long orderId) {
        log.info("Completing orderId={} as admin", orderId);
        Order order = getOrder(orderId);

        if (order.getStatus() != OrderStatus.PAID) {
            log.warn("Cannot complete orderId={} with status={}", orderId, order.getStatus());
            throw new IllegalArgumentException(
                    "Only paid orders can be marked as completed. Current status: " + order.getStatus()
            );
        }

        order.setStatus(OrderStatus.COMPLETED);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order completed successfully: orderId={}", orderId);

        return OrderMapper.toDetailsDTO(updatedOrder);
    }
}
