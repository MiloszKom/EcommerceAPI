package com.example.cart_service.service;

import com.example.cart_service.dto.AddToCartRequest;
import com.example.cart_service.dto.CartDetailsDTO;
import com.example.cart_service.dto.ProductDTO;
import com.example.cart_service.exception.types.ConflictException;
import com.example.cart_service.exception.types.NotFoundException;
import com.example.cart_service.mapper.CartMapper;
import com.example.cart_service.model.Cart;
import com.example.cart_service.model.CartItem;
import com.example.cart_service.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository repository;
    private final ProductClientService productClientService;

    public CartService(CartRepository repository, ProductClientService productClientService) {
        this.repository = repository;
        this.productClientService = productClientService;
    }

    public Cart getOrCreateCart(Long userId) {
        return repository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return repository.save(newCart);
                });
    }

    public CartDetailsDTO getUserCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return CartMapper.toCartDetailsDTO(cart);
    }

    @Transactional
    public CartDetailsDTO addProduct(Long userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId);

        boolean exists = cart.getItems().stream()
                .anyMatch(item -> item.getProductId().equals(request.productId()));
        if (exists) {
            throw new ConflictException("Product already in cart");
        }

        ProductDTO product = productClientService.getProductById(request.productId());

        CartItem cartItem = new CartItem();
        cartItem.setProductId(request.productId());
        cartItem.setQuantity(request.quantity());
        cart.addItem(cartItem);

        Cart updatedCart = repository.save(cart);
        return CartMapper.toCartDetailsDTO(updatedCart);
    }

    @Transactional
    public CartDetailsDTO updateQuantity(Long userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.productId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Product with ID " + request.productId() + " is not in the cart"));

        cartItem.setQuantity(request.quantity());
        Cart updatedCart = repository.save(cart);

        return CartMapper.toCartDetailsDTO(updatedCart);
    }

    @Transactional
    public void removeProduct(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Product with ID " + productId + " is not in the cart"));

        cart.removeItem(cartItem);
        repository.save(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        repository.save(cart);
    }
}
