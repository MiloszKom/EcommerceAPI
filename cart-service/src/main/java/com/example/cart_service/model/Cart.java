package com.example.cart_service.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    private int totalItems = 0;

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }
}
