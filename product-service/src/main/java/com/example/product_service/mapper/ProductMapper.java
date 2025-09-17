package com.example.product_service.mapper;

import com.example.product_service.dto.ProductDetailsDTO;
import com.example.product_service.dto.ProductSummaryDTO;
import com.example.product_service.model.Product;

public class ProductMapper {

    public static ProductDetailsDTO toDetailsDTO(Product product) {
        return new ProductDetailsDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getStock(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public static ProductSummaryDTO toSummaryDTO(Product product) {
        return new ProductSummaryDTO(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStock()
        );
    }
}
