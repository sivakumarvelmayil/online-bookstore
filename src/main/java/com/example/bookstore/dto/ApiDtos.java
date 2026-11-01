
package com.example.bookstore.dto;

import com.example.bookstore.entity.OrderStatus;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ApiDtos {

    private ApiDtos() {
        // Utility class
    }

    // =========================
    // Authentication DTOs
    // =========================

    public record RegisterRequest(

            @NotBlank(message = "Name is required")
            @Size(max = 120, message = "Name cannot exceed 120 characters")
            String name,

            @NotBlank(message = "Email is required")
            @Email(message = "Please provide a valid email address")
            @Size(max = 190, message = "Email cannot exceed 190 characters")
            String email,

            @NotBlank(message = "Password is required")
            @Size(
                    min = 8,
                    max = 72,
                    message = "Password must be between 8 and 72 characters"
            )
            String password
    ) {}

    public record LoginRequest(

            @NotBlank(message = "Email is required")
            @Email(message = "Please provide a valid email address")
            @Size(max = 190, message = "Email cannot exceed 190 characters")
            String email,

            @NotBlank(message = "Password is required")
            String password
    ) {}

    public record AuthResponse(
            String token,
            Long userId,
            String name,
            String email
    ) {}


    // =========================
    // Book DTOs
    // =========================

    public record CreateBookRequest(

            @NotBlank(message = "Book title is required")
            @Size(max = 200, message = "Title cannot exceed 200 characters")
            String title,

            @NotBlank(message = "Author is required")
            @Size(max = 160, message = "Author cannot exceed 160 characters")
            String author,

            @NotNull(message = "Price is required")
            @DecimalMin(value = "0.00", message = "Price cannot be negative")
            @Digits(integer = 8, fraction = 2,
                    message = "Price must have at most 8 integer digits and 2 decimal places")
            BigDecimal price,

            @NotNull(message = "Stock is required")
            @Min(value = 0, message = "Stock cannot be negative")
            Integer stock,

            @Size(max = 1000, message = "Description cannot exceed 1000 characters")
            String description
    ) {}

    public record UpdateBookRequest(

            @NotBlank(message = "Book title is required")
            @Size(max = 200, message = "Title cannot exceed 200 characters")
            String title,

            @NotBlank(message = "Author is required")
            @Size(max = 160, message = "Author cannot exceed 160 characters")
            String author,

            @NotNull(message = "Price is required")
            @DecimalMin(value = "0.00", message = "Price cannot be negative")
            @Digits(integer = 8, fraction = 2)
            BigDecimal price,

            @NotNull(message = "Stock is required")
            @Min(value = 0, message = "Stock cannot be negative")
            Integer stock,

            @Size(max = 1000, message = "Description cannot exceed 1000 characters")
            String description
    ) {}

    public record BookResponse(
            Long id,
            String title,
            String author,
            BigDecimal price,
            Integer stock,
            String description
    ) {}


    // =========================
    // Cart DTOs
    // =========================

    public record AddCartItemRequest(

            @NotNull(message = "Book ID is required")
            @Positive(message = "Book ID must be positive")
            Long bookId,

            @NotNull(message = "Quantity is required")
            @Min(value = 1, message = "Quantity must be at least 1")
            @Max(value = 99, message = "Quantity cannot exceed 99")
            Integer quantity
    ) {}

    public record UpdateCartItemRequest(

            @NotNull(message = "Quantity is required")
            @Min(value = 1, message = "Quantity must be at least 1")
            @Max(value = 99, message = "Quantity cannot exceed 99")
            Integer quantity
    ) {}

    public record CartLine(
            Long bookId,
            String title,
            String author,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal
    ) {}

    public record CartResponse(
            List<CartLine> items,
            BigDecimal total,
            int totalItems
    ) {
        public CartResponse {
            items = items == null ? List.of() : List.copyOf(items);
        }
    }


    // =========================
    // Order DTOs
    // =========================

    public record OrderLine(
            Long bookId,
            String title,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {}

    public record OrderResponse(
            Long id,
            BigDecimal total,
            OrderStatus status,
            Instant createdAt,
            List<OrderLine> items
    ) {
        public OrderResponse {
            items = items == null ? List.of() : List.copyOf(items);
        }
    }


    // =========================
    // Error DTO
    // =========================

    public record ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path
    ) {}
}
