
package com.example.bookstore.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    private Book createBook() {
        return new Book(
                "Clean Code",
                "Robert C. Martin",
                new BigDecimal("599.00"),
                15,
                "A handbook of agile software craftsmanship"
        );
    }

    // Constructor tests

    @Test
    void shouldCreateBookWithValidDetails() {
        Book book = createBook();

        assertNull(book.getId());
        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert C. Martin", book.getAuthor());
        assertEquals(new BigDecimal("599.00"), book.getPrice());
        assertEquals(15, book.getStock());
        assertEquals(
                "A handbook of agile software craftsmanship",
                book.getDescription()
        );
        assertNull(book.getCreatedAt());
        assertNull(book.getUpdatedAt());
    }

    @Test
    void shouldTrimTitleAndAuthor() {
        Book book = new Book(
                "  Clean Code  ",
                "  Robert C. Martin  ",
                new BigDecimal("599.00"),
                15,
                " Description "
        );

        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert C. Martin", book.getAuthor());
        assertEquals("Description", book.getDescription());
    }

    @Test
    void shouldThrowExceptionWhenTitleIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new Book(
                        null,
                        "Robert C. Martin",
                        new BigDecimal("599.00"),
                        15,
                        null
                )
        );

        assertEquals("Title cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAuthorIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new Book(
                        "Clean Code",
                        null,
                        new BigDecimal("599.00"),
                        15,
                        null
                )
        );

        assertEquals("Author cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTitleIsEmpty() {
        assertEquals(
                "Title cannot be empty",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new Book(
                                "   ",
                                "Robert C. Martin",
                                new BigDecimal("599.00"),
                                15,
                                null
                        )
                ).getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenAuthorIsEmpty() {
        assertEquals(
                "Author cannot be empty",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new Book(
                                "Clean Code",
                                "   ",
                                new BigDecimal("599.00"),
                                15,
                                null
                        )
                ).getMessage()
        );
    }

    // Price validation tests

    @Test
    void shouldThrowExceptionWhenPriceIsNull() {
        assertEquals(
                "Price cannot be null",
                assertThrows(
                        NullPointerException.class,
                        () -> new Book(
                                "Clean Code",
                                "Robert C. Martin",
                                null,
                                15,
                                null
                        )
                ).getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenPriceIsNegative() {
        assertEquals(
                "Price cannot be negative",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new Book(
                                "Clean Code",
                                "Robert C. Martin",
                                new BigDecimal("-10.00"),
                                15,
                                null
                        )
                ).getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenPriceHasMoreThanTwoDecimalPlaces() {
        assertEquals(
                "Price cannot have more than 2 decimal places",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new Book(
                                "Clean Code",
                                "Robert C. Martin",
                                new BigDecimal("599.999"),
                                15,
                                null
                        )
                ).getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenPriceExceedsMaximumPrecision() {
        assertEquals(
                "Price exceeds the maximum supported value",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new Book(
                                "Clean Code",
                                "Robert C. Martin",
                                new BigDecimal("123456789.00"),
                                15,
                                null
                        )
                ).getMessage()
        );
    }

    @Test
    void shouldAllowZeroPrice() {
        Book book = new Book(
                "Clean Code",
                "Robert C. Martin",
                BigDecimal.ZERO,
                15,
                null
        );

        assertEquals(BigDecimal.ZERO, book.getPrice());
    }

    // Stock validation tests

    @Test
    void shouldThrowExceptionWhenStockIsNull() {
        assertEquals(
                "Stock cannot be null",
                assertThrows(
                        NullPointerException.class,
                        () -> new Book(
                                "Clean Code",
                                "Robert C. Martin",
                                new BigDecimal("599.00"),
                                null,
                                null
                        )
                ).getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenStockIsNegative() {
        assertEquals(
                "Stock cannot be negative",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new Book(
                                "Clean Code",
                                "Robert C. Martin",
                                new BigDecimal("599.00"),
                                -1,
                                null
                        )
                ).getMessage()
        );
    }

    @Test
    void shouldAllowZeroStock() {
        Book book = new Book(
                "Clean Code",
                "Robert C. Martin",
                new BigDecimal("599.00"),
                0,
                null
        );

        assertEquals(0, book.getStock());
    }

    // Description tests

    @Test
    void shouldAllowNullDescription() {
        Book book = new Book(
                "Clean Code",
                "Robert C. Martin",
                new BigDecimal("599.00"),
                15,
                null
        );

        assertNull(book.getDescription());
    }

    @Test
    void shouldConvertBlankDescriptionToNull() {
        Book book = new Book(
                "Clean Code",
                "Robert C. Martin",
                new BigDecimal("599.00"),
                15,
                "   "
        );

        assertNull(book.getDescription());
    }

    @Test
    void shouldThrowExceptionWhenDescriptionExceeds1000Characters() {
        String description = "A".repeat(1001);

        assertEquals(
                "Description cannot exceed 1000 characters",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new Book(
                                "Clean Code",
                                "Robert C. Martin",
                                new BigDecimal("599.00"),
                                15,
                                description
                        )
                ).getMessage()
        );
    }

    // Stock modification tests

    @Test
    void shouldDecreaseStock() {
        Book book = createBook();

        book.decreaseStock(5);

        assertEquals(10, book.getStock());
    }

    @Test
    void shouldThrowExceptionWhenDecreasingStockByZero() {
        Book book = createBook();

        assertEquals(
                "Quantity must be greater than zero",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> book.decreaseStock(0)
                ).getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenDecreasingStockByNegativeQuantity() {
        Book book = createBook();

        assertThrows(
                IllegalArgumentException.class,
                () -> book.decreaseStock(-2)
        );
    }

    @Test
    void shouldThrowExceptionWhenStockIsInsufficient() {
        Book book = createBook();

        assertEquals(
                "Insufficient stock",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> book.decreaseStock(20)
                ).getMessage()
        );

        assertEquals(15, book.getStock());
    }

    @Test
    void shouldIncreaseStock() {
        Book book = createBook();

        book.increaseStock(5);

        assertEquals(20, book.getStock());
    }

    @Test
    void shouldThrowExceptionWhenIncreasingStockByZero() {
        Book book = createBook();

        assertThrows(
                IllegalArgumentException.class,
                () -> book.increaseStock(0)
        );
    }

    @Test
    void shouldThrowExceptionWhenIncreasingStockByNegativeQuantity() {
        Book book = createBook();

        assertThrows(
                IllegalArgumentException.class,
                () -> book.increaseStock(-5)
        );
    }

    @Test
    void shouldThrowExceptionWhenStockIncreaseOverflows() {
        Book book = new Book(
                "Clean Code",
                "Robert C. Martin",
                new BigDecimal("599.00"),
                Integer.MAX_VALUE,
                null
        );

        assertThrows(
                ArithmeticException.class,
                () -> book.increaseStock(1)
        );
    }

    // Price update tests

    @Test
    void shouldUpdatePrice() {
        Book book = createBook();

        book.updatePrice(new BigDecimal("699.00"));

        assertEquals(new BigDecimal("699.00"), book.getPrice());
    }

    @Test
    void shouldNotUpdatePriceWhenNewPriceIsInvalid() {
        Book book = createBook();

        assertThrows(
                IllegalArgumentException.class,
                () -> book.updatePrice(new BigDecimal("-100.00"))
        );

        assertEquals(new BigDecimal("599.00"), book.getPrice());
    }

    // Details update tests

    @Test
    void shouldUpdateBookDetails() {
        Book book = createBook();

        book.updateDetails(
                "Effective Java",
                "Joshua Bloch",
                "Best practices for Java programming"
        );

        assertEquals("Effective Java", book.getTitle());
        assertEquals("Joshua Bloch", book.getAuthor());
        assertEquals(
                "Best practices for Java programming",
                book.getDescription()
        );
    }

    @Test
    void shouldTrimUpdatedBookDetails() {
        Book book = createBook();

        book.updateDetails(
                "  Effective Java  ",
                "  Joshua Bloch  ",
                "  Java best practices  "
        );

        assertEquals("Effective Java", book.getTitle());
        assertEquals("Joshua Bloch", book.getAuthor());
        assertEquals("Java best practices", book.getDescription());
    }

    @Test
    void shouldNotUpdateDetailsWhenTitleIsInvalid() {
        Book book = createBook();

        assertThrows(
                IllegalArgumentException.class,
                () -> book.updateDetails(
                        " ",
                        "Joshua Bloch",
                        "Updated description"
                )
        );

        assertEquals("Clean Code", book.getTitle());
    }

    // JPA lifecycle tests

    @Test
    void shouldSetCreatedAtAndUpdatedAtOnCreate() {
        Book book = createBook();

        book.onCreate();

        assertNotNull(book.getCreatedAt());
        assertNotNull(book.getUpdatedAt());
        assertEquals(book.getCreatedAt(), book.getUpdatedAt());
    }

    @Test
    void shouldUpdateUpdatedAtOnUpdate() throws InterruptedException {
        Book book = createBook();

        book.onCreate();

        Instant originalUpdatedAt = book.getUpdatedAt();

        Thread.sleep(2);

        book.onUpdate();

        assertNotNull(book.getUpdatedAt());
        assertTrue(book.getUpdatedAt().isAfter(originalUpdatedAt));
    }
}
