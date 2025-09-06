package com.example.EcommerceAPI.order;

import com.example.EcommerceAPI.order.dto.OrderDetailsDTO;
import com.example.EcommerceAPI.order.dto.OrderItemDTO;
import com.example.EcommerceAPI.order.dto.OrderSummaryDTO;
import com.example.EcommerceAPI.order.entity.Order;
import com.example.EcommerceAPI.order.entity.OrderItem;
import com.example.EcommerceAPI.product.ProductMapper;
import com.example.EcommerceAPI.user.UserMapper;

import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderDetailsDTO toDetailsDTO(Order order) {
        return new OrderDetailsDTO(
                order.getId(),
                UserMapper.toSummaryDTO(order.getUser()),
                order.getItems().stream()
                        .map(OrderMapper::toOrderItemDTO)
                        .collect(Collectors.toList()),
                order.getStatus(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private static OrderItemDTO toOrderItemDTO(OrderItem item) {
        return new OrderItemDTO(
                item.getId(),
                ProductMapper.toSummaryDTO(item.getProduct()),
                item.getQuantity(),
                item.getPriceAtPurchase()
        );
    }

    public static OrderSummaryDTO toSummaryDTO(Order order) {
        return new OrderSummaryDTO(
                order.getId(),
                UserMapper.toSummaryDTO(order.getUser()),
                order.getStatus(),
                order.getTotalPrice()
        );
    }
}
