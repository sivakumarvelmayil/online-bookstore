
package com.example.bookstore.service;

import com.example.bookstore.dto.ApiDtos.AuthResponse;
import com.example.bookstore.dto.ApiDtos.LoginRequest;
import com.example.bookstore.dto.ApiDtos.RegisterRequest;
import com.example.bookstore.entity.AppUser;
import com.example.bookstore.exception.EmailAlreadyRegisteredException;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Service
public class AuthService {

    private static final String EMAIL_ALREADY_REGISTERED =
            "Email is already registered";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(
                    EMAIL_ALREADY_REGISTERED
            );
        }

        AppUser user = new AppUser(
                request.name().trim(),
                email,
                passwordEncoder.encode(request.password())
        );

        AppUser savedUser = userRepository.save(user);

        return buildAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        String email = normalizeEmail(request.email());

        Authentication authentication =
                authenticationManager.authenticate(
                        UsernamePasswordAuthenticationToken.unauthenticated(
                                email,
                                request.password()
                        )
                );

        String authenticatedEmail = authentication.getName();

        AppUser user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found"
                        )
                );

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(AppUser user) {

        String token = jwtService.generate(user.getEmail());

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    private String normalizeEmail(String email) {

        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException(
                    "Email must not be empty"
            );
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }
}
