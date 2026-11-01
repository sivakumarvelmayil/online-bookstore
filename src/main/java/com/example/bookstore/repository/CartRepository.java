
package com.example.bookstore.repository;

import com.example.bookstore.entity.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Fetches the cart along with its items and associated books.
     * Useful for displaying cart details and checkout.
     */
    @EntityGraph(attributePaths = {"items", "items.book"})
    Optional<Cart> findByUserId(Long userId);

    /**
     * Checks whether a cart exists for a particular user.
     */
    boolean existsByUserId(Long userId);
}
