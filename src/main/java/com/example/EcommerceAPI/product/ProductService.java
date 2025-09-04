package com.example.EcommerceAPI.product;

import com.example.EcommerceAPI.exception.types.ProductNotFoundException;
import com.example.EcommerceAPI.product.dto.ProductDTO;
import com.example.EcommerceAPI.product.dto.ProductDetailsDTO;
import com.example.EcommerceAPI.product.dto.ProductSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    public Product getProduct(long productId) {
        return repository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    public ProductDetailsDTO createProduct(ProductDTO productDTO) {
        Product product = new Product();

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setCategory(productDTO.getCategory().toLowerCase());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());

        Product saved = repository.save(product);

        return ProductMapper.toDetailsDTO(saved);
    }

    public List<ProductSummaryDTO> getProducts() {
        List<Product> products;

        products = repository.findAll();

        return products.stream()
                .map(ProductMapper::toSummaryDTO)
                .toList();
    }

    public ProductDetailsDTO getProductById(long productId) {
        Product product = getProduct(productId);
        return ProductMapper.toDetailsDTO(product);
    }

    public ProductDetailsDTO updateProduct(long productId, ProductDTO productDTO) {
        Product product = getProduct(productId);

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setCategory(productDTO.getCategory().toLowerCase());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());

        Product updated = repository.save(product);
        return ProductMapper.toDetailsDTO(updated);
    }

    public void deleteProduct(long productId) {
        Product product = getProduct(productId);
        repository.delete(product);
    }
}
