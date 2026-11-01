
package com.example.bookstore.config;

import com.example.bookstore.entity.AppUser;
import com.example.bookstore.entity.Book;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@Profile("dev")
public class DataSeeder {

    private static final String DEMO_EMAIL = "demo@bookstore.local";
    private static final String DEMO_PASSWORD = "Password123!";

    @Bean
    CommandLineRunner seedData(
            BookRepository bookRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            seedBooks(bookRepository);

            seedDemoUser(userRepository, passwordEncoder);
        };
    }

    private void seedBooks(BookRepository bookRepository) {

        List<Book> sampleBooks = List.of(
                new Book(
                        "Clean Code",
                        "Robert C. Martin",
                        new BigDecimal("599.00"),
                        15,
                        "A handbook of agile software craftsmanship."
                ),
                new Book(
                        "Effective Java",
                        "Joshua Bloch",
                        new BigDecimal("699.00"),
                        12,
                        "Best practices for the Java platform."
                ),
                new Book(
                        "Designing Data-Intensive Applications",
                        "Martin Kleppmann",
                        new BigDecimal("899.00"),
                        8,
                        "Foundations of reliable, scalable data systems."
                ),
                new Book(
                        "The Pragmatic Programmer",
                        "David Thomas and Andrew Hunt",
                        new BigDecimal("649.00"),
                        20,
                        "Your journey to mastery."
                )
        );

        for (Book book : sampleBooks) {

            boolean exists = bookRepository
                    .existsByTitleAndAuthor(
                            book.getTitle(),
                            book.getAuthor()
                    );

            if (!exists) {
                bookRepository.save(book);
            }
        }
    }

    private void seedDemoUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        if (!userRepository.existsByEmail(DEMO_EMAIL)) {

            AppUser demoUser = new AppUser(
                    "Demo User",
                    DEMO_EMAIL,
                    passwordEncoder.encode(DEMO_PASSWORD)
            );

            userRepository.save(demoUser);
        }
    }
}
