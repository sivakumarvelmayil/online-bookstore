
package com.example.bookstore.controller;

import com.example.bookstore.dto.ApiDtos.AddCartItemRequest;
import com.example.bookstore.dto.ApiDtos.CartResponse;
import com.example.bookstore.dto.ApiDtos.UpdateCartItemRequest;
import com.example.bookstore.service.CartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Shopping Cart", description = "APIs for managing the user's shopping cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(
            summary = "Get shopping cart",
            description = "Retrieves all items currently present in the authenticated user's cart"
    )
    public CartResponse getCart(Authentication authentication) {
        return cartService.get(getAuthenticatedEmail(authentication));
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Add item to cart",
            description = "Adds a book to the authenticated user's cart"
    )
    public CartResponse addItem(
            Authentication authentication,
            @Valid @RequestBody AddCartItemRequest request) {

        return cartService.add(
                getAuthenticatedEmail(authentication),
                request.bookId(),
                request.quantity()
        );
    }

    @PutMapping("/items/{bookId}")
    @Operation(
            summary = "Update cart item",
            description = "Updates the quantity of an existing book in the cart"
    )
    public CartResponse updateItem(
            Authentication authentication,
            @Parameter(description = "Unique ID of the book")
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        validateBookId(bookId);

        return cartService.update(
                getAuthenticatedEmail(authentication),
                bookId,
                request.quantity()
        );
    }

    @DeleteMapping("/items/{bookId}")
    @Operation(
            summary = "Remove cart item",
            description = "Removes a specific book from the authenticated user's cart"
    )
    public CartResponse removeItem(
            Authentication authentication,
            @Parameter(description = "Unique ID of the book")
            @PathVariable Long bookId) {

        validateBookId(bookId);

        return cartService.remove(
                getAuthenticatedEmail(authentication),
                bookId
        );
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Clear shopping cart",
            description = "Removes all items from the authenticated user's cart"
    )
    public void clearCart(Authentication authentication) {
        cartService.clear(getAuthenticatedEmail(authentication));
    }

    private String getAuthenticatedEmail(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User authentication is required"
            );
        }

        return authentication.getName();
    }

    private void validateBookId(Long bookId) {
        if (bookId == null || bookId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Book ID must be greater than zero"
            );
        }
    }
}
