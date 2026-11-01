
package com.example.bookstore.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final JwtParser jwtParser;
    private final long expirationMs;
    private final String issuer;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:3600000}") long expirationMs,
            @Value("${app.jwt.issuer:online-bookstore}") String issuer) {

        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret cannot be empty");
        }

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain at least 32 bytes for HS256"
            );
        }

        if (expirationMs <= 0) {
            throw new IllegalArgumentException(
                    "JWT expiration must be greater than zero"
            );
        }

        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("JWT issuer cannot be empty");
        }

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
        this.issuer = issuer;

        // Build parser once and reuse it.
        this.jwtParser = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(issuer)
                .build();
    }

    /**
     * Generates a JWT for the given email.
     */
    public String generate(String email) {

        String normalizedEmail = normalizeEmail(email);

        Instant now = Instant.now();

        Date issuedAt = Date.from(now);
        Date expiration = Date.from(now.plusMillis(expirationMs));

        return Jwts.builder()
                .issuer(issuer)
                .subject(normalizedEmail)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extracts email from a verified JWT.
     * Signature, expiration and issuer are validated during parsing.
     */
    public String extractEmail(String token) {

        Claims claims = extractClaims(token);

        String email = claims.getSubject();

        if (email == null || email.isBlank()) {
            throw new JwtException("JWT subject is missing");
        }

        return email;
    }

    /**
     * Validates JWT against the expected email.
     */
    public boolean isValid(String token, String email) {

        try {
            String tokenEmail = extractEmail(token);

            return tokenEmail.equals(normalizeEmail(email));

        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Parses and verifies JWT claims.
     */
    private Claims extractClaims(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("JWT token cannot be empty");
        }

        return jwtParser
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Normalizes email consistently.
     */
    private String normalizeEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email cannot be empty"
            );
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }
}
