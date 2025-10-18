package com.example.product_service.mapper;

import com.example.product_service.dto.ProductDetailsDto;
import com.example.product_service.dto.ProductSummaryDto;
import com.example.product_service.entity.Product;

public class ProductMapper {

    public static ProductDetailsDto toDetailsDto(Product product) {
        return new ProductDetailsDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public static ProductSummaryDto toSummaryDto(Product product) {
        return new ProductSummaryDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock()
        );
    }
}
