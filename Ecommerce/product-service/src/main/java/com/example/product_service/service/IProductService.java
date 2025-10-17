package com.example.product_service.service;

import com.example.product_service.dto.ProductDetailsDto;
import com.example.product_service.dto.ProductRequestDto;
import com.example.product_service.dto.ProductSummaryDto;

import java.util.List;

public interface IProductService {
    List<ProductSummaryDto> getProducts();
    ProductDetailsDto getProductById(long productId);
    ProductDetailsDto createProduct(ProductRequestDto productRequestDto);
    ProductDetailsDto updateProduct(long productId, ProductRequestDto productRequestDto);
    void deleteProduct(long productId);

    void reduceStock(long productId, Integer quantity);
    void increaseStock(long productId, Integer quantity);
}
