package com.example.EcommerceAPI.order;

import com.example.EcommerceAPI.auth.AuthService;
import com.example.EcommerceAPI.cart.CartService;
import com.example.EcommerceAPI.cart.dto.CartDetailsDTO;
import com.example.EcommerceAPI.exception.types.OrderNotFoundException;
import com.example.EcommerceAPI.exception.types.UnauthorizedException;
import com.example.EcommerceAPI.order.dto.OrderDetailsDTO;
import com.example.EcommerceAPI.order.dto.OrderSummaryDTO;
import com.example.EcommerceAPI.order.entity.Order;
import com.example.EcommerceAPI.order.entity.OrderItem;
import com.example.EcommerceAPI.order.entity.OrderStatus;
import com.example.EcommerceAPI.product.Product;
import com.example.EcommerceAPI.product.ProductService;
import com.example.EcommerceAPI.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ProductService productService;

    @Autowired
    AuthService authService;

    @Autowired
    private CartService cartService;

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Transactional
    public OrderDetailsDTO createOrder() {
        User currentUser = authService.getCurrentUser();

        CartDetailsDTO cart = cartService.getUserCart();

        if (cart.items().isEmpty()) {
            throw new IllegalArgumentException("Cannot create order: cart is empty.");
        }

        Order order = new Order();
        order.setUser(currentUser);

        List<OrderItem> orderItems = cart.items().stream()
                .map(itemDTO -> {
                    Product product = productService.getProduct(itemDTO.product().id());

                    productService.validateStock(product.getId(), itemDTO.quantity());

                    productService.decrementStock(product.getId(), itemDTO.quantity());

                    OrderItem item = new OrderItem();
                    item.setProduct(product);
                    item.setQuantity(itemDTO.quantity());
                    item.setPriceAtPurchase(product.getPrice());
                    item.setOrder(order);
                    return item;
                })
                .toList();

        order.setItems(orderItems);

        BigDecimal totalPrice = orderItems.stream()
                .map(i -> i.getPriceAtPurchase().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);

        cartService.clearCart();

        return OrderMapper.toDetailsDTO(savedOrder);
    }

    @Transactional
    public OrderDetailsDTO payOrder(Long orderId) {
        User currentUser = authService.getCurrentUser();
        Order order = getOrder(orderId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Order cannot be paid. Current status: " + order.getStatus());
        }

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You do not have permission to pay for this order.");
        }

        for (OrderItem item : order.getItems()) {
            productService.validateStock(item.getProduct().getId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.PAID);

        Order completedOrder = orderRepository.save(order);
        return OrderMapper.toDetailsDTO(completedOrder);
    }

    public List<OrderSummaryDTO> getUserOrders() {
        User currentUser = authService.getCurrentUser();

        return currentUser.getOrders().stream()
                .map(OrderMapper::toSummaryDTO)
                .toList();
    }

    public OrderDetailsDTO getOrderDetails(Long orderId) {
        Order order = getOrder(orderId);
        User currentUser = authService.getCurrentUser();

        if (currentUser.getRole() != User.Role.ADMIN && !order.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You cannot access this order.");
        }

        return OrderMapper.toDetailsDTO(order);
    }

    @Transactional
    public OrderDetailsDTO cancelOrder(Long orderId) {
        User currentUser = authService.getCurrentUser();
        Order order = getOrder(orderId);

        if (currentUser.getRole() != User.Role.ADMIN
                && !order.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You do not have permission to cancel this order.");
        }


        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Order cannot be cancelled. Current status: " + order.getStatus()
            );
        }

        for (OrderItem item : order.getItems()) {
            productService.incrementStock(item.getProduct().getId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);

        return OrderMapper.toDetailsDTO(cancelledOrder);
    }

    public List<OrderSummaryDTO> getAllOrders(OrderStatus status) {
        List<Order> orders = orderRepository.findOrders(status);
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

        order.setStatus(OrderStatus.COMPLETED);  // new status
        Order updatedOrder = orderRepository.save(order);

        return OrderMapper.toDetailsDTO(updatedOrder);
    }
}
