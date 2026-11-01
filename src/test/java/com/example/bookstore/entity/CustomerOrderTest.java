
package com.example.bookstore.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerOrderTest {

    private AppUser user;
    private Book book;
    private CustomerOrder customerOrder;

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

        customerOrder = new CustomerOrder(user);
    }

    // Constructor tests

    @Test
    void shouldCreateOrderWithValidUser() {
        assertNull(customerOrder.getId());
        assertSame(user, customerOrder.getUser());
        assertEquals(BigDecimal.ZERO, customerOrder.getTotal());
        assertEquals(OrderStatus.PLACED, customerOrder.getStatus());
        assertNull(customerOrder.getCreatedAt());
        assertTrue(customerOrder.getItems().isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenUserIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new CustomerOrder(null)
        );

        assertEquals("User cannot be null", exception.getMessage());
    }

    // addItem tests

    @Test
    void shouldAddItemToOrder() {
        customerOrder.addItem(
                book,
                2,
                new BigDecimal("599.00")
        );

        assertEquals(1, customerOrder.getItems().size());
        assertEquals(
                new BigDecimal("1198.00"),
                customerOrder.getTotal()
        );
    }

    @Test
    void shouldCalculateTotalForMultipleItems() {
        Book secondBook = new Book(
                "Effective Java",
                "Joshua Bloch",
                new BigDecimal("699.00"),
                10,
                "Java best practices"
        );

        customerOrder.addItem(
                book,
                2,
                new BigDecimal("599.00")
        );

        customerOrder.addItem(
                secondBook,
                3,
                new BigDecimal("699.00")
        );

        // (599 * 2) + (699 * 3) = 3295
        assertEquals(
                new BigDecimal("3295.00"),
                customerOrder.getTotal()
        );

        assertEquals(2, customerOrder.getItems().size());
    }

    @Test
    void shouldAddItemWithQuantityOne() {
        customerOrder.addItem(
                book,
                1,
                new BigDecimal("599.00")
        );

        assertEquals(
                new BigDecimal("599.00"),
                customerOrder.getTotal()
        );

        assertEquals(1, customerOrder.getItems().size());
    }

    @Test
    void shouldAllowMaximumQuantityOf99() {
        customerOrder.addItem(
                book,
                99,
                new BigDecimal("599.00")
        );

        assertEquals(1, customerOrder.getItems().size());

        assertEquals(
                new BigDecimal("59301.00"),
                customerOrder.getTotal()
        );
    }

    @Test
    void shouldThrowExceptionWhenBookIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> customerOrder.addItem(
                        null,
                        1,
                        new BigDecimal("599.00")
                )
        );

        assertEquals("Book cannot be null", exception.getMessage());

        assertTrue(customerOrder.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, customerOrder.getTotal());
    }

    @Test
    void shouldThrowExceptionWhenUnitPriceIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> customerOrder.addItem(book, 1, null)
        );

        assertEquals(
                "Unit price cannot be null",
                exception.getMessage()
        );

        assertTrue(customerOrder.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, customerOrder.getTotal());
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsZero() {
        assertEquals(
                "Quantity must be between 1 and 99",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> customerOrder.addItem(
                                book,
                                0,
                                new BigDecimal("599.00")
                        )
                ).getMessage()
        );

        assertTrue(customerOrder.getItems().isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsNegative() {
        assertThrows(
                IllegalArgumentException.class,
                () -> customerOrder.addItem(
                        book,
                        -1,
                        new BigDecimal("599.00")
                )
        );

        assertTrue(customerOrder.getItems().isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenQuantityExceeds99() {
        assertThrows(
                IllegalArgumentException.class,
                () -> customerOrder.addItem(
                        book,
                        100,
                        new BigDecimal("599.00")
                )
        );

        assertTrue(customerOrder.getItems().isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenUnitPriceIsNegative() {
        assertEquals(
                "Unit price must be non-negative with at most 2 decimal places",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> customerOrder.addItem(
                                book,
                                1,
                                new BigDecimal("-10.00")
                        )
                ).getMessage()
        );

        assertTrue(customerOrder.getItems().isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenUnitPriceHasMoreThanTwoDecimals() {
        assertThrows(
                IllegalArgumentException.class,
                () -> customerOrder.addItem(
                        book,
                        1,
                        new BigDecimal("599.999")
                )
        );

        assertTrue(customerOrder.getItems().isEmpty());
    }

    @Test
    void shouldAllowZeroUnitPrice() {
        customerOrder.addItem(
                book,
                2,
                BigDecimal.ZERO
        );

        assertEquals(BigDecimal.ZERO, customerOrder.getTotal());
        assertEquals(1, customerOrder.getItems().size());
    }

    @Test
    void shouldNotChangeExistingTotalWhenAddingInvalidItem() {
        customerOrder.addItem(
                book,
                2,
                new BigDecimal("599.00")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> customerOrder.addItem(
                        book,
                        0,
                        new BigDecimal("100.00")
                )
        );

        assertEquals(
                new BigDecimal("1198.00"),
                customerOrder.getTotal()
        );

        assertEquals(1, customerOrder.getItems().size());
    }

    // Collection tests

    @Test
    void shouldReturnUnmodifiableItemsList() {
        customerOrder.addItem(
                book,
                1,
                new BigDecimal("599.00")
        );

        List<OrderItem> items = customerOrder.getItems();

        assertThrows(
                UnsupportedOperationException.class,
                () -> items.clear()
        );

        assertEquals(1, customerOrder.getItems().size());
    }

    @Test
    void shouldReturnEmptyItemsListForNewOrder() {
        assertNotNull(customerOrder.getItems());
        assertTrue(customerOrder.getItems().isEmpty());
    }

    // Lifecycle tests

    @Test
    void shouldSetCreatedAtOnCreate() {
        customerOrder.onCreate();

        assertNotNull(customerOrder.getCreatedAt());
    }

    @Test
    void shouldKeepTotalAndStatusOnCreate() {
        customerOrder.addItem(
                book,
                2,
                new BigDecimal("599.00")
        );

        customerOrder.onCreate();

        assertEquals(
                new BigDecimal("1198.00"),
                customerOrder.getTotal()
        );

        assertEquals(OrderStatus.PLACED, customerOrder.getStatus());
    }

    @Test
    void shouldNotOverwriteExistingCreatedAt() throws Exception {
        Instant existingTime = Instant.parse(
                "2026-01-01T10:00:00Z"
        );

        var field = CustomerOrder.class.getDeclaredField("createdAt");
        field.setAccessible(true);
        field.set(customerOrder, existingTime);

        customerOrder.onCreate();

        assertEquals(existingTime, customerOrder.getCreatedAt());
    }
}
