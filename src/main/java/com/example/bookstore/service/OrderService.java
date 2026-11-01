
package com.example.bookstore.service;

import com.example.bookstore.dto.ApiDtos.OrderLine;
import com.example.bookstore.dto.ApiDtos.OrderResponse;
import com.example.bookstore.entity.AppUser;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Cart;
import com.example.bookstore.entity.CartItem;
import com.example.bookstore.entity.CustomerOrder;
import com.example.bookstore.entity.OrderItem;
import com.example.bookstore.exception.NotFoundException;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CartRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final UserRepository users;
    private final CartRepository carts;
    private final OrderRepository orders;
    private final BookRepository books;

    public OrderService(
            UserRepository users,
            CartRepository carts,
            OrderRepository orders,
            BookRepository books) {

        this.users = users;
        this.carts = carts;
        this.orders = orders;
        this.books = books;
    }

    @Transactional
    public OrderResponse checkout(String email) {

        // 1. Find the customer
        AppUser user = users.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException("User not found"));

        // 2. Find the customer's cart
        Cart cart = carts.findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Cart is empty"));

        List<CartItem> cartItems = new ArrayList<>(cart.getItems());

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // 3. Sort book IDs before locking to reduce deadlock risk
        cartItems.sort(
                Comparator.comparing(item -> item.getBook().getId())
        );

        // 4. Lock books and retrieve their latest database values
        Map<Long, Book> lockedBooks = new HashMap<>();

        for (CartItem cartItem : cartItems) {

            Long bookId = cartItem.getBook().getId();

            Book book = books.findByIdForUpdate(bookId)
                    .orElseThrow(() ->
                            new NotFoundException("Book not found: " + bookId));

            lockedBooks.put(bookId, book);
        }

        // 5. Validate stock before making any changes
        for (CartItem cartItem : cartItems) {

            Book book = lockedBooks.get(cartItem.getBook().getId());

            if (book.getStock() < cartItem.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for: " + book.getTitle()
                );
            }
        }

        // 6. Create order and calculate total from database prices
        CustomerOrder order = new CustomerOrder(user);

        for (CartItem cartItem : cartItems) {

            Book book = lockedBooks.get(cartItem.getBook().getId());

            // Preserve the current book price as the order item price
            BigDecimal unitPrice = book.getPrice();

            order.addItem(
                    book,
                    cartItem.getQuantity(),
                    unitPrice
            );

            // Decrease stock
            book.decreaseStock(cartItem.getQuantity());
        }

        // 7. Save order and clear cart
        CustomerOrder savedOrder = orders.save(order);

        cart.clear();

        // Cart is managed by the current persistence context.
        // No explicit carts.save(cart) is necessary.

        // 8. Convert saved order to response
        return map(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll(String email) {

        AppUser user = users.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException("User not found"));

        return orders.findAllByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(String email, Long id) {

        AppUser user = users.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException("User not found"));

        CustomerOrder order = orders.findByIdAndUserId(id, user.getId())
                .orElseThrow(() ->
                        new NotFoundException("Order not found"));

        return map(order);
    }

    private OrderResponse map(CustomerOrder order) {

        List<OrderLine> lines = order.getItems()
                .stream()
                .map(this::mapOrderItem)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getTotal(),
                order.getStatus(),
                order.getCreatedAt(),
                lines
        );
    }

    private OrderLine mapOrderItem(OrderItem item) {

        BigDecimal subtotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new OrderLine(
                item.getBook().getId(),
                item.getBook().getTitle(),
                item.getQuantity(),
                item.getUnitPrice(),
                subtotal
        );
    }
}
