package com.example.cart_service.service.impl;

import com.example.cart_service.dto.CartDto;
import com.example.cart_service.dto.CartRequest;
import com.example.cart_service.dto.UpdateCartItemRequest;
import com.example.cart_service.dto.client.ProductDto;
import com.example.cart_service.entity.Cart;
import com.example.cart_service.entity.CartItem;
import com.example.cart_service.exception.ConflictException;
import com.example.cart_service.exception.ExternalServiceException;
import com.example.cart_service.exception.ResourceNotFoundException;
import com.example.cart_service.mapper.CartMapper;
import com.example.cart_service.repository.CartRepository;
import com.example.cart_service.service.ICartService;
import com.example.cart_service.service.client.ProductFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartServiceImpl implements ICartService {
    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final ProductFeignClient productFeignClient;

    public CartServiceImpl(
            CartRepository cartRepository,
            ProductFeignClient productFeignClient
    ) {
        this.cartRepository = cartRepository;
        this.productFeignClient = productFeignClient;
    }


    private Cart getOrCreateCart(String userId) throws ExternalServiceException {
        log.debug("Fetching cart for userId={}", userId);

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Cart not found for userId={}, creating new cart", userId);
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });
    }

    @Override
    public CartDto getUserCart(String userId) {;
        log.debug("Retrieving user cart for userId={}", userId);
        Cart cart = getOrCreateCart(userId);

        log.info("Cart retrieved for userId={}", userId);
        return CartMapper.mapToCartDto(cart);
    }

    @Override
    @Transactional
    public CartDto addProductToCart(String userId, CartRequest request) {
        log.info("UserId={} attempting to add productId={} to cart", userId, request.productId());
        Cart cart = getOrCreateCart(userId);

        boolean exists = cart.getItems().stream()
                .anyMatch(item -> item.getProductId().equals(request.productId()));

        if (exists) {
            log.warn("UserId={} tried to add productId={} which already exists in cart", userId, request.productId());
            throw new ConflictException("Product already in cart");
        }

        log.debug("Checking productId={} exists via ProductFeignClient", request.productId());
        ProductDto product = productFeignClient.getProductById(request.productId());
        log.info("Product existence successfully confirmed for productId={} (name={})", product.id(), product.name());

        CartItem cartItem = new CartItem();
        cartItem.setProductId(request.productId());
        cartItem.setQuantity(request.quantity());
        cart.addItem(cartItem);

        Cart updatedCart = cartRepository.save(cart);
        log.info("UserId={} added productId={} successfully. Total items now: {}",
                userId, request.productId(),
                updatedCart.getItems().size());
        return CartMapper.mapToCartDto(updatedCart);
    }

    @Override
    @Transactional
    public CartDto updateCartItem(String userId, Long itemId, UpdateCartItemRequest request) {
        log.info("UserId={} updating itemId={} quantity={}", userId, itemId, request.quantity());
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("UserId={} attempted to update itemId={} that doesn't exist in cart",
                            userId, itemId);
                    return new ResourceNotFoundException("Cart item", "id", itemId);
                });

        cartItem.setQuantity(request.quantity());

        Cart updatedCart = cartRepository.save(cart);
        log.debug("Updated quantity for itemId={} in userId={} cart", itemId, userId);
        return CartMapper.mapToCartDto(updatedCart);
    }

    @Override
    @Transactional
    public CartDto removeCartItem(String userId, Long itemId) {
        log.info("UserId={} removing itemId={} from cart", userId, itemId);
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("UserId={} tried to remove itemId={} that doesn't exist", userId, itemId);
                    return new ResourceNotFoundException("CartItem", "id", itemId);
                });

        cart.removeItem(cartItem);

        Cart updatedCart = cartRepository.save(cart);
        log.info("UserId={} removed itemId={} successfully", userId, itemId);
        return CartMapper.mapToCartDto(updatedCart);
    }

    @Override
    @Transactional
    public CartDto clearCart(String userId) {
        log.warn("UserId={} clearing entire cart", userId);
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();

        Cart updatedCart = cartRepository.save(cart);
        log.info("Cart cleared successfully for userId={}", userId);
        return CartMapper.mapToCartDto(updatedCart);
    }
}
