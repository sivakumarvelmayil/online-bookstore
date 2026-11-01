
package com.example.bookstore.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.test.util.ReflectionTestUtils;

class CartTest {

    private AppUser user;
    private Book book;
    private Book anotherBook;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = new AppUser(
                "Test User",
                "test@example.com",
                "hashed-password"
        );

        book = new Book(
                "Java",
                "Author",
                new BigDecimal("100.00"),
                5,
                "Java programming"
        );

        anotherBook = new Book(
                "Spring Boot",
                "Another Author",
                new BigDecimal("200.00"),
                10,
                "Spring Boot development"
        );

        ReflectionTestUtils.setField(book, "id", 1L);
        ReflectionTestUtils.setField(anotherBook, "id", 2L);

        cart = new Cart(user);
    }

    // --------------------------------------------------
    // ADD ITEMS
    // --------------------------------------------------

    @Test
    void addShouldAddNewBookToCart() {

        cart.add(book, 2);

        assertEquals(1, cart.getItems().size());

        CartItem item = cart.getItems().get(0);

        assertEquals(book, item.getBook());
        assertEquals(2, item.getQuantity());
    }

    @Test
    void addingSameBookIncrementsExistingLine() {

        cart.add(book, 1);
        cart.add(book, 2);

        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().get(0).getQuantity());
    }

    @Test
    void addingDifferentBooksCreatesSeparateLines() {

        cart.add(book, 1);
        cart.add(anotherBook, 2);

        assertEquals(2, cart.getItems().size());

        assertEquals(1, cart.getItems().get(0).getQuantity());
        assertEquals(2, cart.getItems().get(1).getQuantity());
    }

    @Test
    void addingSameBookDoesNotCreateDuplicateLines() {

        cart.add(book, 2);
        cart.add(book, 3);
        cart.add(book, 1);

        assertEquals(1, cart.getItems().size());
        assertEquals(6, cart.getItems().get(0).getQuantity());
    }

    @Test
    void addShouldRejectZeroQuantity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> cart.add(book, 0)
        );

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void addShouldRejectNegativeQuantity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> cart.add(book, -1)
        );

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void addShouldRejectQuantityGreaterThanMaximum() {

        assertThrows(
                IllegalArgumentException.class,
                () -> cart.add(book, 100)
        );

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void addShouldRejectNullBook() {

        assertThrows(
                NullPointerException.class,
                () -> cart.add(null, 1)
        );

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void addShouldRejectQuantityExceedingMaximumAfterIncrement() {

        cart.add(book, 60);

        assertThrows(
                IllegalArgumentException.class,
                () -> cart.add(book, 40)
        );

        assertEquals(1, cart.getItems().size());
        assertEquals(60, cart.getItems().get(0).getQuantity());
    }

    // --------------------------------------------------
    // UPDATE QUANTITY
    // --------------------------------------------------

    @Test
    void setQuantityShouldUpdateExistingCartItem() {

        cart.add(book, 2);

        cart.setQuantity(book.getId(), 4);

        assertEquals(4, cart.getItems().get(0).getQuantity());
    }

    @Test
    void setQuantityShouldRejectZero() {

        cart.add(book, 2);

        assertThrows(
                IllegalArgumentException.class,
                () -> cart.setQuantity(book.getId(), 0)
        );

        assertEquals(2, cart.getItems().get(0).getQuantity());
    }

    @Test
    void setQuantityShouldRejectNegativeQuantity() {

        cart.add(book, 2);

        assertThrows(
                IllegalArgumentException.class,
                () -> cart.setQuantity(book.getId(), -1)
        );

        assertEquals(2, cart.getItems().get(0).getQuantity());
    }

    @Test
    void setQuantityShouldRejectQuantityGreaterThanMaximum() {

        cart.add(book, 2);

        assertThrows(
                IllegalArgumentException.class,
                () -> cart.setQuantity(book.getId(), 100)
        );

        assertEquals(2, cart.getItems().get(0).getQuantity());
    }

    // --------------------------------------------------
    // REMOVE ITEMS
    // --------------------------------------------------

    @Test
    void removeShouldRemoveExistingBook() {

        cart.add(book, 2);
        cart.add(anotherBook, 1);

        cart.remove(book.getId());

        assertEquals(1, cart.getItems().size());
        assertEquals(anotherBook, cart.getItems().get(0).getBook());
    }

    @Test
    void removeShouldNotAffectOtherItems() {

        cart.add(book, 2);
        cart.add(anotherBook, 3);

        cart.remove(book.getId());

        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().get(0).getQuantity());
    }

    // --------------------------------------------------
    // CLEAR CART
    // --------------------------------------------------

    @Test
    void clearShouldRemoveAllItems() {

        cart.add(book, 2);
        cart.add(anotherBook, 3);

        cart.clear();

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void clearShouldWorkWhenCartIsAlreadyEmpty() {

        assertDoesNotThrow(() -> cart.clear());

        assertTrue(cart.getItems().isEmpty());
    }

    // --------------------------------------------------
    // CART INITIALIZATION
    // --------------------------------------------------

    @Test
    void constructorShouldAssociateUserWithCart() {

        assertEquals(user, cart.getUser());
        assertNotNull(cart.getItems());
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void addingItemsShouldPreserveCartOwner() {

        cart.add(book, 2);

        assertEquals(user, cart.getUser());
        assertEquals(1, cart.getItems().size());
    }
}
