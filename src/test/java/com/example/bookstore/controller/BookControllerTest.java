package com.example.bookstore.controller;

import com.example.bookstore.dto.ApiDtos.BookResponse;
import com.example.bookstore.security.CustomUserDetailsService;
import com.example.bookstore.security.JwtService;
import com.example.bookstore.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.argThat;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;


    // --------------------------------------------------
    // GET ALL BOOKS
    // --------------------------------------------------

    @Test
    void findAll_ShouldReturnBooksWithDefaultPagination()
            throws Exception {

        Page<BookResponse> bookPage =
                new PageImpl<>(List.of());

        when(bookService.findAll(any(Pageable.class)))
                .thenReturn(bookPage);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(bookService).findAll(argThat(pageable ->
                pageable.getPageNumber() == 0 &&
                        pageable.getPageSize() == 12 &&
                        pageable.getSort().getOrderFor("title").isAscending()
        ));
    }


    @Test
    void findAll_ShouldReturnBooksWithCustomPagination()
            throws Exception {

        when(bookService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/books")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(bookService).findAll(argThat(pageable ->
                pageable.getPageNumber() == 1 &&
                        pageable.getPageSize() == 5
        ));
    }


    @Test
    void findAll_ShouldSortBooksByTitleDescending()
            throws Exception {

        when(bookService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/books")
                        .param("sort", "title,DESC"))
                .andExpect(status().isOk());

        verify(bookService).findAll(argThat(pageable ->
                pageable.getSort().getOrderFor("title") != null &&
                        pageable.getSort().getOrderFor("title").isDescending()
        ));
    }


    @Test
    void findAll_ShouldSupportMultipleSortParameters()
            throws Exception {

        when(bookService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/books")
                        .param("sort", "title,ASC", "price,DESC"))
                .andExpect(status().isOk());

        verify(bookService).findAll(argThat(pageable -> {

            Sort sort = pageable.getSort();

            return sort.getOrderFor("title") != null
                    && sort.getOrderFor("title").isAscending()
                    && sort.getOrderFor("price") != null
                    && sort.getOrderFor("price").isDescending();
        }));
    }


    // --------------------------------------------------
    // GET BOOK BY ID
    // --------------------------------------------------

    @Test
    void findById_ShouldReturnBook() throws Exception {

        BookResponse response = createBookResponse();

        when(bookService.findById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk());

        verify(bookService).findById(1L);
    }


    @Test
    void findById_ShouldReturnBadRequestForZeroId()
            throws Exception {

        mockMvc.perform(get("/api/books/0"))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).findById(any());
    }


    @Test
    void findById_ShouldReturnBadRequestForNegativeId()
            throws Exception {

        mockMvc.perform(get("/api/books/-1"))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).findById(any());
    }


    // --------------------------------------------------
    // PAGINATION VALIDATION
    // --------------------------------------------------

    @Test
    void findAll_ShouldRejectNegativePage()
            throws Exception {

        mockMvc.perform(get("/api/books")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).findAll(any());
    }


    @Test
    void findAll_ShouldRejectZeroPageSize()
            throws Exception {

        mockMvc.perform(get("/api/books")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).findAll(any());
    }


    @Test
    void findAll_ShouldRejectPageSizeGreaterThan100()
            throws Exception {

        mockMvc.perform(get("/api/books")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).findAll(any());
    }


    // --------------------------------------------------
    // SORT VALIDATION
    // --------------------------------------------------

    @Test
    void findAll_ShouldRejectInvalidSortField()
            throws Exception {

        mockMvc.perform(get("/api/books")
                        .param("sort", "password,ASC"))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).findAll(any());
    }


    @Test
    void findAll_ShouldRejectInvalidSortDirection()
            throws Exception {

        mockMvc.perform(get("/api/books")
                        .param("sort", "title,INVALID"))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).findAll(any());
    }


    @Test
    void findAll_ShouldRejectInvalidSortFormat()
            throws Exception {

        mockMvc.perform(get("/api/books")
                        .param("sort", "title"))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).findAll(any());
    }


    @Test
    void findAll_ShouldRejectBlankSortParameter()
            throws Exception {

        mockMvc.perform(get("/api/books")
                        .param("sort", ""))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).findAll(any());
    }


    // --------------------------------------------------
    // HELPER METHOD
    // --------------------------------------------------

    private BookResponse createBookResponse() {

        // Update constructor arguments according to ApiDtos.BookResponse

        return new BookResponse(
                1L,
                "Effective Java",
                "Joshua Bloch",
                BigDecimal.valueOf(500.0),
                10,
                "description"
        );
    }
}