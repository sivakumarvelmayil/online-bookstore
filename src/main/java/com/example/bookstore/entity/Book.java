
package com.example.bookstore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "books",
        indexes = {
                @jakarta.persistence.Index(
                        name = "idx_books_title",
                        columnList = "title"
                ),
                @jakarta.persistence.Index(
                        name = "idx_books_author",
                        columnList = "author"
                )
        }
)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 160)
    private String author;

    @Column(
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * Required by JPA.
     */
    protected Book() {
    }

    public Book(
            String title,
            String author,
            BigDecimal price,
            Integer stock,
            String description) {

        this.title = validateText(title, "Title");
        this.author = validateText(author, "Author");
        this.price = validatePrice(price);
        this.stock = validateStock(stock);
        this.description = normalizeDescription(description);
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    private static String validateText(String value, String fieldName) {

        Objects.requireNonNull(value, fieldName + " cannot be null");

        String trimmedValue = value.trim();

        if (trimmedValue.isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be empty"
            );
        }

        return trimmedValue;
    }

    private static BigDecimal validatePrice(BigDecimal price) {

        Objects.requireNonNull(price, "Price cannot be null");

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Price cannot be negative"
            );
        }

        if (price.scale() > 2) {
            throw new IllegalArgumentException(
                    "Price cannot have more than 2 decimal places"
            );
        }

        if (price.precision() > 10) {
            throw new IllegalArgumentException(
                    "Price exceeds the maximum supported value"
            );
        }

        return price;
    }

    private static Integer validateStock(Integer stock) {

        Objects.requireNonNull(stock, "Stock cannot be null");

        if (stock < 0) {
            throw new IllegalArgumentException(
                    "Stock cannot be negative"
            );
        }

        return stock;
    }

    private static String normalizeDescription(String description) {

        if (description == null) {
            return null;
        }

        String trimmedDescription = description.trim();

        if (trimmedDescription.length() > 1000) {
            throw new IllegalArgumentException(
                    "Description cannot exceed 1000 characters"
            );
        }

        return trimmedDescription.isEmpty() ? null : trimmedDescription;
    }

    public void decreaseStock(int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        if (stock < quantity) {
            throw new IllegalArgumentException(
                    "Insufficient stock"
            );
        }

        stock -= quantity;
    }

    public void increaseStock(int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        stock = Math.addExact(stock, quantity);
    }

    public void updatePrice(BigDecimal newPrice) {
        this.price = validatePrice(newPrice);
    }

    public void updateDetails(
            String title,
            String author,
            String description) {

        this.title = validateText(title, "Title");
        this.author = validateText(author, "Author");
        this.description = normalizeDescription(description);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
