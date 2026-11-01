
package com.example.bookstore.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartItemTest {

    private Cart cart;
    private Book book;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        book = new Book(
                "Clean Code",
                "Robert C. Martin",
                new java.math.BigDecimal("599.00"),
                15,
                "A handbook of agile software craftsmanship"
        );
    }

    // Constructor tests

    @Test
    void shouldCreateCartItemWithValidDetails() {
        CartItem cartItem = new CartItem(cart, book, 2);

        assertNull(cartItem.getId());
        assertSame(cart, cartItem.getCart());
        assertSame(book, cartItem.getBook());
        assertEquals(2, cartItem.getQuantity());
    }

    @Test
    void shouldThrowExceptionWhenCartIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new CartItem(null, book, 1)
        );

        assertEquals("Cart cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenBookIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new CartItem(cart, null, 1)
        );

        assertEquals("Book cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenInitialQuantityIsZero() {
        assertEquals(
                "Quantity must be between 1 and 99",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new CartItem(cart, book, 0)
                ).getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenInitialQuantityIsNegative() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CartItem(cart, book, -1)
        );
    }

    @Test
    void shouldThrowExceptionWhenInitialQuantityExceeds99() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CartItem(cart, book, 100)
        );
    }

    @Test
    void shouldAllowMinimumQuantity() {
        CartItem cartItem = new CartItem(cart, book, 1);

        assertEquals(1, cartItem.getQuantity());
    }

    @Test
    void shouldAllowMaximumQuantity() {
        CartItem cartItem = new CartItem(cart, book, 99);

        assertEquals(99, cartItem.getQuantity());
    }

    // changeQuantity tests

    @Test
    void shouldChangeQuantity() {
        CartItem cartItem = new CartItem(cart, book, 2);

        cartItem.changeQuantity(5);

        assertEquals(5, cartItem.getQuantity());
    }

    @Test
    void shouldThrowExceptionWhenChangingQuantityToZero() {
        CartItem cartItem = new CartItem(cart, book, 2);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartItem.changeQuantity(0)
        );

        assertEquals(2, cartItem.getQuantity());
    }

    @Test
    void shouldThrowExceptionWhenChangingQuantityToNegative() {
        CartItem cartItem = new CartItem(cart, book, 2);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartItem.changeQuantity(-5)
        );

        assertEquals(2, cartItem.getQuantity());
    }

    @Test
    void shouldThrowExceptionWhenChangingQuantityAbove99() {
        CartItem cartItem = new CartItem(cart, book, 2);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartItem.changeQuantity(100)
        );

        assertEquals(2, cartItem.getQuantity());
    }

    // increaseQuantity tests

    @Test
    void shouldIncreaseQuantity() {
        CartItem cartItem = new CartItem(cart, book, 2);

        cartItem.increaseQuantity(3);

        assertEquals(5, cartItem.getQuantity());
    }

    @Test
    void shouldThrowExceptionWhenIncreasingByZero() {
        CartItem cartItem = new CartItem(cart, book, 2);

        assertEquals(
                "Amount must be greater than zero",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> cartItem.increaseQuantity(0)
                ).getMessage()
        );

        assertEquals(2, cartItem.getQuantity());
    }

    @Test
    void shouldThrowExceptionWhenIncreasingByNegativeAmount() {
        CartItem cartItem = new CartItem(cart, book, 2);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartItem.increaseQuantity(-1)
        );

        assertEquals(2, cartItem.getQuantity());
    }

    @Test
    void shouldThrowExceptionWhenIncreasingBeyond99() {
        CartItem cartItem = new CartItem(cart, book, 95);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartItem.increaseQuantity(5)
        );

        assertEquals(95, cartItem.getQuantity());
    }

    @Test
    void shouldAllowIncreasingQuantityUpTo99() {
        CartItem cartItem = new CartItem(cart, book, 95);

        cartItem.increaseQuantity(4);

        assertEquals(99, cartItem.getQuantity());
    }

    @Test
    void shouldThrowArithmeticExceptionWhenQuantityOverflows() {
        CartItem cartItem = new CartItem(cart, book, 1);

        assertThrows(
                ArithmeticException.class,
                () -> cartItem.increaseQuantity(Integer.MAX_VALUE)
        );

        assertEquals(1, cartItem.getQuantity());
    }

    // decreaseQuantity tests

    @Test
    void shouldDecreaseQuantity() {
        CartItem cartItem = new CartItem(cart, book, 5);

        cartItem.decreaseQuantity(2);

        assertEquals(3, cartItem.getQuantity());
    }

    @Test
    void shouldThrowExceptionWhenDecreasingByZero() {
        CartItem cartItem = new CartItem(cart, book, 5);

        assertEquals(
                "Amount must be greater than zero",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> cartItem.decreaseQuantity(0)
                ).getMessage()
        );

        assertEquals(5, cartItem.getQuantity());
    }

    @Test
    void shouldThrowExceptionWhenDecreasingByNegativeAmount() {
        CartItem cartItem = new CartItem(cart, book, 5);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartItem.decreaseQuantity(-2)
        );

        assertEquals(5, cartItem.getQuantity());
    }

    @Test
    void shouldThrowExceptionWhenDecreasingQuantityToZero() {
        CartItem cartItem = new CartItem(cart, book, 2);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartItem.decreaseQuantity(2)
        );

        assertEquals(2, cartItem.getQuantity());
    }

    @Test
    void shouldThrowExceptionWhenDecreasingBelowZero() {
        CartItem cartItem = new CartItem(cart, book, 2);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartItem.decreaseQuantity(3)
        );

        assertEquals(2, cartItem.getQuantity());
    }

    @Test
    void shouldAllowDecreasingQuantityToOne() {
        CartItem cartItem = new CartItem(cart, book, 5);

        cartItem.decreaseQuantity(4);

        assertEquals(1, cartItem.getQuantity());
    }
}
