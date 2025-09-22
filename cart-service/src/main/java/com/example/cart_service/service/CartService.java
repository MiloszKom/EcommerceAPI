package com.example.cart_service.service;

import com.example.cart_service.client.ProductClient;
import com.example.cart_service.config.SecurityUtils;
import com.example.cart_service.dto.AddToCartRequest;
import com.example.cart_service.dto.CartDetailsDTO;
import com.example.cart_service.dto.ProductDTO;
import com.example.cart_service.exception.types.ConflictException;
import com.example.cart_service.exception.types.NotFoundException;
import com.example.cart_service.exception.types.ServiceCommunicationException;
import com.example.cart_service.mapper.CartMapper;
import com.example.cart_service.model.Cart;
import com.example.cart_service.model.CartItem;
import com.example.cart_service.repository.CartRepository;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    @Autowired
    private CartRepository repository;

    @Autowired
    private ProductClient productClient;

    @Autowired
    HttpServletRequest servletRequest;

    public Cart getOrCreateCart(Long userId) {
        return repository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return repository.save(newCart);
                });
    }

    public CartDetailsDTO getUserCart() {
        Long userId = SecurityUtils.getCurrentUserId(servletRequest);
        Cart cart = getOrCreateCart(userId);

        return CartMapper.toCartDetailsDTO(cart);
    }

    @Transactional
    public CartDetailsDTO addProduct(AddToCartRequest request) {
        Long userId = SecurityUtils.getCurrentUserId(servletRequest);
        Cart cart = getOrCreateCart(userId);

        ProductDTO product;
        try {
            product = productClient.getProductById(request.productId());
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Product with ID " + request.productId() + " does not exist");
        } catch (FeignException.Unauthorized e) {
            throw new SecurityException("Unauthorized to fetch product");
        } catch (FeignException e) {
            throw new ServiceCommunicationException("Failed to fetch product details");
        }

        boolean exists = cart.getItems().stream()
                .anyMatch(item -> item.getProductId().equals(request.productId()));
        if (exists) {
            throw new ConflictException("Product already in cart");
        }

        CartItem cartItem = new CartItem();
        cartItem.setProductId(request.productId());
        cartItem.setQuantity(request.quantity());
        cart.addItem(cartItem);

        Cart updatedCart = repository.save(cart);

        return CartMapper.toCartDetailsDTO(updatedCart);
    }

    @Transactional
    public CartDetailsDTO updateQuantity(AddToCartRequest request) {
        Long userId = SecurityUtils.getCurrentUserId(servletRequest);
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.productId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Product with ID " + request.productId() + " is not in the cart"));

        int newQuantity = request.quantity();

        Cart updatedCart = repository.save(cart);

        cartItem.setQuantity(newQuantity);
        return CartMapper.toCartDetailsDTO(updatedCart);
    }

    @Transactional
    public void removeProduct(Long productId) {
        Long userId = SecurityUtils.getCurrentUserId(servletRequest);
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Product with ID " + productId + " is not in the cart"));

        cart.removeItem(cartItem);

        repository.save(cart);
    }

    public void clearCart() {
        Long userId = SecurityUtils.getCurrentUserId(servletRequest);
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        repository.save(cart);
    }
}
