
package com.example.bookstore.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    protected OrderItem() {
        // Required by JPA
    }

    public OrderItem(
            CustomerOrder order,
            Book book,
            int quantity,
            BigDecimal unitPrice) {

        this.order = Objects.requireNonNull(
                order, "Order cannot be null");

        this.book = Objects.requireNonNull(
                book, "Book cannot be null");

        changeQuantity(quantity);
        setUnitPrice(unitPrice);
    }

    public Long getId() {
        return id;
    }

    public CustomerOrder getOrder() {
        return order;
    }

    public Book getBook() {
        return book;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(
                BigDecimal.valueOf(quantity));
    }

    private void changeQuantity(int quantity) {

        if (quantity < 1 || quantity > 99) {
            throw new IllegalArgumentException(
                    "Quantity must be between 1 and 99");
        }

        this.quantity = quantity;
    }

    private void setUnitPrice(BigDecimal unitPrice) {

        Objects.requireNonNull(
                unitPrice, "Unit price cannot be null");

        if (unitPrice.signum() < 0 || unitPrice.scale() > 2
                || unitPrice.precision() > 10) {
            throw new IllegalArgumentException(
                    "Unit price must be non-negative and fit DECIMAL(10,2)");
        }

        this.unitPrice = unitPrice;
    }
}
