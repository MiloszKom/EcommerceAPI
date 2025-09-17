package com.example.order_service.mapper;

import com.example.order_service.dto.OrderDetailsDTO;
import com.example.order_service.dto.OrderItemDTO;
import com.example.order_service.dto.OrderSummaryDTO;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderDetailsDTO toDetailsDTO(Order order) {
        List<OrderItemDTO> items = order.getItems().stream()
                .map(item -> new OrderItemDTO(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPriceAtPurchase()
                ))
                .collect(Collectors.toList());

        return new OrderDetailsDTO(
                order.getId(),
                order.getUserId(),
                items,
                order.getStatus(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public static OrderSummaryDTO toSummaryDTO(Order order) {
        int numberOfItems = order.getItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        return new OrderSummaryDTO(
                order.getId(),
                order.getStatus(),
                order.getTotalPrice(),
                numberOfItems,
                order.getCreatedAt()
        );
    }
}
