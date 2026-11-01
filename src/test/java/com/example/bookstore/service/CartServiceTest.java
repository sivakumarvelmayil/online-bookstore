
package com.example.bookstore.service;

import com.example.bookstore.dto.ApiDtos.CartResponse;
import com.example.bookstore.entity.AppUser;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Cart;
import com.example.bookstore.entity.CartItem;
import com.example.bookstore.exception.NotFoundException;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CartRepository;
import com.example.bookstore.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository carts;

    @Mock
    private UserRepository users;

    @Mock
    private BookRepository books;

    @InjectMocks
    private CartService cartService;

    private AppUser user;
    private Cart cart;
    private CartItem cartItem;
    private Book book;

    private static final String EMAIL = "test@bookstore.com";
    private static final Long USER_ID = 1L;
    private static final Long BOOK_ID = 10L;

    @BeforeEach
    void setUp() {
        user = new AppUser(
                "Test User",
                EMAIL,
                "hashed-password"
        );

        ReflectionTestUtils.setField(user, "id", USER_ID);

        cart = mock(Cart.class);
        cartItem = mock(CartItem.class);
        book = mock(Book.class);
    }

    private void mockUserAndCart() {

        when(users.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(carts.findByUserId(USER_ID))
                .thenReturn(Optional.of(cart));
    }

    private void mockCartItem() {

        when(cart.getItems())
                .thenReturn(List.of(cartItem));

        when(cartItem.getBook())
                .thenReturn(book);

        when(book.getId())
                .thenReturn(BOOK_ID);
    }

    private void mockCartResponseData() {

        when(book.getTitle())
                .thenReturn("Clean Code");

        when(book.getAuthor())
                .thenReturn("Robert Martin");

        when(book.getPrice())
                .thenReturn(new BigDecimal("599.00"));

        when(cartItem.getQuantity())
                .thenReturn(2);
    }

    // ------------------------------------------------
    // GET CART TESTS
    // ------------------------------------------------

    @Test
    void getShouldReturnCartResponse() {
        mockCartItem();
        mockCartResponseData();
        mockUserAndCart();

        CartResponse response = cartService.get(EMAIL);

        assertNotNull(response);
        assertEquals(1, response.items().size());

        assertEquals(
                "Clean Code",
                response.items().get(0).title()
        );

        assertEquals(
                "Robert Martin",
                response.items().get(0).author()
        );

        assertEquals(
                new BigDecimal("599.00"),
                response.items().get(0).unitPrice()
        );

        assertEquals(2, response.items().get(0).quantity());

        assertEquals(
                new BigDecimal("1198.00"),
                response.total()
        );

        assertEquals(2, response.totalItems());

        verify(carts).findByUserId(USER_ID);
    }

    @Test
    void getShouldReturnEmptyCart() {
        when(users.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(carts.findByUserId(USER_ID))
                .thenReturn(Optional.of(cart));

        when(cart.getItems())
                .thenReturn(List.of());

        CartResponse response = cartService.get(EMAIL);

        assertNotNull(response);
        assertTrue(response.items().isEmpty());
        assertEquals(BigDecimal.ZERO, response.total());
        assertEquals(0, response.totalItems());
    }

    @Test
    void getShouldCreateCartWhenCartDoesNotExist() {
        Cart newCart = mock(Cart.class);

        when(users.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(carts.findByUserId(USER_ID))
                .thenReturn(Optional.empty());

        when(carts.save(any(Cart.class)))
                .thenReturn(newCart);

        when(newCart.getItems())
                .thenReturn(List.of());

        CartResponse response = cartService.get(EMAIL);

        assertNotNull(response);
        assertTrue(response.items().isEmpty());

        verify(carts).save(any(Cart.class));
    }

    @Test
    void getShouldThrowWhenUserDoesNotExist() {
        when(users.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> cartService.get(EMAIL)
        );

        verifyNoInteractions(carts, books);
    }

    // ------------------------------------------------
    // ADD CART ITEM TESTS
    // ------------------------------------------------

    @Test
    void addShouldAddBookToCart() {
        when(users.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(carts.findByUserId(USER_ID))
                .thenReturn(Optional.of(cart));

        when(books.findById(BOOK_ID))
                .thenReturn(Optional.of(book));

        when(book.getStock()).thenReturn(10);

        when(carts.save(cart))
                .thenReturn(cart);

        when(cart.getItems())
                .thenReturn(List.of(cartItem));

        when(cartItem.getBook()).thenReturn(book);
        when(book.getId()).thenReturn(BOOK_ID);
        when(book.getTitle()).thenReturn("Clean Code");
        when(book.getAuthor()).thenReturn("Robert Martin");
        when(book.getPrice()).thenReturn(new BigDecimal("599.00"));
        when(cartItem.getQuantity()).thenReturn(2);

        CartResponse response =
                cartService.add(EMAIL, BOOK_ID, 2);

        verify(cart).add(book, 2);
        verify(carts).save(cart);

        assertNotNull(response);
        assertEquals(1, response.items().size());
    }

    @Test
    void addShouldThrowWhenBookDoesNotExist() {
        when(users.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(carts.findByUserId(USER_ID))
                .thenReturn(Optional.of(cart));

        when(books.findById(BOOK_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> cartService.add(EMAIL, BOOK_ID, 2)
        );

        verify(cart, never()).add(any(Book.class), anyInt());
        verify(carts, never()).save(any(Cart.class));
    }

    @Test
    void addShouldRejectQuantityGreaterThanStock() {
        when(users.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(carts.findByUserId(USER_ID))
                .thenReturn(Optional.of(cart));

        when(books.findById(BOOK_ID))
                .thenReturn(Optional.of(book));

        when(book.getStock()).thenReturn(1);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.add(EMAIL, BOOK_ID, 2)
        );

        verify(cart, never()).add(any(Book.class), anyInt());
        verify(carts, never()).save(any(Cart.class));
    }

    @Test
    void addShouldRejectZeroQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.add(EMAIL, BOOK_ID, 0)
        );

        verifyNoInteractions(users, carts, books);
    }

    @Test
    void addShouldRejectInvalidBookId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.add(EMAIL, 0L, 2)
        );

        verifyNoInteractions(users, carts, books);
    }

    // ------------------------------------------------
    // UPDATE CART ITEM TESTS
    // ------------------------------------------------

    @Test
    void updateShouldUpdateCartItemQuantity() {
        mockUserAndCart();
        mockCartItem();
        mockCartResponseData();

        // The requested quantity must not exceed available stock.
        when(book.getStock()).thenReturn(10);

        // Mock response mapping after the quantity update.
        when(cartItem.getQuantity()).thenReturn(3);

        when(carts.save(cart)).thenReturn(cart);

        CartResponse response = cartService.update(EMAIL, BOOK_ID, 3);

        verify(cart).setQuantity(BOOK_ID, 3);
        verify(carts).save(cart);

        assertEquals(1, response.items().size());
        assertEquals(3, response.items().get(0).quantity());
        assertEquals(new BigDecimal("1797.00"), response.total());
        assertEquals(3, response.totalItems());
    }

    @Test
    void updateShouldThrowWhenCartItemDoesNotExist() {
        when(users.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(carts.findByUserId(USER_ID))
                .thenReturn(Optional.of(cart));

        when(cart.getItems())
                .thenReturn(List.of());

        assertThrows(
                NotFoundException.class,
                () -> cartService.update(EMAIL, BOOK_ID, 3)
        );

        verify(carts, never()).save(any(Cart.class));
    }

    @Test
    void updateShouldRejectQuantityGreaterThanStock() {
        mockUserAndCart();
        mockCartItem();

        when(book.getStock()).thenReturn(2);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.update(EMAIL, BOOK_ID, 3)
        );

        verify(cart, never()).setQuantity(anyLong(), anyInt());
        verify(carts, never()).save(any(Cart.class));
    }

    @Test
    void updateShouldRejectInvalidQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.update(EMAIL, BOOK_ID, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.update(EMAIL, BOOK_ID, 100)
        );

        verifyNoInteractions(users, carts, books);
    }

    // ------------------------------------------------
    // REMOVE CART ITEM TESTS
    // ------------------------------------------------

    @Test
    void removeShouldRemoveBookFromCart() {
        mockUserAndCart();

        when(carts.save(cart)).thenReturn(cart);

        CartResponse response =
                cartService.remove(EMAIL, BOOK_ID);

        verify(cart).remove(BOOK_ID);
        verify(carts).save(cart);

        assertNotNull(response);
    }

    @Test
    void removeShouldRejectInvalidBookId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.remove(EMAIL, 0L)
        );

        verifyNoInteractions(users, carts, books);
    }

    @Test
    void removeShouldThrowWhenUserDoesNotExist() {
        when(users.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> cartService.remove(EMAIL, BOOK_ID)
        );

        verify(carts, never()).save(any(Cart.class));
    }

    // ------------------------------------------------
    // CLEAR CART TESTS
    // ------------------------------------------------

    @Test
    void clearShouldRemoveAllCartItems() {
        when(users.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        when(carts.findByUserId(USER_ID))
                .thenReturn(Optional.of(cart));

        cartService.clear(EMAIL);

        verify(cart).clear();
        verify(carts).save(cart);
    }

    @Test
    void clearShouldThrowWhenUserDoesNotExist() {
        when(users.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> cartService.clear(EMAIL)
        );

        verify(carts, never()).save(any(Cart.class));
    }

    // ------------------------------------------------
    // COMMON VALIDATION TESTS
    // ------------------------------------------------

    @Test
    void allOperationsShouldRejectBlankEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.get(" ")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.add(" ", BOOK_ID, 2)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.update(" ", BOOK_ID, 2)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.remove(" ", BOOK_ID)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.clear(" ")
        );

        verifyNoInteractions(users, carts, books);
    }
}
