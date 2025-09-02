package com.example.EcommerceAPI.product;

import com.example.EcommerceAPI.product.dto.ProductDetailsDTO;
import com.example.EcommerceAPI.product.dto.ProductSummaryDTO;

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
