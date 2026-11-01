package com.example.bookstore.controller;

import com.example.bookstore.security.CustomUserDetailsService;
import com.example.bookstore.security.JwtService;
import com.example.bookstore.service.CartService;

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

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    private static final String EMAIL = "john@gmail.com";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    // Required because the security configuration may instantiate this filter.
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

    // 1. Get cart successfully
    @Test
    void getCart_ShouldReturnOk() throws Exception {

        mockMvc.perform(get("/api/cart")
                        .principal(authenticatedUser()))
                .andExpect(status().isOk());

        verify(cartService).get(EMAIL);
    }

    // 2. Add item successfully
    @Test
    void addItem_ShouldReturnCreated() throws Exception {

        String request = """
                {
                    "bookId": 1,
                    "quantity": 2
                }
                """;

        mockMvc.perform(post("/api/cart/items")
                        .principal(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        verify(cartService).add(EMAIL, 1L, 2);
    }

    // 3. Update cart item successfully
    @Test
    void updateItem_ShouldReturnOk() throws Exception {

        String request = """
                {
                    "quantity": 3
                }
                """;

        mockMvc.perform(put("/api/cart/items/1")
                        .principal(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        verify(cartService).update(EMAIL, 1L, 3);
    }

    // 4. Remove cart item successfully
    @Test
    void removeItem_ShouldReturnOk() throws Exception {

        mockMvc.perform(delete("/api/cart/items/1")
                        .principal(authenticatedUser()))
                .andExpect(status().isOk());

        verify(cartService).remove(EMAIL, 1L);
    }

    // 5. Clear cart successfully
    @Test
    void clearCart_ShouldReturnNoContent() throws Exception {

        mockMvc.perform(delete("/api/cart")
                        .principal(authenticatedUser()))
                .andExpect(status().isNoContent());

        verify(cartService).clear(EMAIL);
    }

    // 6. Update item with zero book ID
    @Test
    void updateItem_WithZeroBookId_ShouldReturnBadRequest() throws Exception {

        String request = """
                {
                    "quantity": 2
                }
                """;

        mockMvc.perform(put("/api/cart/items/0")
                        .principal(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).update(anyString(), anyLong(), anyInt());
    }

    // 7. Update item with negative book ID
    @Test
    void updateItem_WithNegativeBookId_ShouldReturnBadRequest() throws Exception {

        String request = """
                {
                    "quantity": 2
                }
                """;

        mockMvc.perform(put("/api/cart/items/-1")
                        .principal(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).update(anyString(), anyLong(), anyInt());
    }

    // 8. Remove item with zero book ID
    @Test
    void removeItem_WithZeroBookId_ShouldReturnBadRequest() throws Exception {

        mockMvc.perform(delete("/api/cart/items/0")
                        .principal(authenticatedUser()))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).remove(anyString(), anyLong());
    }

    // 9. Remove item with negative book ID
    @Test
    void removeItem_WithNegativeBookId_ShouldReturnBadRequest() throws Exception {

        mockMvc.perform(delete("/api/cart/items/-1")
                        .principal(authenticatedUser()))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).remove(anyString(), anyLong());
    }

    // 10. Get cart without authentication
    @Test
    void getCart_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).get(anyString());
    }

    // 11. Add item without authentication
    @Test
    void addItem_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {

        String request = """
                {
                    "bookId": 1,
                    "quantity": 2
                }
                """;

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).add(anyString(), anyLong(), anyInt());
    }

    // 12. Update item without authentication
    @Test
    void updateItem_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {

        String request = """
                {
                    "quantity": 2
                }
                """;

        mockMvc.perform(put("/api/cart/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).update(anyString(), anyLong(), anyInt());
    }

    // 13. Remove item without authentication
    @Test
    void removeItem_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {

        mockMvc.perform(delete("/api/cart/items/1"))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).remove(anyString(), anyLong());
    }

    // 14. Clear cart without authentication
    @Test
    void clearCart_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {

        mockMvc.perform(delete("/api/cart"))
                .andExpect(status().isUnauthorized());

        verify(cartService, never()).clear(anyString());
    }
}