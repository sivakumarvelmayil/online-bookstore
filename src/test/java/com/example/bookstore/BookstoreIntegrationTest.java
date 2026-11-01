
package com.example.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookstoreIntegrationTest {

    private static final String BOOKS_URL = "/api/books";
    private static final String CART_URL = "/api/cart";

    @Autowired
    private MockMvc mockMvc;

    // --------------------------------------------------
    // BOOK CATALOG TESTS
    // --------------------------------------------------

    @Test
    void bookCatalogIsPublicAndReturnsPagedBooks() throws Exception {

        mockMvc.perform(get(BOOKS_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(12))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber());
    }

    @Test
    void bookCatalogSupportsPagination() throws Exception {

        mockMvc.perform(get(BOOKS_URL)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void bookCatalogSupportsSorting() throws Exception {

        mockMvc.perform(get(BOOKS_URL)
                        .param("sort", "price,desc"))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void bookCatalogSupportsMultipleSortFields() throws Exception {

        mockMvc.perform(get(BOOKS_URL)
                        .param("sort", "price,asc")
                        .param("sort", "title,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void bookCatalogRejectsNegativePageNumber() throws Exception {

        mockMvc.perform(get(BOOKS_URL)
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookCatalogRejectsInvalidPageSize() throws Exception {

        mockMvc.perform(get(BOOKS_URL)
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookCatalogRejectsPageSizeAboveMaximum() throws Exception {

        mockMvc.perform(get(BOOKS_URL)
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookCatalogRejectsUnsupportedSortField() throws Exception {

        mockMvc.perform(get(BOOKS_URL)
                        .param("sort", "password,asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookCatalogRejectsInvalidSortDirection() throws Exception {

        mockMvc.perform(get(BOOKS_URL)
                        .param("sort", "title,invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookCatalogRejectsInvalidSortFormat() throws Exception {

        mockMvc.perform(get(BOOKS_URL)
                        .param("sort", "title"))
                .andExpect(status().isBadRequest());
    }

    // --------------------------------------------------
    // BOOK DETAILS TESTS
    // --------------------------------------------------

    @Test
    void getBookByIdRejectsZeroId() throws Exception {

        mockMvc.perform(get(BOOKS_URL + "/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookByIdRejectsNegativeId() throws Exception {

        mockMvc.perform(get(BOOKS_URL + "/-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookByIdReturnsNotFoundForUnknownBook() throws Exception {

        mockMvc.perform(get(BOOKS_URL + "/999999"))
                .andExpect(status().isNotFound());
    }

    // --------------------------------------------------
    // SECURITY TESTS
    // --------------------------------------------------

    @Test
    void cartRequiresAuthentication() throws Exception {

        mockMvc.perform(get(CART_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ordersRequireAuthentication() throws Exception {

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void checkoutRequiresAuthentication() throws Exception {

        mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders.post(
                                        "/api/orders/checkout"))
                .andExpect(status().isUnauthorized());
    }
}
