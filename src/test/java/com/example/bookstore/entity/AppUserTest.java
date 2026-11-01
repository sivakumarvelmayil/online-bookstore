
package com.example.bookstore.entity;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class AppUserTest {

    @Test
    void shouldCreateUserWithValidDetails() {
        AppUser user = new AppUser(
                "John Doe",
                "john@example.com",
                "hashedPassword123"
        );

        assertNull(user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("hashedPassword123", user.getPasswordHash());
        assertNull(user.getCreatedAt());
    }

    @Test
    void shouldTrimNameDuringConstruction() {
        AppUser user = new AppUser(
                "  John Doe  ",
                "john@example.com",
                "hashedPassword123"
        );

        assertEquals("John Doe", user.getName());
    }

    @Test
    void shouldNormalizeEmailToLowercaseAndTrimSpaces() {
        AppUser user = new AppUser(
                "John Doe",
                "  JOHN@EXAMPLE.COM  ",
                "hashedPassword123"
        );

        assertEquals("john@example.com", user.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new AppUser(
                        null,
                        "john@example.com",
                        "hashedPassword123"
                )
        );

        assertEquals("Name cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new AppUser(
                        "John Doe",
                        null,
                        "hashedPassword123"
                )
        );

        assertEquals("Email cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPasswordHashIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new AppUser(
                        "John Doe",
                        "john@example.com",
                        null
                )
        );

        assertEquals(
                "Password hash cannot be null",
                exception.getMessage()
        );
    }

    @Test
    void shouldSetCreatedAtAndNormalizeEmailOnPrePersist() {
        AppUser user = new AppUser(
                "John Doe",
                "john@example.com",
                "hashedPassword123"
        );

        user.onCreate();

        assertNotNull(user.getCreatedAt());
        assertEquals("john@example.com", user.getEmail());
    }

    @Test
    void shouldNotOverwriteExistingCreatedAtOnPrePersist() {
        AppUser user = new AppUser(
                "John Doe",
                "john@example.com",
                "hashedPassword123"
        );

        Instant existingTime = Instant.parse("2026-01-01T10:00:00Z");

        // Set createdAt using reflection because there is no public setter.
        try {
            var field = AppUser.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(user, existingTime);
        } catch (ReflectiveOperationException exception) {
            fail("Unable to set createdAt for test", exception);
        }

        user.onCreate();

        assertEquals(existingTime, user.getCreatedAt());
    }
}
