
package com.example.bookstore.service;

import com.example.bookstore.dto.ApiDtos.AuthResponse;
import com.example.bookstore.dto.ApiDtos.LoginRequest;
import com.example.bookstore.dto.ApiDtos.RegisterRequest;
import com.example.bookstore.entity.AppUser;
import com.example.bookstore.exception.EmailAlreadyRegisteredException;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.JwtService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldCreateUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest(
                "Test User",
                "TEST@BOOKSTORE.COM",
                "Password123!"
        );

        when(userRepository.existsByEmail("test@bookstore.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("Password123!"))
                .thenReturn("hashed-password");
        when(jwtService.generate("test@bookstore.com"))
                .thenReturn("jwt-token");

        when(userRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> {
                    AppUser user = invocation.getArgument(0);
                    return user;
                });

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals("Test User", response.name());
        assertEquals("test@bookstore.com", response.email());

        ArgumentCaptor<AppUser> captor =
                ArgumentCaptor.forClass(AppUser.class);

        verify(userRepository).save(captor.capture());

        AppUser savedUser = captor.getValue();
        assertEquals("Test User", savedUser.getName());
        assertEquals("test@bookstore.com", savedUser.getEmail());
        assertEquals("hashed-password", savedUser.getPasswordHash());

        verify(passwordEncoder).encode("Password123!");
        verify(jwtService).generate("test@bookstore.com");
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "Test User",
                "test@bookstore.com",
                "Password123!"
        );

        when(userRepository.existsByEmail("test@bookstore.com"))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> authService.register(request)
        );

        verify(userRepository, never()).save(any(AppUser.class));
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void loginShouldAuthenticateAndReturnToken() {
        LoginRequest request = new LoginRequest(
                "TEST@BOOKSTORE.COM",
                "Password123!"
        );

        AppUser user = new AppUser(
                "Test User",
                "test@bookstore.com",
                "hashed-password"
        );

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("test@bookstore.com");

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenReturn(authentication);

        when(userRepository.findByEmail("test@bookstore.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generate("test@bookstore.com"))
                .thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals("Test User", response.name());
        assertEquals("test@bookstore.com", response.email());

        verify(authenticationManager).authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ));
        verify(userRepository).findByEmail("test@bookstore.com");
        verify(jwtService).generate("test@bookstore.com");
    }

    @Test
    void loginShouldRejectInvalidCredentials() {
        LoginRequest request = new LoginRequest(
                "test@bookstore.com",
                "WrongPassword"
        );

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        verify(userRepository, never()).findByEmail(anyString());
        verifyNoInteractions(jwtService);
    }

    @Test
    void loginShouldFailWhenAuthenticatedUserCannotBeFound() {
        LoginRequest request = new LoginRequest(
                "test@bookstore.com",
                "Password123!"
        );

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("test@bookstore.com");

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenReturn(authentication);

        when(userRepository.findByEmail("test@bookstore.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> authService.login(request)
        );

        verify(userRepository).findByEmail("test@bookstore.com");
        verifyNoInteractions(jwtService);
    }
}
