
package com.example.bookstore.service;

import com.example.bookstore.dto.ApiDtos.BookResponse;
import com.example.bookstore.entity.Book;
import com.example.bookstore.exception.NotFoundException;
import com.example.bookstore.repository.BookRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Retrieves all books using pagination and sorting.
     */
    public Page<BookResponse> findAll(Pageable pageable) {

        if (pageable == null) {
            throw new IllegalArgumentException(
                    "Pagination information cannot be null"
            );
        }

        return bookRepository.findAll(pageable)
                .map(this::mapToBookResponse);
    }

    /**
     * Retrieves a book using its unique ID.
     */
    public BookResponse findById(Long id) {

        validateBookId(id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Book not found with ID: " + id)
                );

        return mapToBookResponse(book);
    }

    /**
     * Converts Book entity into BookResponse DTO.
     */
    private BookResponse mapToBookResponse(Book book) {

        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPrice(),
                book.getStock(),
                book.getDescription()
        );
    }

    /**
     * Validates the book ID.
     */
    private void validateBookId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Book ID must be greater than zero"
            );
        }
    }
}
