package com.example.cart_service.service;

import com.example.cart_service.dto.ProductDTO;
import com.example.cart_service.exception.types.ConflictException;
import com.example.cart_service.exception.types.NotFoundException;
import com.example.cart_service.exception.types.ServiceCommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProductServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceClient.class);

    private final RestTemplate restTemplate;
    private final String productServiceUrl;

    public ProductServiceClient(RestTemplate restTemplate, @Value("${product.service.url}") String productServiceUrl) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }

    public ProductDTO fetchProductById(Long productId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            logger.info("Fetching product with ID: {} from URL: {}", productId, productServiceUrl + productId);
            ResponseEntity<ProductDTO> response = restTemplate.exchange(
                    productServiceUrl + productId,
                    HttpMethod.GET,
                    entity,
                    ProductDTO.class
            );

            ProductDTO product = response.getBody();
            logger.info("Received response for product ID: {} - Status: {}, Product: {}",
                    productId, response.getStatusCode(), product);

            if (!response.getStatusCode().is2xxSuccessful() || product == null) {
                logger.error("Failed to fetch product with ID: {}. Status: {}", productId, response.getStatusCode());
                throw new NotFoundException("Product with ID " + productId + " does not exist");
            }

            return product;
        } catch (HttpClientErrorException e) {

            logger.error("Error fetching product with ID: {}. Status: {}, Message: {}",
                    productId, e.getStatusCode(), e.getMessage());

            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException("Product with ID " + productId + " does not exist");
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new SecurityException("Unauthorized access to Product Service");
            }
            throw new ServiceCommunicationException("Error communicating with Product Service");
        } catch (ResourceAccessException e) {
            logger.error("Connection error fetching product with ID: {}. Message: {}", productId, e.getMessage());
            throw new ServiceCommunicationException("Unable to connect to Product Service");
        } catch (Exception e) {
            logger.error("Unexpected error fetching product with ID: {}. Exception type: {}", productId, e.getClass().getName(), e);
            throw new ServiceCommunicationException("Unexpected error communicating with Product Service");
        }
    }

}