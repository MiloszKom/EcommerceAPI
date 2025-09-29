package com.example.cart_service;

import com.example.cart_service.dto.AddToCartRequest;
import com.example.cart_service.dto.CartDetailsDTO;
import com.example.cart_service.dto.ProductDTO;
import com.example.cart_service.exception.types.ConflictException;
import com.example.cart_service.exception.types.NotFoundException;
import com.example.cart_service.model.Cart;
import com.example.cart_service.model.CartItem;
import com.example.cart_service.repository.CartRepository;
import com.example.cart_service.service.CartService;
import com.example.cart_service.service.ProductClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository repository;

    @Mock
    private ProductClientService productClientService;

    private CartService underTest;

    @BeforeEach
    void setUp() {
        underTest = new CartService(repository, productClientService);
    }

    @Test
    void getUserCart_whenCartExists_returnsCartDetailsDTO() {
        Cart cart = new Cart();
        ReflectionTestUtils.setField(cart, "id", 100L);
        cart.setUserId(1L);
        CartItem item = new CartItem();
        item.setProductId(10L);
        item.setQuantity(2);
        cart.addItem(item);

        when(repository.findByUserId(1L)).thenReturn(Optional.of(cart));

        CartDetailsDTO result = underTest.getUserCart(1L);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).productId()).isEqualTo(10L);
        assertThat(result.items().get(0).quantity()).isEqualTo(2);
    }

    @Test
    void getOrCreateCart_whenCartExists_returnsExistingCart() {
        Cart existing = new Cart();
        existing.setUserId(1L);

        when(repository.findByUserId(1L)).thenReturn(Optional.of(existing));

        Cart result = underTest.getOrCreateCart(1L);

        assertThat(result).isEqualTo(existing);
        verify(repository, never()).save(any(Cart.class));
    }

    @Test
    void getOrCreateCart_whenCartDoesNotExist_createsNewCart() {
        when(repository.findByUserId(1L)).thenReturn(Optional.empty());

        Cart saved = new Cart();
        saved.setUserId(1L);

        when(repository.save(any(Cart.class))).thenReturn(saved);

        Cart result = underTest.getOrCreateCart(1L);

        assertThat(result.getUserId()).isEqualTo(1L);
        verify(repository).save(any(Cart.class));
    }

    @Test
    void addProduct_whenNotInCart_addsAndSaves() {
        Cart cart = new Cart();
        ReflectionTestUtils.setField(cart, "id", 100L);
        cart.setUserId(1L);

        when(repository.findByUserId(1L)).thenReturn(Optional.of(cart));

        ProductDTO productDTO = new ProductDTO(10L, "Fender Stratocaster", "Description","guitars",
                BigDecimal.valueOf(1200.00), 5);
        when(productClientService.getProductById(10L)).thenReturn(productDTO);

        AddToCartRequest request = new AddToCartRequest(10L, 2);

        when(repository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        CartDetailsDTO result = underTest.addProduct(1L, request);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).productId()).isEqualTo(10L);
        assertThat(result.items().get(0).quantity()).isEqualTo(2);

        verify(repository).save(cart);
    }

    @Test
    void addProduct_whenAlreadyInCart_throwsConflictException() {
        Cart cart = new Cart();
        cart.setUserId(1L);

        CartItem item = new CartItem();
        item.setProductId(10L);
        item.setQuantity(1);
        cart.addItem(item);

        when(repository.findByUserId(1L)).thenReturn(Optional.of(cart));

        AddToCartRequest request = new AddToCartRequest(10L, 2);

        assertThatThrownBy(() -> underTest.addProduct(1L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Product already in cart");

        verify(repository, never()).save(any(Cart.class));
    }

    @Test
    void addProduct_whenProductNotFound_throwsNotFoundException() {
        Cart cart = new Cart();
        cart.setUserId(1L);
        when(repository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productClientService.getProductById(10L)).thenThrow(new NotFoundException("Product not found"));

        AddToCartRequest request = new AddToCartRequest(10L, 2);

        assertThatThrownBy(() -> underTest.addProduct(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void updateQuantity_whenProductExists_updatesQuantity() {
        Cart cart = new Cart();
        ReflectionTestUtils.setField(cart, "id", 100L);
        cart.setUserId(1L);

        CartItem item = new CartItem();
        item.setProductId(10L);
        item.setQuantity(1);
        cart.addItem(item);

        when(repository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(repository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        AddToCartRequest request = new AddToCartRequest(10L, 5);

        CartDetailsDTO result = underTest.updateQuantity(1L, request);

        assertThat(result.items().get(0).quantity()).isEqualTo(5);
        verify(repository).save(cart);
    }

    @Test
    void updateQuantity_whenProductNotInCart_throwsNotFoundException() {
        Cart cart = new Cart();
        cart.setUserId(1L);

        when(repository.findByUserId(1L)).thenReturn(Optional.of(cart));

        AddToCartRequest request = new AddToCartRequest(10L, 5);

        assertThatThrownBy(() -> underTest.updateQuantity(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Product with ID 10");

        verify(repository, never()).save(any(Cart.class));
    }

    @Test
    void removeProduct_whenExists_removesAndSaves() {
        Cart cart = new Cart();
        cart.setUserId(1L);

        CartItem item = new CartItem();
        item.setProductId(10L);
        item.setQuantity(1);
        cart.addItem(item);

        when(repository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(repository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        underTest.removeProduct(1L, 10L);

        assertThat(cart.getItems()).isEmpty();
        verify(repository).save(cart);
    }

    @Test
    void removeProduct_whenNotInCart_throwsNotFoundException() {
        Cart cart = new Cart();
        cart.setUserId(1L);

        when(repository.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> underTest.removeProduct(1L, 10L))
                .isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any(Cart.class));
    }

    @Test
    void clearCart_clearsAllItems() {
        Cart cart = new Cart();
        cart.setUserId(1L);

        CartItem item1 = new CartItem();
        item1.setProductId(10L);
        cart.addItem(item1);

        CartItem item2 = new CartItem();
        item2.setProductId(20L);
        cart.addItem(item2);

        when(repository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(repository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        underTest.clearCart(1L);

        assertThat(cart.getItems()).isEmpty();
        verify(repository).save(cart);
    }
}
