
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
                ),
                new Book(
                        "Java: The Complete Reference",
                        "Herbert Schildt",
                        new BigDecimal("799.00"),
                        10,
                        "Comprehensive guide to Java programming."
                ),
                new Book(
                        "Spring in Action",
                        "Craig Walls",
                        new BigDecimal("749.00"),
                        14,
                        "Learn Spring Framework and Spring Boot development."
                ),
                new Book(
                        "Head First Design Patterns",
                        "Eric Freeman and Elisabeth Robson",
                        new BigDecimal("699.00"),
                        9,
                        "A beginner-friendly guide to object-oriented design patterns."
                ),
                new Book(
                        "Clean Architecture",
                        "Robert C. Martin",
                        new BigDecimal("849.00"),
                        11,
                        "A guide to designing maintainable and scalable software systems."
                ),
                new Book(
                        "Refactoring",
                        "Martin Fowler",
                        new BigDecimal("899.00"),
                        7,
                        "Improving the design of existing code."
                ),
                new Book(
                        "Domain-Driven Design",
                        "Eric Evans",
                        new BigDecimal("999.00"),
                        6,
                        "Tackling complexity in software through domain-driven design."
                ),
                new Book(
                        "Microservices Patterns",
                        "Chris Richardson",
                        new BigDecimal("899.00"),
                        13,
                        "Practical patterns for building microservices-based applications."
                ),
                new Book(
                        "Building Microservices",
                        "Sam Newman",
                        new BigDecimal("949.00"),
                        8,
                        "Designing fine-grained systems using microservices architecture."
                ),
                new Book(
                        "Database System Concepts",
                        "Abraham Silberschatz",
                        new BigDecimal("1099.00"),
                        5,
                        "Fundamentals of database management systems."
                ),
                new Book(
                        "Designing Distributed Systems",
                        "Brendan Burns",
                        new BigDecimal("799.00"),
                        10,
                        "Patterns and practices for distributed system design."
                ),
                new Book(
                        "The Linux Command Line",
                        "William Shotts",
                        new BigDecimal("599.00"),
                        16,
                        "Learn Linux command-line tools and shell scripting."
                ),
                new Book(
                        "Grokking Algorithms",
                        "Aditya Bhargava",
                        new BigDecimal("549.00"),
                        18,
                        "An illustrated introduction to algorithms and data structures."
                ),
                new Book(
                        "Cracking the Coding Interview",
                        "Gayle Laakmann McDowell",
                        new BigDecimal("899.00"),
                        12,
                        "Programming interview questions and problem-solving techniques."
                ),
                new Book(
                        "System Design Interview",
                        "Alex Xu",
                        new BigDecimal("799.00"),
                        9,
                        "An insider's guide to scalable system design interviews."
                ),
                new Book(
                        "Kubernetes Up and Running",
                        "Brendan Burns, Joe Beda and Kelsey Hightower",
                        new BigDecimal("999.00"),
                        7,
                        "Learn Kubernetes concepts and container orchestration."
                ),
                new Book(
                        "Kafka: The Definitive Guide",
                        "Gwen Shapira and Others",
                        new BigDecimal("899.00"),
                        8,
                        "A practical guide to Apache Kafka and event streaming."
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
