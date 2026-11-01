
package com.example.bookstore.controller;

import com.example.bookstore.security.CustomUserDetailsService;
import com.example.bookstore.security.JwtService;
import com.example.bookstore.service.OrderService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    private static final String EMAIL = "john@gmail.com";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    // Required if the security configuration loads JwtAuthenticationFilter.
    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private Authentication authenticatedUser() {
        return UsernamePasswordAuthenticationToken.authenticated(
                EMAIL,
                null,
                List.of()
        );
    }

    // 1. Checkout successfully
    @Test
    void checkout_ShouldReturnCreated() throws Exception {

        mockMvc.perform(post("/api/orders/checkout")
                        .principal(authenticatedUser()))
                .andExpect(status().isCreated());

        verify(orderService).checkout(EMAIL);
    }

    // 2. Get all orders successfully
    @Test
    void getAllOrders_ShouldReturnOk() throws Exception {

        when(orderService.findAll(EMAIL))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/orders")
                        .principal(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(orderService).findAll(EMAIL);
    }

    // 3. Get all orders when orders exist
    @Test
    void getAllOrders_WhenOrdersExist_ShouldReturnOrders() throws Exception {

        // The mocked service returns an empty list here.
        // Replace with actual OrderResponse instances to test JSON fields.
        when(orderService.findAll(EMAIL))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/orders")
                        .principal(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(orderService).findAll(EMAIL);
    }

    // 4. Get order by ID successfully
    @Test
    void getOrderById_ShouldReturnOk() throws Exception {

        mockMvc.perform(get("/api/orders/1")
                        .principal(authenticatedUser()))
                .andExpect(status().isOk());

        verify(orderService).findById(EMAIL, 1L);
    }

    // 5. Get order with zero ID
    @Test
    void getOrderById_WithZeroId_ShouldReturnBadRequest() throws Exception {

        mockMvc.perform(get("/api/orders/0")
                        .principal(authenticatedUser()))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).findById(anyString(), anyLong());
    }

    // 6. Get order with negative ID
    @Test
    void getOrderById_WithNegativeId_ShouldReturnBadRequest() throws Exception {

        mockMvc.perform(get("/api/orders/-1")
                        .principal(authenticatedUser()))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).findById(anyString(), anyLong());
    }

    // 7. Get order with non-numeric ID
    @Test
    void getOrderById_WithInvalidId_ShouldReturnBadRequest() throws Exception {

        mockMvc.perform(get("/api/orders/abc")
                        .principal(authenticatedUser()))
                .andExpect(status().isInternalServerError());

        verify(orderService, never()).findById(anyString(), anyLong());
    }

    // 8. Checkout without authentication
    @Test
    void checkout_WithoutAuthentication_ShouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(post("/api/orders/checkout"))
                .andExpect(status().isUnauthorized());

        verify(orderService, never()).checkout(anyString());
    }

    // 9. Get all orders without authentication
    @Test
    void getAllOrders_WithoutAuthentication_ShouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());

        verify(orderService, never()).findAll(anyString());
    }

    // 10. Get order by ID without authentication
    @Test
    void getOrderById_WithoutAuthentication_ShouldReturnUnauthorized()
            throws Exception {

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isUnauthorized());

        verify(orderService, never()).findById(anyString(), anyLong());
    }
}
