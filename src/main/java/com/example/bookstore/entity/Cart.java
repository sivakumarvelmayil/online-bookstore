
package com.example.bookstore.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CartItem> items = new ArrayList<>();

    protected Cart() {}

    public Cart(AppUser user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void add(Book book, int quantity) {

        CartItem existingItem = items.stream()
                .filter(item ->
                        Objects.equals(
                                item.getBook().getId(),
                                book.getId()
                        )
                )
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.changeQuantity(
                    existingItem.getQuantity() + quantity
            );
        } else {
            items.add(new CartItem(this, book, quantity));
        }
    }

    public void setQuantity(Long bookId, int quantity) {
        CartItem item = items.stream()
                .filter(i ->
                        Objects.equals(i.getBook().getId(), bookId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Book is not in cart")
                );

        item.changeQuantity(quantity);
    }

    public void remove(Long bookId) {
        items.removeIf(i ->
                Objects.equals(i.getBook().getId(), bookId)
        );
    }

    public void clear() {
        items.clear();
    }
}
