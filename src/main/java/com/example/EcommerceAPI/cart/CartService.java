package com.example.EcommerceAPI.cart;

import com.example.EcommerceAPI.auth.AuthService;
import com.example.EcommerceAPI.cart.dto.AddToCartRequest;
import com.example.EcommerceAPI.cart.dto.CartDetailsDTO;
import com.example.EcommerceAPI.exception.types.CartNotFoundException;
import com.example.EcommerceAPI.exception.types.ProductNotFoundException;
import com.example.EcommerceAPI.product.Product;
import com.example.EcommerceAPI.product.ProductRepository;
import com.example.EcommerceAPI.product.ProductService;
import com.example.EcommerceAPI.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private AuthService authService;

    private Cart getCart() {
        User currUser = authService.getCurrentUser();
        return cartRepository.findByUserId(currUser.getId())
                .orElseThrow(() -> new CartNotFoundException(currUser.getId()));
    }

    public CartDetailsDTO getUserCart() {
        Cart userCart = getCart();
        return CartMapper.toDTO(userCart);
    }

    public CartDetailsDTO addProduct(AddToCartRequest request) {
        Cart cart = getCart();

        boolean exists = cart.getItems().stream()
                .anyMatch(item -> item.getProduct().getId().equals(request.getProductId()));

        if (exists) {
            throw new IllegalArgumentException("Product is already in the cart.");
        }

        Product product = productService.getProduct(request.getProductId());

        CartItem newItem = new CartItem();
        newItem.setProduct(product);
        newItem.setQuantity(request.getQuantity());
        newItem.setCart(cart);

        cart.addItem(newItem);

        Cart updatedCart = cartRepository.save(cart);
        return CartMapper.toDTO(updatedCart);
    }

    public CartDetailsDTO updateQuantity(AddToCartRequest request) {
        Cart cart = getCart();

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product with id " + request.getProductId() + " is not in the cart"));

        existingItem.setQuantity(request.getQuantity());

        Cart updatedCart = cartRepository.save(cart);

        return CartMapper.toDTO(updatedCart);
    }

    public void removeProduct(Long productId) {
        Cart cart = getCart();

        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException(productId));

        cart.removeItem(itemToRemove);
        cartRepository.save(cart);
    }

    public void clearCart() {
        Cart cart = getCart();
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
