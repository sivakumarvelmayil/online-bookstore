
package com.example.bookstore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PLACED;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<OrderItem> items = new ArrayList<>();

    protected CustomerOrder() {
        // Required by JPA
    }

    public CustomerOrder(AppUser user) {
        this.user = Objects.requireNonNull(
                user, "User cannot be null"
        );
        this.total = BigDecimal.ZERO;
        this.status = OrderStatus.PLACED;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (total == null) {
            total = BigDecimal.ZERO;
        }

        if (status == null) {
            status = OrderStatus.PLACED;
        }
    }

    public void addItem(Book book, int quantity, BigDecimal unitPrice) {

        Objects.requireNonNull(book, "Book cannot be null");
        Objects.requireNonNull(unitPrice, "Unit price cannot be null");

        if (quantity < 1 || quantity > 99) {
            throw new IllegalArgumentException(
                    "Quantity must be between 1 and 99"
            );
        }

        if (unitPrice.signum() < 0 || unitPrice.scale() > 2) {
            throw new IllegalArgumentException(
                    "Unit price must be non-negative with at most 2 decimal places"
            );
        }

        OrderItem item = new OrderItem(
                this, book, quantity, unitPrice
        );

        items.add(item);

        BigDecimal itemTotal = unitPrice.multiply(
                BigDecimal.valueOf(quantity)
        );

        this.total = this.total.add(itemTotal);
    }

    public void changeStatus(OrderStatus newStatus) {

        Objects.requireNonNull(
                newStatus, "Order status cannot be null");

        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Cannot change order status from "
                            + status + " to " + newStatus);
        }

        this.status = newStatus;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
