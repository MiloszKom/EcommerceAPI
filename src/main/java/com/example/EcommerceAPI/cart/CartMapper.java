package com.example.EcommerceAPI.cart;

import com.example.EcommerceAPI.cart.dto.CartDetailsDTO;
import com.example.EcommerceAPI.cart.dto.CartItemDTO;
import com.example.EcommerceAPI.cart.dto.CartSummaryDTO;
import com.example.EcommerceAPI.product.ProductMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class CartMapper {

    public static CartDetailsDTO toDTO(Cart cart) {
        List<CartItemDTO> items = cart.getItems().stream()
                .map(CartMapper::toCartItemDTO)
                .collect(Collectors.toList());

        CartStats stats = calculateTotals(cart);

        return new CartDetailsDTO(
                cart.getId(),
                items,
                stats.totalPrice,
                stats.totalItems
        );
    }

    public static CartSummaryDTO toSummaryDTO(Cart cart) {
        CartStats stats = calculateTotals(cart);

        return new CartSummaryDTO(
                stats.totalPrice,
                stats.totalItems
        );
    }

    private static CartItemDTO toCartItemDTO(CartItem item) {
        return new CartItemDTO(
                ProductMapper.toSummaryDTO(item.getProduct()),
                item.getQuantity()
        );
    }

    private static CartStats calculateTotals(Cart cart) {
        int totalItems = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        BigDecimal totalPrice = cart.getItems().stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartStats(totalPrice, totalItems);
    }

    private record CartStats(BigDecimal totalPrice, int totalItems) {}
}
