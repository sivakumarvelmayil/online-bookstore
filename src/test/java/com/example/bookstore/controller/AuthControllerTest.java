package com.example.bookstore.controller;

import com.example.bookstore.dto.ApiDtos.AuthResponse;
import com.example.bookstore.dto.ApiDtos.LoginRequest;
import com.example.bookstore.dto.ApiDtos.RegisterRequest;
import com.example.bookstore.service.AuthService;
import com.example.bookstore.security.JwtService;
import com.example.bookstore.security.CustomUserDetailsService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void register_ShouldReturnCreated() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "John",
                "john@gmail.com",
                "password123"
        );

        AuthResponse response = new AuthResponse(
                "jwt-token",
                1L,
                "John",
                "john@gmail.com"
        );

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void login_ShouldReturnOk() throws Exception {

        LoginRequest request = new LoginRequest(
                "john@gmail.com",
                "password123"
        );

        AuthResponse response = new AuthResponse(
                "jwt-token",
                1L,
                "John",
                "john@gmail.com"
        );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authService).login(any(LoginRequest.class));
    }
}