package com.example.cart_service.service.impl;

import com.example.cart_service.dto.CartDto;
import com.example.cart_service.dto.CartRequest;
import com.example.cart_service.dto.client.ProductDto;
import com.example.cart_service.entity.Cart;
import com.example.cart_service.entity.CartItem;
import com.example.cart_service.exception.ConflictException;
import com.example.cart_service.exception.ResourceNotFoundException;
import com.example.cart_service.mapper.CartMapper;
import com.example.cart_service.repository.CartRepository;
import com.example.cart_service.service.ICartService;
import com.example.cart_service.service.client.ProductFeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final ProductFeignClient productFeignClient;

    public CartServiceImpl(
            CartRepository cartRepository,
            ProductFeignClient productFeignClient
    ) {
        this.cartRepository = cartRepository;
        this.productFeignClient = productFeignClient;
    }


    private Cart getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });
    }

    @Override
    public CartDto getUserCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        return CartMapper.mapToCartDto(cart);
    }

    @Override
    @Transactional
    public CartDto addProductToCart(String userId, CartRequest request) {
        Cart cart = getOrCreateCart(userId);

        boolean exists = cart.getItems().stream()
                .anyMatch(item -> item.getProductId().equals(request.productId()));

        if (exists) {
            throw new ConflictException("Product already in cart");
        }

        ResponseEntity<ProductDto> product = productFeignClient.getProductById(request.productId());

        if (product == null) {
            throw new ResourceNotFoundException("Product", "productId", request.productId());
        }

        CartItem cartItem = new CartItem();
        cartItem.setProductId(request.productId());
        cartItem.setQuantity(request.quantity());
        cart.addItem(cartItem);

        Cart updatedCart = cartRepository.save(cart);
        return CartMapper.mapToCartDto(updatedCart);
    }

    @Override
    @Transactional
    public CartDto updateCartItem(String userId, CartRequest request) {
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.productId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product", "productId", request.productId()));

        cartItem.setQuantity(request.quantity());

        Cart updatedCart = cartRepository.save(cart);
        return CartMapper.mapToCartDto(updatedCart);
    }

    @Override
    @Transactional
    public CartDto removeCartItem(String userId, Long productId) {
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product", "productId", productId));

        cart.removeItem(cartItem);

        Cart updatedCart = cartRepository.save(cart);
        return CartMapper.mapToCartDto(updatedCart);
    }

    @Override
    @Transactional
    public CartDto clearCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();

        Cart updatedCart = cartRepository.save(cart);
        return CartMapper.mapToCartDto(updatedCart);
    }
}
