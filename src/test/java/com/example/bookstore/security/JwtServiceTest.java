
package com.example.bookstore.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET =
            "my-test-secret-key-must-be-at-least-32-bytes-long-123456";

    private static final String EMAIL = "john@gmail.com";

    private static final String ISSUER = "online-bookstore";

    private static final long EXPIRATION_MS = 3600000;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                SECRET,
                EXPIRATION_MS,
                ISSUER
        );
    }

    // 1. Generate JWT successfully
    @Test
    void generate_WithValidEmail_ShouldReturnJwtToken() {

        String token = jwtService.generate(EMAIL);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(3, token.split("\\.").length);
    }

    // 2. Generate JWT with normalized email
    @Test
    void generate_ShouldNormalizeEmail() {

        String token = jwtService.generate("  JOHN@GMAIL.COM  ");

        String extractedEmail = jwtService.extractEmail(token);

        assertEquals(EMAIL, extractedEmail);
    }

    // 3. Extract email from valid JWT
    @Test
    void extractEmail_WithValidToken_ShouldReturnEmail() {

        String token = jwtService.generate(EMAIL);

        String extractedEmail = jwtService.extractEmail(token);

        assertEquals(EMAIL, extractedEmail);
    }

    // 4. Validate token with matching email
    @Test
    void isValid_WithMatchingEmail_ShouldReturnTrue() {

        String token = jwtService.generate(EMAIL);

        boolean result = jwtService.isValid(token, EMAIL);

        assertTrue(result);
    }

    // 5. Validate token with uppercase email
    @Test
    void isValid_WithUppercaseEmail_ShouldReturnTrue() {

        String token = jwtService.generate(EMAIL);

        boolean result = jwtService.isValid(
                token,
                " JOHN@GMAIL.COM "
        );

        assertTrue(result);
    }

    // 6. Validate token with different email
    @Test
    void isValid_WithDifferentEmail_ShouldReturnFalse() {

        String token = jwtService.generate(EMAIL);

        boolean result = jwtService.isValid(
                token,
                "other@gmail.com"
        );

        assertFalse(result);
    }

    // 7. Validate token with null email
    @Test
    void isValid_WithNullEmail_ShouldReturnFalse() {

        String token = jwtService.generate(EMAIL);

        assertFalse(jwtService.isValid(token, null));
    }

    // 8. Validate token with empty email
    @Test
    void isValid_WithEmptyEmail_ShouldReturnFalse() {

        String token = jwtService.generate(EMAIL);

        assertFalse(jwtService.isValid(token, ""));
    }

    // 9. Extract email from null token
    @Test
    void extractEmail_WithNullToken_ShouldThrowException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.extractEmail(null)
        );
    }

    // 10. Extract email from empty token
    @Test
    void extractEmail_WithEmptyToken_ShouldThrowException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.extractEmail("")
        );
    }

    // 11. Extract email from malformed token
    @Test
    void extractEmail_WithMalformedToken_ShouldThrowException() {

        assertThrows(
                JwtException.class,
                () -> jwtService.extractEmail("invalid.jwt.token")
        );
    }

    // 12. Validate malformed token
    @Test
    void isValid_WithMalformedToken_ShouldReturnFalse() {

        assertFalse(
                jwtService.isValid("invalid.jwt.token", EMAIL)
        );
    }

    // 13. Validate token signed with a different secret
    @Test
    void extractEmail_WithDifferentSignature_ShouldThrowException() {

        String anotherSecret =
                "another-test-secret-key-must-be-at-least-32-bytes-long";

        JwtService anotherJwtService = new JwtService(
                anotherSecret,
                EXPIRATION_MS,
                ISSUER
        );

        String token = anotherJwtService.generate(EMAIL);

        assertThrows(
                JwtException.class,
                () -> jwtService.extractEmail(token)
        );
    }

    // 14. Validate token with different issuer
    @Test
    void extractEmail_WithDifferentIssuer_ShouldThrowException() {

        JwtService anotherJwtService = new JwtService(
                SECRET,
                EXPIRATION_MS,
                "different-issuer"
        );

        String token = anotherJwtService.generate(EMAIL);

        assertThrows(
                JwtException.class,
                () -> jwtService.extractEmail(token)
        );
    }

    // 15. Validate expired token
    @Test
    void extractEmail_WithExpiredToken_ShouldThrowException()
            throws InterruptedException {

        JwtService shortExpiryJwtService = new JwtService(
                SECRET,
                1,
                ISSUER
        );

        String token = shortExpiryJwtService.generate(EMAIL);

        Thread.sleep(50);

        assertThrows(
                JwtException.class,
                () -> shortExpiryJwtService.extractEmail(token)
        );
    }

    // 16. Validate expired token using isValid
    @Test
    void isValid_WithExpiredToken_ShouldReturnFalse()
            throws InterruptedException {

        JwtService shortExpiryJwtService = new JwtService(
                SECRET,
                1,
                ISSUER
        );

        String token = shortExpiryJwtService.generate(EMAIL);

        Thread.sleep(50);

        assertFalse(
                shortExpiryJwtService.isValid(token, EMAIL)
        );
    }

    // 17. Extract email when JWT subject is missing
    @Test
    void extractEmail_WithMissingSubject_ShouldThrowException() {

        SecretKey key = Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8)
        );

        Instant now = Instant.now();

        String token = Jwts.builder()
                .issuer(ISSUER)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(key)
                .compact();

        assertThrows(
                JwtException.class,
                () -> jwtService.extractEmail(token)
        );
    }

    // 18. Generate token with null email
    @Test
    void generate_WithNullEmail_ShouldThrowException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.generate(null)
        );
    }

    // 19. Generate token with empty email
    @Test
    void generate_WithEmptyEmail_ShouldThrowException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.generate("")
        );
    }

    // 20. Generate token with blank email
    @Test
    void generate_WithBlankEmail_ShouldThrowException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.generate("   ")
        );
    }

    // 21. Constructor with null secret
    @Test
    void constructor_WithNullSecret_ShouldThrowException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService(null, EXPIRATION_MS, ISSUER)
        );
    }

    // 22. Constructor with empty secret
    @Test
    void constructor_WithEmptySecret_ShouldThrowException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService("", EXPIRATION_MS, ISSUER)
        );
    }

    // 23. Constructor with short secret
    @Test
    void constructor_WithShortSecret_ShouldThrowException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService(
                        "short-secret",
                        EXPIRATION_MS,
                        ISSUER
                )
        );
    }

    // 24. Constructor with zero expiration
    @Test
    void constructor_WithZeroExpiration_ShouldThrowException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService(SECRET, 0, ISSUER)
        );
    }

    // 25. Constructor with negative expiration
    @Test
    void constructor_WithNegativeExpiration_ShouldThrowException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService(SECRET, -1000, ISSUER)
        );
    }

    // 26. Constructor with empty issuer
    @Test
    void constructor_WithEmptyIssuer_ShouldThrowException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService(SECRET, EXPIRATION_MS, "")
        );
    }

    // 27. Constructor with null issuer
    @Test
    void constructor_WithNullIssuer_ShouldThrowException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService(SECRET, EXPIRATION_MS, null)
        );
    }
}
