package com.example.EcommerceAPI.product;

import com.example.EcommerceAPI.product.dto.ProductDTO;
import com.example.EcommerceAPI.product.dto.ProductDetailsDTO;
import com.example.EcommerceAPI.product.dto.ProductSummaryDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService service;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<ProductDetailsDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        ProductDetailsDTO created = service.createProduct(productDTO);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/")
    public ResponseEntity<List<ProductSummaryDTO>> getProducts() {
        List<ProductSummaryDTO> products = service.getProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailsDTO> getProductById(@PathVariable Long id) {
        ProductDetailsDTO product = service.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductDetailsDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDTO productDTO
    ) {
        ProductDetailsDTO updated = service.updateProduct(id, productDTO);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        service.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
