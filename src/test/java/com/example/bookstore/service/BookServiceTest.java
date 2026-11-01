
package com.example.bookstore.service;

import com.example.bookstore.dto.ApiDtos.BookResponse;
import com.example.bookstore.entity.Book;
import com.example.bookstore.exception.NotFoundException;
import com.example.bookstore.repository.BookRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void findAllShouldReturnBooksAsDtos() {
        Book book = new Book(
                "Clean Code",
                "Robert Martin",
                new BigDecimal("599.00"),
                3,
                "Good book"
        );

        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(book)));

        Page<BookResponse> result = bookService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        BookResponse response = result.getContent().get(0);
        assertEquals("Clean Code", response.title());
        assertEquals("Robert Martin", response.author());
        assertEquals(new BigDecimal("599.00"), response.price());
        assertEquals(3, response.stock());
        assertEquals("Good book", response.description());

        verify(bookRepository).findAll(pageable);
    }

    @Test
    void findAllShouldReturnEmptyPageWhenNoBooksExist() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findAll(pageable))
                .thenReturn(Page.empty(pageable));

        Page<BookResponse> result = bookService.findAll(pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(bookRepository).findAll(pageable);
    }

    @Test
    void findByIdShouldReturnBookAsDto() {
        Book book = new Book(
                "Effective Java",
                "Joshua Bloch",
                new BigDecimal("699.00"),
                5,
                "Java best practices"
        );
        ReflectionTestUtils.setField(book, "id", 1L);

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        BookResponse result = bookService.findById(1L);

        assertEquals(1L, result.id());
        assertEquals("Effective Java", result.title());
        assertEquals("Joshua Bloch", result.author());
        assertEquals(new BigDecimal("699.00"), result.price());
        assertEquals(5, result.stock());

        verify(bookRepository).findById(1L);
    }

    @Test
    void findByIdShouldThrowNotFoundWhenBookDoesNotExist() {
        when(bookRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> bookService.findById(100L)
        );

        verify(bookRepository).findById(100L);
    }

    @Test
    void findByIdShouldRejectNullId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> bookService.findById(null)
        );

        verifyNoInteractions(bookRepository);
    }

    @Test
    void findByIdShouldRejectZeroOrNegativeId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> bookService.findById(0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> bookService.findById(-1L)
        );

        verifyNoInteractions(bookRepository);
    }

    @Test
    void findAllShouldRejectNullPageable() {
        assertThrows(
                IllegalArgumentException.class,
                () -> bookService.findAll(null)
        );

        verifyNoInteractions(bookRepository);
    }
}
