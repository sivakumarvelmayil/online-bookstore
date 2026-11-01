
package com.example.bookstore.security;

import com.example.bookstore.entity.AppUser;
import com.example.bookstore.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private AppUser appUser;

    private static final String EMAIL = "john@gmail.com";
    private static final String PASSWORD_HASH = "$2a$10$hashedPassword";

    // 1. Load user successfully
    @Test
    void loadUserByUsername_WithValidEmail_ShouldReturnUserDetails() {

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(appUser));
        when(appUser.getEmail()).thenReturn(EMAIL);
        when(appUser.getPasswordHash()).thenReturn(PASSWORD_HASH);

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(EMAIL);

        assertNotNull(userDetails);
        assertEquals(EMAIL, userDetails.getUsername());
        assertEquals(PASSWORD_HASH, userDetails.getPassword());

        verify(userRepository).findByEmail(EMAIL);
    }

    // 2. Verify ROLE_USER authority
    @Test
    void loadUserByUsername_ShouldAssignRoleUserAuthority() {

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(appUser));
        when(appUser.getEmail()).thenReturn(EMAIL);
        when(appUser.getPasswordHash()).thenReturn(PASSWORD_HASH);

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(EMAIL);

        assertEquals(1, userDetails.getAuthorities().size());

        assertTrue(userDetails.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_USER")
        ));

        verify(userRepository).findByEmail(EMAIL);
    }

    // 3. Verify email normalization
    @Test
    void loadUserByUsername_ShouldTrimAndLowercaseEmail() {

        String inputEmail = "  JOHN@GMAIL.COM  ";

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(appUser));
        when(appUser.getEmail()).thenReturn(EMAIL);
        when(appUser.getPasswordHash()).thenReturn(PASSWORD_HASH);

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(inputEmail);

        assertNotNull(userDetails);
        assertEquals(EMAIL, userDetails.getUsername());

        verify(userRepository).findByEmail(EMAIL);
    }

    // 4. User not found
    @Test
    void loadUserByUsername_WhenUserDoesNotExist_ShouldThrowException() {

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(EMAIL)
        );

        assertEquals(
                "User not found with email: " + EMAIL,
                exception.getMessage()
        );

        verify(userRepository).findByEmail(EMAIL);
    }

    // 5. Null email
    @Test
    void loadUserByUsername_WithNullEmail_ShouldThrowException() {

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(null)
        );

        assertEquals("Email cannot be empty", exception.getMessage());

        verifyNoInteractions(userRepository);
    }

    // 6. Empty email
    @Test
    void loadUserByUsername_WithEmptyEmail_ShouldThrowException() {

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("")
        );

        assertEquals("Email cannot be empty", exception.getMessage());

        verifyNoInteractions(userRepository);
    }

    // 7. Whitespace-only email
    @Test
    void loadUserByUsername_WithBlankEmail_ShouldThrowException() {

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("   ")
        );

        assertEquals("Email cannot be empty", exception.getMessage());

        verifyNoInteractions(userRepository);
    }

    // 8. Verify account status
    @Test
    void loadUserByUsername_ShouldReturnEnabledAndNonExpiredAccount() {

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(appUser));
        when(appUser.getEmail()).thenReturn(EMAIL);
        when(appUser.getPasswordHash()).thenReturn(PASSWORD_HASH);

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(EMAIL);

        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());

        verify(userRepository).findByEmail(EMAIL);
    }
}
