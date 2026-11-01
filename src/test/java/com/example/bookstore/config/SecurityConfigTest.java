
package com.example.bookstore.config;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private AuthenticationManager authenticationManager;

    // Configuration bean tests

    @Test
    void shouldLoadSecurityFilterChainBean() {
        assertNotNull(securityFilterChain);
    }

    @Test
    void shouldLoadAuthenticationManagerBean() {
        assertNotNull(authenticationManager);
    }

    @Test
    void shouldLoadPasswordEncoderBean() {
        assertNotNull(passwordEncoder);
        assertInstanceOf(BCryptPasswordEncoder.class, passwordEncoder);
    }

    // Password encoder tests

    @Test
    void shouldEncodePasswordUsingBCrypt() {
        String rawPassword = "Password123!";

        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void shouldGenerateDifferentHashesForSamePassword() {
        String rawPassword = "Password123!";

        String firstHash = passwordEncoder.encode(rawPassword);
        String secondHash = passwordEncoder.encode(rawPassword);

        assertNotEquals(firstHash, secondHash);

        assertTrue(passwordEncoder.matches(rawPassword, firstHash));
        assertTrue(passwordEncoder.matches(rawPassword, secondHash));
    }

    @Test
    void shouldRejectIncorrectPassword() {
        String encodedPassword =
                passwordEncoder.encode("Password123!");

        assertFalse(
                passwordEncoder.matches("WrongPassword", encodedPassword)
        );
    }

    // Public endpoint tests

    @Test
    void shouldAllowUnauthenticatedAccessToBooksEndpoint()
            throws Exception {

        mockMvc.perform(get("/api/books"))
                .andExpect(result ->
                        assertNotEquals(401, result.getResponse().getStatus())
                );
    }

    @Test
    void shouldAllowUnauthenticatedAccessToAuthenticationEndpoint()
            throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(result ->
                        assertNotEquals(401, result.getResponse().getStatus())
                );
    }

    // Protected endpoint tests

    @Test
    void shouldReturn401ForUnauthenticatedRequest()
            throws Exception {

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = "USER")
    void shouldAllowAuthenticatedAccessToProtectedEndpoint()
            throws Exception {

        mockMvc.perform(get("/api/cart"))
                .andExpect(result ->
                        assertNotEquals(401, result.getResponse().getStatus())
                );
    }

    // OPTIONS request tests

    @Test
    void shouldAllowOptionsRequestWithoutAuthentication()
            throws Exception {

        mockMvc.perform(options("/api/cart"))
                .andExpect(result ->
                        assertNotEquals(401, result.getResponse().getStatus())
                );
    }

    // CORS tests

    @Test
    void shouldAllowConfiguredFrontendOrigin()
            throws Exception {

        mockMvc.perform(options("/api/books")
                        .header(
                                HttpHeaders.ORIGIN,
                                "http://localhost:3000"
                        )
                        .header(
                                HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                                "GET"
                        ))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:3000"
                ));
    }

    @Test
    void shouldAllowConfiguredCorsMethods()
            throws Exception {

        mockMvc.perform(options("/api/books")
                        .header(
                                HttpHeaders.ORIGIN,
                                "http://localhost:3000"
                        )
                        .header(
                                HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                                "POST"
                        ))
                .andExpect(status().isOk())
                .andExpect(header().exists(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS
                ));
    }

    @Test
    void shouldAllowAuthorizationHeaderInCorsRequest()
            throws Exception {

        mockMvc.perform(options("/api/cart")
                        .header(
                                HttpHeaders.ORIGIN,
                                "http://localhost:3000"
                        )
                        .header(
                                HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                                "GET"
                        )
                        .header(
                                HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                                "Authorization, Content-Type"
                        ))
                .andExpect(status().isOk())
                .andExpect(header().exists(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS
                ));
    }

    @Test
    void shouldNotAllowUnconfiguredCorsOrigin()
            throws Exception {

        mockMvc.perform(options("/api/books")
                        .header(
                                HttpHeaders.ORIGIN,
                                "http://malicious-site.com"
                        )
                        .header(
                                HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                                "GET"
                        ))
                .andExpect(header().doesNotExist(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN
                ));
    }

    // Configuration bean existence tests

    @Test
    void shouldRegisterCorsConfigurationSourceBean() {
        assertTrue(
                applicationContext.containsBean("corsConfigurationSource")
        );
    }

    @Test
    void shouldRegisterAuthenticationManagerBean() {
        assertTrue(
                applicationContext.containsBean("authenticationManager")
        );
    }

    @Test
    void shouldRegisterPasswordEncoderBean() {
        assertTrue(
                applicationContext.containsBean("passwordEncoder")
        );
    }
}
