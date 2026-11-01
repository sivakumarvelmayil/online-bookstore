
package com.example.bookstore.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    private AppUser user;
    private Book book;
    private CustomerOrder order;

    @BeforeEach
    void setUp() {
        user = new AppUser(
                "John Doe",
                "john@example.com",
                "hashedPassword123"
        );

        book = new Book(
                "Clean Code",
                "Robert C. Martin",
                new BigDecimal("599.00"),
                15,
                "A handbook of agile software craftsmanship"
        );

        order = new CustomerOrder(user);
    }

    // Constructor tests

    @Test
    void shouldCreateOrderItemWithValidDetails() {
        OrderItem item = new OrderItem(
                order,
                book,
                2,
                new BigDecimal("599.00")
        );

        assertNull(item.getId());
        assertSame(order, item.getOrder());
        assertSame(book, item.getBook());
        assertEquals(2, item.getQuantity());
        assertEquals(new BigDecimal("599.00"), item.getUnitPrice());
    }

    @Test
    void shouldThrowExceptionWhenOrderIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new OrderItem(
                        null,
                        book,
                        1,
                        new BigDecimal("599.00")
                )
        );

        assertEquals("Order cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenBookIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new OrderItem(
                        order,
                        null,
                        1,
                        new BigDecimal("599.00")
                )
        );

        assertEquals("Book cannot be null", exception.getMessage());
    }

    // Quantity validation tests

    @Test
    void shouldAllowMinimumQuantity() {
        OrderItem item = new OrderItem(
                order,
                book,
                1,
                new BigDecimal("599.00")
        );

        assertEquals(1, item.getQuantity());
    }

    @Test
    void shouldAllowMaximumQuantity() {
        OrderItem item = new OrderItem(
                order,
                book,
                99,
                new BigDecimal("599.00")
        );

        assertEquals(99, item.getQuantity());
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsZero() {
        assertEquals(
                "Quantity must be between 1 and 99",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new OrderItem(
                                order,
                                book,
                                0,
                                new BigDecimal("599.00")
                        )
                ).getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsNegative() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderItem(
                        order,
                        book,
                        -1,
                        new BigDecimal("599.00")
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenQuantityExceeds99() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderItem(
                        order,
                        book,
                        100,
                        new BigDecimal("599.00")
                )
        );
    }

    // Unit price validation tests

    @Test
    void shouldThrowExceptionWhenUnitPriceIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new OrderItem(
                        order,
                        book,
                        1,
                        null
                )
        );

        assertEquals(
                "Unit price cannot be null",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenUnitPriceIsNegative() {
        assertEquals(
                "Unit price must be non-negative and fit DECIMAL(10,2)",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new OrderItem(
                                order,
                                book,
                                1,
                                new BigDecimal("-10.00")
                        )
                ).getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenUnitPriceHasMoreThanTwoDecimalPlaces() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderItem(
                        order,
                        book,
                        1,
                        new BigDecimal("599.999")
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenUnitPriceExceedsPrecision10() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderItem(
                        order,
                        book,
                        1,
                        new BigDecimal("123456789.00")
                )
        );
    }

    @Test
    void shouldAllowMaximumSupportedUnitPrice() {
        BigDecimal maximumPrice = new BigDecimal("99999999.99");

        OrderItem item = new OrderItem(
                order,
                book,
                1,
                maximumPrice
        );

        assertEquals(maximumPrice, item.getUnitPrice());
    }

    @Test
    void shouldAllowZeroUnitPrice() {
        OrderItem item = new OrderItem(
                order,
                book,
                1,
                BigDecimal.ZERO
        );

        assertEquals(BigDecimal.ZERO, item.getUnitPrice());
    }

    // Subtotal calculation tests

    @Test
    void shouldCalculateSubtotalForSingleQuantity() {
        OrderItem item = new OrderItem(
                order,
                book,
                1,
                new BigDecimal("599.00")
        );

        assertEquals(
                new BigDecimal("599.00"),
                item.getSubtotal()
        );
    }

    @Test
    void shouldCalculateSubtotalForMultipleQuantity() {
        OrderItem item = new OrderItem(
                order,
                book,
                3,
                new BigDecimal("599.00")
        );

        assertEquals(
                new BigDecimal("1797.00"),
                item.getSubtotal()
        );
    }

    @Test
    void shouldCalculateSubtotalForMaximumQuantity() {
        OrderItem item = new OrderItem(
                order,
                book,
                99,
                new BigDecimal("599.00")
        );

        assertEquals(
                new BigDecimal("59301.00"),
                item.getSubtotal()
        );
    }

    @Test
    void shouldReturnZeroSubtotalWhenUnitPriceIsZero() {
        OrderItem item = new OrderItem(
                order,
                book,
                5,
                BigDecimal.ZERO
        );

        assertEquals(BigDecimal.ZERO, item.getSubtotal());
    }
}
