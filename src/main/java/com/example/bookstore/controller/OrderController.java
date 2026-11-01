
package com.example.bookstore.controller;

import com.example.bookstore.dto.ApiDtos.OrderResponse;
import com.example.bookstore.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "APIs for checkout and order management")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Checkout cart",
            description = "Creates an order using the authenticated user's shopping cart"
    )
    public OrderResponse checkout(Authentication authentication) {

        return orderService.checkout(
                getAuthenticatedEmail(authentication)
        );
    }

    @GetMapping
    @Operation(
            summary = "Get all orders",
            description = "Retrieves all orders belonging to the authenticated user"
    )
    public List<OrderResponse> getAllOrders(Authentication authentication) {

        return orderService.findAll(
                getAuthenticatedEmail(authentication)
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get order by ID",
            description = "Retrieves a specific order belonging to the authenticated user"
    )
    public OrderResponse getOrderById(
            Authentication authentication,
            @Parameter(description = "Unique ID of the order")
            @PathVariable Long id) {

        validateOrderId(id);

        return orderService.findById(
                getAuthenticatedEmail(authentication),
                id
        );
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

    private void validateOrderId(Long id) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order ID must be greater than zero"
            );
        }
    }
}
