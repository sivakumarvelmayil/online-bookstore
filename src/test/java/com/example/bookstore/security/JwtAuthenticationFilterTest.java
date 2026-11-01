
package com.example.bookstore.security;

import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String EMAIL = "john@gmail.com";
    private static final String TOKEN = "valid-jwt-token";

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        userDetails = User.builder()
                .username(EMAIL)
                .password("encoded-password")
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_USER")
                ))
                .build();

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // 1. Valid JWT should authenticate the user
    @Test
    void doFilterInternal_WithValidToken_ShouldAuthenticateUser()
            throws ServletException, IOException {

        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + TOKEN
        );

        when(jwtService.extractEmail(TOKEN)).thenReturn(EMAIL);

        when(userDetailsService.loadUserByUsername(EMAIL))
                .thenReturn(userDetails);

        when(jwtService.isValid(TOKEN, EMAIL)).thenReturn(true);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertInstanceOf(UserDetails.class, authentication.getPrincipal());

        UserDetails authenticatedUser =
                (UserDetails) authentication.getPrincipal();

        assertEquals(EMAIL, authenticatedUser.getUsername());

        assertTrue(authentication.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_USER")
        ));

        verify(jwtService).extractEmail(TOKEN);
        verify(userDetailsService).loadUserByUsername(EMAIL);
        verify(jwtService).isValid(TOKEN, EMAIL);
        verify(filterChain, times(1))
                .doFilter(request, response);
    }

    // 2. Missing Authorization header
    @Test
    void doFilterInternal_WithoutAuthorizationHeader_ShouldNotAuthenticate()
            throws ServletException, IOException {

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verifyNoInteractions(jwtService, userDetailsService);

        verify(filterChain, times(1))
                .doFilter(request, response);
    }

    // 3. Authorization header without Bearer prefix
    @Test
    void doFilterInternal_WithoutBearerPrefix_ShouldNotAuthenticate()
            throws ServletException, IOException {

        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                TOKEN
        );

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verifyNoInteractions(jwtService, userDetailsService);

        verify(filterChain, times(1))
                .doFilter(request, response);
    }

    // 4. Empty Bearer token
    @Test
    void doFilterInternal_WithEmptyBearerToken_ShouldNotAuthenticate()
            throws ServletException, IOException {

        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer   "
        );

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verifyNoInteractions(jwtService, userDetailsService);

        verify(filterChain, times(1))
                .doFilter(request, response);
    }

    // 5. Invalid JWT
    @Test
    void doFilterInternal_WithInvalidJwt_ShouldClearSecurityContext()
            throws ServletException, IOException {

        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + TOKEN
        );

        when(jwtService.extractEmail(TOKEN))
                .thenThrow(new JwtException("Invalid JWT"));

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(jwtService).extractEmail(TOKEN);

        verifyNoInteractions(userDetailsService);

        verify(filterChain, times(1))
                .doFilter(request, response);
    }

    // 6. User not found
    @Test
    void doFilterInternal_WhenUserDoesNotExist_ShouldClearSecurityContext()
            throws ServletException, IOException {

        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + TOKEN
        );

        when(jwtService.extractEmail(TOKEN)).thenReturn(EMAIL);

        when(userDetailsService.loadUserByUsername(EMAIL))
                .thenThrow(new UsernameNotFoundException("User not found"));

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(jwtService).extractEmail(TOKEN);
        verify(userDetailsService).loadUserByUsername(EMAIL);

        verify(jwtService, never()).isValid(anyString(), anyString());

        verify(filterChain, times(1))
                .doFilter(request, response);
    }

    // 7. Token extracted but validation fails
    @Test
    void doFilterInternal_WithInvalidTokenValidation_ShouldNotAuthenticate()
            throws ServletException, IOException {

        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + TOKEN
        );

        when(jwtService.extractEmail(TOKEN)).thenReturn(EMAIL);

        when(userDetailsService.loadUserByUsername(EMAIL))
                .thenReturn(userDetails);

        when(jwtService.isValid(TOKEN, EMAIL)).thenReturn(false);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(jwtService).extractEmail(TOKEN);
        verify(userDetailsService).loadUserByUsername(EMAIL);
        verify(jwtService).isValid(TOKEN, EMAIL);

        verify(filterChain, times(1))
                .doFilter(request, response);
    }

    // 8. Existing authentication should not be overwritten
    @Test
    void doFilterInternal_WhenAuthenticationAlreadyExists_ShouldNotAuthenticateAgain()
            throws ServletException, IOException {

        Authentication existingAuthentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "existing-user",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        SecurityContextHolder.getContext()
                .setAuthentication(existingAuthentication);

        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + TOKEN
        );

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals("existing-user", authentication.getName());

        verifyNoInteractions(jwtService, userDetailsService);

        verify(filterChain, times(1))
                .doFilter(request, response);
    }

    // 9. IllegalArgumentException while extracting email
    @Test
    void doFilterInternal_WhenTokenIsMalformed_ShouldClearSecurityContext()
            throws ServletException, IOException {

        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + TOKEN
        );

        when(jwtService.extractEmail(TOKEN))
                .thenThrow(new IllegalArgumentException("Malformed token"));

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(filterChain, times(1))
                .doFilter(request, response);
    }

    // 10. Verify filter chain is executed exactly once
    @Test
    void doFilterInternal_ShouldExecuteFilterChainExactlyOnce()
            throws ServletException, IOException {

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain, times(1))
                .doFilter(request, response);
    }
}
