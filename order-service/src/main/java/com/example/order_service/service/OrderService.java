package com.example.order_service.service;

import com.example.order_service.client.CartClient;
import com.example.order_service.client.ProductClient;
import com.example.order_service.dto.*;
import com.example.order_service.exception.types.*;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderItem;
import com.example.order_service.model.OrderStatus;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.config.SecurityUtils;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    CartClient cartClient;

    @Autowired
    ProductClient productClient;

    @Autowired
    private HttpServletRequest request;

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Cannot find order with id: " + orderId));
    }

    @Transactional
    public OrderDetailsDTO createOrder() {
        Long userId = SecurityUtils.getCurrentUserId(request);

        CartDetailsDTO cart;

        try {
            cart = cartClient.getUserCart();
        } catch (Exception e) {
            throw new ServiceCommunicationException("Error communicating with Cart Service");
        }

        if (cart.items().isEmpty()) {
            throw new IllegalArgumentException("Cannot create order: cart is empty.");
        }

        Order order = new Order();
        order.setUserId(userId);

        List<OrderItem> orderItems = cart.items().stream().map(cartItem -> {
            ProductDTO product = productClient.getProductById(cartItem.productId());

            try {
                productClient.reduceProductStock(
                        cartItem.productId(),
                        new StockUpdateRequest(cartItem.quantity())
                );
            } catch (FeignException.Conflict e) {
                throw new ConflictException(
                        "Not enough stock for product ID " + cartItem.productId()
                );
            }

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

        try {
            cartClient.clearCart();
        } catch (Exception e) {
            throw new ServiceCommunicationException("Error communicating with Cart Service");
        }

        return OrderMapper.toDetailsDTO(savedOrder);
    }

    public OrderDetailsDTO payOrder(Long orderId) {
        Long currentUserId = SecurityUtils.getCurrentUserId(request);
        Order order = getOrder(orderId);

        if (!order.getUserId().equals(currentUserId)) {
            throw new UnauthorizedException("You do not have permission to pay for this order.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ConflictException("Order cannot be paid. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.PAID);
        Order completedOrder = orderRepository.save(order);

        return OrderMapper.toDetailsDTO(completedOrder);
    }

    public List<OrderSummaryDTO> getUserOrders() {
        String userIdHeader = request.getHeader("userId");
        Long userId = Long.parseLong(userIdHeader);

        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(OrderMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    public OrderDetailsDTO getOrderDetails(Long orderId) {
        Long currentUserId = SecurityUtils.getCurrentUserId(request);
        String role = SecurityUtils.getCurrentUserRole(request);

        Order order = getOrder(orderId);

        if (!order.getUserId().equals(currentUserId) && !"ROLE_ADMIN".equals(role)) {
            throw new UnauthorizedException("You cannot access this order.");
        }

        return OrderMapper.toDetailsDTO(order);
    }

    @Transactional
    public OrderDetailsDTO cancelOrder(Long orderId) {
        Long currentUserId = SecurityUtils.getCurrentUserId(request);
        String role = SecurityUtils.getCurrentUserRole(request);


        Order order = getOrder(orderId);

        if (!order.getUserId().equals(currentUserId) && !"ROLE_ADMIN".equals(role)) {
            throw new AccessDeniedException("You do not have permission to cancel this order.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Order cannot be cancelled. Current status: " + order.getStatus()
            );
        }

        for (OrderItem item : order.getItems()) {
            try {
                productClient.increaseProductStock(
                        item.getProductId(),
                        new StockUpdateRequest(item.getQuantity())
                );
            } catch (FeignException e) {
                throw new ServiceCommunicationException(
                        "Failed to update stock for product ID " + item.getProductId()
                );
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);

        return OrderMapper.toDetailsDTO(cancelledOrder);
    }

    public List<OrderSummaryDTO> getAllOrders(OrderStatus status) {
        List<Order> orders;

        if (status != null) {
            orders = orderRepository.findByStatus(status);
        } else {
            orders = orderRepository.findAll();
        }

        return orders.stream()
                .map(OrderMapper::toSummaryDTO)
                .toList();
    }

    public OrderDetailsDTO completeOrderAsAdmin(Long orderId) {
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
