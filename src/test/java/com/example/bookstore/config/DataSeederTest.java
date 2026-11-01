
package com.example.bookstore.config;

import com.example.bookstore.entity.AppUser;
import com.example.bookstore.entity.Book;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataSeeder dataSeeder;

    private CommandLineRunner commandLineRunner;

    @BeforeEach
    void setUp() {
        commandLineRunner = dataSeeder.seedData(
                bookRepository,
                userRepository,
                passwordEncoder
        );
    }

    // Configuration annotation tests

    @Test
    void shouldHaveConfigurationAnnotation() {
        assertTrue(
                DataSeeder.class.isAnnotationPresent(Configuration.class)
        );
    }

    @Test
    void shouldRunOnlyInDevProfile() {
        Profile profile = DataSeeder.class.getAnnotation(Profile.class);

        assertNotNull(profile);
        assertArrayEquals(new String[]{"dev"}, profile.value());
    }

    @Test
    void shouldDeclareSeedDataAsSpringBean() throws Exception {
        assertTrue(
                DataSeeder.class
                        .getDeclaredMethod(
                                "seedData",
                                BookRepository.class,
                                UserRepository.class,
                                PasswordEncoder.class
                        )
                        .isAnnotationPresent(Bean.class)
        );
    }

    // Book seeding tests

    @Test
    void shouldSeedAll20BooksWhenBooksDoNotExist() throws Exception {

        when(bookRepository.existsByTitleAndAuthor(anyString(), anyString()))
                .thenReturn(false);

        when(userRepository.existsByEmail("demo@bookstore.local"))
                .thenReturn(true);

        commandLineRunner.run(new String[]{});

        verify(bookRepository, times(20))
                .existsByTitleAndAuthor(anyString(), anyString());

        verify(bookRepository, times(20))
                .save(any(Book.class));
    }

    @Test
    void shouldNotSeedBooksWhenTheyAlreadyExist() throws Exception {

        when(bookRepository.existsByTitleAndAuthor(anyString(), anyString()))
                .thenReturn(true);

        when(userRepository.existsByEmail("demo@bookstore.local"))
                .thenReturn(true);

        commandLineRunner.run(new String[]{});

        verify(bookRepository, times(20))
                .existsByTitleAndAuthor(anyString(), anyString());

        verify(bookRepository, never())
                .save(any(Book.class));
    }

    @Test
    void shouldSaveOnlyBooksThatDoNotExist() throws Exception {

        // First 10 books already exist; remaining 10 do not.
        when(bookRepository.existsByTitleAndAuthor(anyString(), anyString()))
                .thenReturn(
                        true, true, true, true, true,
                        true, true, true, true, true,
                        false, false, false, false, false,
                        false, false, false, false, false
                );

        when(userRepository.existsByEmail("demo@bookstore.local"))
                .thenReturn(true);

        commandLineRunner.run(new String[]{});

        verify(bookRepository, times(20))
                .existsByTitleAndAuthor(anyString(), anyString());

        verify(bookRepository, times(10))
                .save(any(Book.class));
    }

    @Test
    void shouldSaveExpectedBookDetails() throws Exception {

        when(bookRepository.existsByTitleAndAuthor(anyString(), anyString()))
                .thenReturn(false);

        when(userRepository.existsByEmail("demo@bookstore.local"))
                .thenReturn(true);

        commandLineRunner.run(new String[]{});

        ArgumentCaptor<Book> bookCaptor =
                ArgumentCaptor.forClass(Book.class);

        verify(bookRepository, times(20))
                .save(bookCaptor.capture());

        List<Book> savedBooks = bookCaptor.getAllValues();

        assertEquals(20, savedBooks.size());

        assertTrue(savedBooks.stream().anyMatch(book ->
                book.getTitle().equals("Clean Code")
                        && book.getAuthor().equals("Robert C. Martin")
        ));

        assertTrue(savedBooks.stream().anyMatch(book ->
                book.getTitle().equals("Effective Java")
                        && book.getAuthor().equals("Joshua Bloch")
        ));

        assertTrue(savedBooks.stream().anyMatch(book ->
                book.getTitle().equals("Kafka: The Definitive Guide")
                        && book.getAuthor().equals("Gwen Shapira and Others")
        ));
    }

    // Demo user seeding tests

    @Test
    void shouldCreateDemoUserWhenUserDoesNotExist() throws Exception {

        when(bookRepository.existsByTitleAndAuthor(anyString(), anyString()))
                .thenReturn(true);

        when(userRepository.existsByEmail("demo@bookstore.local"))
                .thenReturn(false);

        when(passwordEncoder.encode("Password123!"))
                .thenReturn("encodedPassword");

        commandLineRunner.run(new String[]{});

        verify(userRepository)
                .existsByEmail("demo@bookstore.local");

        verify(passwordEncoder)
                .encode("Password123!");

        ArgumentCaptor<AppUser> userCaptor =
                ArgumentCaptor.forClass(AppUser.class);

        verify(userRepository).save(userCaptor.capture());

        AppUser savedUser = userCaptor.getValue();

        assertEquals("Demo User", savedUser.getName());
        assertEquals("demo@bookstore.local", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPasswordHash());
    }

    @Test
    void shouldNotCreateDemoUserWhenUserAlreadyExists() throws Exception {

        when(bookRepository.existsByTitleAndAuthor(anyString(), anyString()))
                .thenReturn(true);

        when(userRepository.existsByEmail("demo@bookstore.local"))
                .thenReturn(true);

        commandLineRunner.run(new String[]{});

        verify(userRepository)
                .existsByEmail("demo@bookstore.local");

        verify(userRepository, never())
                .save(any(AppUser.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void shouldNotSavePlainTextPassword() throws Exception {

        when(bookRepository.existsByTitleAndAuthor(anyString(), anyString()))
                .thenReturn(true);

        when(userRepository.existsByEmail("demo@bookstore.local"))
                .thenReturn(false);

        when(passwordEncoder.encode("Password123!"))
                .thenReturn("bcryptEncodedPassword");

        commandLineRunner.run(new String[]{});

        ArgumentCaptor<AppUser> userCaptor =
                ArgumentCaptor.forClass(AppUser.class);

        verify(userRepository).save(userCaptor.capture());

        assertNotEquals(
                "Password123!",
                userCaptor.getValue().getPasswordHash()
        );

        assertEquals(
                "bcryptEncodedPassword",
                userCaptor.getValue().getPasswordHash()
        );
    }

    // Combined seeding test

    @Test
    void shouldSeedBooksAndDemoUserInSameExecution() throws Exception {

        when(bookRepository.existsByTitleAndAuthor(anyString(), anyString()))
                .thenReturn(false);

        when(userRepository.existsByEmail("demo@bookstore.local"))
                .thenReturn(false);

        when(passwordEncoder.encode("Password123!"))
                .thenReturn("encodedPassword");

        commandLineRunner.run(new String[]{});

        verify(bookRepository, times(20))
                .save(any(Book.class));

        verify(userRepository).save(any(AppUser.class));

        verify(passwordEncoder).encode("Password123!");
    }
}
