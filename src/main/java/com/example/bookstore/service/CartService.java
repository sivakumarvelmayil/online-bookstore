
package com.example.bookstore.service;

import com.example.bookstore.dto.ApiDtos.CartLine;
import com.example.bookstore.dto.ApiDtos.CartResponse;
import com.example.bookstore.entity.AppUser;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Cart;
import com.example.bookstore.entity.CartItem;
import com.example.bookstore.exception.NotFoundException;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CartRepository;
import com.example.bookstore.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class CartService {

    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 99;

    private final CartRepository carts;
    private final UserRepository users;
    private final BookRepository books;

    public CartService(
            CartRepository carts,
            UserRepository users,
            BookRepository books) {
        this.carts = carts;
        this.users = users;
        this.books = books;
    }

    private Cart getOrCreate(String email) {
        validateEmail(email);

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        AppUser user = users.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new NotFoundException("User not found"));

        return carts.findByUserId(user.getId())
                .orElseGet(() -> carts.save(new Cart(user)));
    }

    @Transactional
    public CartResponse get(String email) {
        Cart cart = getOrCreate(email);
        return map(cart);
    }

    @Transactional
    public CartResponse add(String email, Long bookId, int quantity) {
        validateBookId(bookId);
        validateQuantity(quantity);

        Cart cart = getOrCreate(email);

        Book book = books.findById(bookId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Book not found: " + bookId));

        validateStock(book, quantity);

        cart.add(book, quantity);

        Cart savedCart = carts.save(cart);

        return map(savedCart);
    }

    @Transactional
    public CartResponse update(String email, Long bookId, int quantity) {
        validateBookId(bookId);
        validateQuantity(quantity);

        Cart cart = getOrCreate(email);

        CartItem currentItem = cart.getItems()
                .stream()
                .filter(item ->
                        item.getBook().getId().equals(bookId))
                .findFirst()
                .orElseThrow(() ->
                        new NotFoundException("Cart item not found"));

        Book book = currentItem.getBook();

        validateStock(book, quantity);

        cart.setQuantity(bookId, quantity);

        Cart savedCart = carts.save(cart);

        return map(savedCart);
    }

    @Transactional
    public CartResponse remove(String email, Long bookId) {
        validateBookId(bookId);

        Cart cart = getOrCreate(email);

        cart.remove(bookId);

        return map(carts.save(cart));
    }

    @Transactional
    public void clear(String email) {
        Cart cart = getOrCreate(email);

        cart.clear();

        carts.save(cart);
    }

    private CartResponse map(Cart cart) {

        List<CartLine> lines = cart.getItems()
                .stream()
                .map(item -> {
                    Book book = item.getBook();

                    BigDecimal lineTotal = book.getPrice()
                            .multiply(BigDecimal.valueOf(
                                    item.getQuantity()));

                    return new CartLine(
                            book.getId(),
                            book.getTitle(),
                            book.getAuthor(),
                            book.getPrice(),
                            item.getQuantity(),
                            lineTotal
                    );
                })
                .toList();

        BigDecimal total = lines.stream()
                .map(CartLine::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = lines.stream()
                .mapToInt(CartLine::quantity)
                .sum();

        return new CartResponse(lines, total, totalItems);
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email cannot be empty");
        }
    }

    private void validateBookId(Long bookId) {
        if (bookId == null || bookId <= 0) {
            throw new IllegalArgumentException(
                    "Book ID must be greater than zero");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException(
                    "Quantity must be between 1 and 99");
        }
    }

    private void validateStock(Book book, int quantity) {
        if (book.getStock() < quantity) {
            throw new IllegalArgumentException(
                    "Requested quantity exceeds available stock");
        }
    }
}
