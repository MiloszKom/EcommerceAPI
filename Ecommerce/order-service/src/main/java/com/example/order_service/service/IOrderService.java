package com.example.order_service.service;

import com.example.order_service.dto.OrderDetailsDto;
import com.example.order_service.dto.OrderSummaryDto;

import java.util.List;


public interface IOrderService {
    OrderDetailsDto createOrder(String userId);
    OrderDetailsDto payOrder(String userId, Long orderId);
    List<OrderSummaryDto> getCurrentUserOrders(String userId);
    OrderDetailsDto getOrderDetails(String userId, Boolean isAdmin, Long orderId);
    OrderDetailsDto cancelOrder(String userId, Boolean isAdmin, Long orderId);
    List<OrderSummaryDto> getAllOrders();
    OrderDetailsDto completeOrderAsAdmin(Long orderId);
}
