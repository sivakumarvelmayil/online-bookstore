
package com.example.bookstore.repository;

import com.example.bookstore.entity.Book;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    // Search books by title (case-insensitive)
    Page<Book> findByTitleContainingIgnoreCase(
            String title,
            Pageable pageable
    );

    // Search books by author (case-insensitive)
    Page<Book> findByAuthorContainingIgnoreCase(
            String author,
            Pageable pageable
    );

    // Search by title or author
    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
            String title,
            String author,
            Pageable pageable
    );

    // Find books within a price range
    Page<Book> findByPriceBetween(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    );

    // Find books that are currently in stock
    Page<Book> findByStockGreaterThan(
            Integer stock,
            Pageable pageable
    );

    boolean existsByTitleAndAuthor(
            String title,
            String author
    );

    // Find a book with pessimistic write lock during checkout
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);
}
