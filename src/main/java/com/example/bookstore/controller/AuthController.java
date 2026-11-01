
package com.example.bookstore.controller;

import com.example.bookstore.dto.ApiDtos.AuthResponse;
import com.example.bookstore.dto.ApiDtos.LoginRequest;
import com.example.bookstore.dto.ApiDtos.RegisterRequest;
import com.example.bookstore.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Authentication",
        description = "User registration and authentication APIs"
)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new bookstore user.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns an authentication token"
    )
    public AuthResponse register(
            @Valid @RequestBody RegisterRequest request) {

        return authService.register(request);
    }

    /**
     * Authenticates an existing bookstore user.
     */
    @PostMapping("/login")
    @Operation(
            summary = "Login user",
            description = "Authenticates a user using email and password"
    )
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }
}
