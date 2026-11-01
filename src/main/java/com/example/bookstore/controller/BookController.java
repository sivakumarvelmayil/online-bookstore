
package com.example.bookstore.controller;

import com.example.bookstore.dto.ApiDtos.BookResponse;
import com.example.bookstore.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "APIs for browsing and retrieving books")
public class BookController {

    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("id", "title", "author", "price", "stock");

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // --------------------------------------------------
    // GET ALL BOOKS
    // --------------------------------------------------

    @GetMapping
    @Operation(
            summary = "Get all books",
            description = "Returns paginated and sortable book records"
    )
    public Page<BookResponse> findAll(

            @Parameter(description = "Page number, starting from 0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size, maximum 100")
            @RequestParam(defaultValue = "12") int size,

            HttpServletRequest request) {

        validatePagination(page, size);

        // Read raw parameter values to preserve commas.
        String[] sortParameters = request.getParameterValues("sort");

        Sort sorting = buildSort(sortParameters);

        Pageable pageable = PageRequest.of(page, size, sorting);

        return bookService.findAll(pageable);
    }

    // --------------------------------------------------
    // GET BOOK BY ID
    // --------------------------------------------------

    @GetMapping("/{id}")
    @Operation(
            summary = "Get book by ID",
            description = "Returns details of a specific book"
    )
    public BookResponse findById(
            @Parameter(description = "Unique book ID")
            @PathVariable Long id) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Book ID must be greater than zero"
            );
        }

        return bookService.findById(id);
    }

    // --------------------------------------------------
    // VALIDATE PAGINATION
    // --------------------------------------------------

    private void validatePagination(int page, int size) {

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be between 1 and " + MAX_PAGE_SIZE
            );
        }
    }

    // --------------------------------------------------
    // BUILD SORT
    // --------------------------------------------------

    private Sort buildSort(String[] sortParameters) {

        if (sortParameters == null || sortParameters.length == 0) {
            return Sort.by(Sort.Direction.ASC, "title");
        }

        Sort sorting = Sort.unsorted();

        for (String parameter : sortParameters) {

            if (parameter == null || parameter.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid sorting parameter"
                );
            }

            String[] parts = parameter.split(",", -1);

            if (parts.length != 2) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid sort format. Use field,direction"
                );
            }

            String field = parts[0].trim();
            String directionValue = parts[1].trim();

            if (!ALLOWED_SORT_FIELDS.contains(field)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Sorting is not allowed for field: " + field
                );
            }

            Sort.Direction direction;

            try {
                direction = Sort.Direction.fromString(directionValue);
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Sort direction must be ASC or DESC"
                );
            }

            sorting = sorting.and(Sort.by(direction, field));
        }

        return sorting;
    }
}
