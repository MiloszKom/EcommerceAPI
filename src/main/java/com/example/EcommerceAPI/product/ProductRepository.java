package com.example.EcommerceAPI.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Product> findProducts(@Param("category") String category,
                               @Param("maxPrice") BigDecimal price);
}
